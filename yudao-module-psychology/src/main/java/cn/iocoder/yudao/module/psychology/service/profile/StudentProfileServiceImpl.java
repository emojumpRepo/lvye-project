package cn.iocoder.yudao.module.psychology.service.profile;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.*;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentProfileSimpleVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.ParentContactDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileRecordDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.profile.ParentContactMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.profile.StudentProfileMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.profile.StudentProfileRecordMapper;
import cn.iocoder.yudao.module.psychology.enums.ContactEnum;
import cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.psychology.enums.TimelineEventTypeEnum;
import cn.iocoder.yudao.module.psychology.service.common.DataImportService;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMapper;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import cn.iocoder.yudao.module.system.enums.common.SexEnum;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_IMPORT_LIST_IS_EMPTY;

/**
 * 学生档案 Service 实现类
 */
@Service
@Validated
@Slf4j
public class StudentProfileServiceImpl implements StudentProfileService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    static final String USER_INIT_PASSWORD_KEY = "student.defaultPassword";

    static final String SCHOOL_YEAR = "school.year";

    @Resource
    private StudentProfileMapper studentProfileMapper;

    @Resource
    private StudentProfileRecordMapper studentProfileRecordMapper;

    @Resource
    private AdminUserMapper adminUserMapper;

    @Resource
    private PermissionService permissionService;

    @Resource
    private AdminUserService adminUserService;

    @Resource
    private AdminUserApi adminUserApi;

    @Resource
    private DeptApi deptApi;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private ConfigApi configApi;

    @Resource
    private PermissionApi permissionApi;

    @Resource
    private StudentTimelineService studentTimelineService;

    @Resource
    private ParentContactMapper parentContactMapper;

    @Resource
    private DataImportService dataImportService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStudentProfile(@Valid StudentProfileSaveReqVO createReqVO) {
        // 校验学号唯一性
        validateStudentNoUnique(null, createReqVO.getStudentNo());
        // 校验身份证唯一性
        validateIdCardUnique(null, createReqVO.getIdCard());
        //插入用户表
        AdminUserDO adminUserDO = new AdminUserDO();
        adminUserDO.setUsername(createReqVO.getStudentNo());
        adminUserDO.setDeptId(createReqVO.getClassDeptId());
        adminUserDO.setNickname(createReqVO.getName());
        adminUserDO.setSex(createReqVO.getSex());
        adminUserDO.setMobile(createReqVO.getMobile());
        adminUserDO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        String initPassword = configApi.getConfigValueByKey(USER_INIT_PASSWORD_KEY);
        adminUserDO.setPassword(encodePassword(initPassword));
        adminUserMapper.insert(adminUserDO);
        // 插入学生档案表
        StudentProfileDO studentProfile = BeanUtils.toBean(createReqVO, StudentProfileDO.class);
        studentProfile.setUserId(adminUserDO.getId());
        studentProfileMapper.insert(studentProfile);
        //设置学生角色
        Set<Long> roles = new HashSet<>();
        RoleDO studentRole = roleMapper.selectOne(RoleDO::getName, "学生");
        if (studentRole == null) {
            throw exception(ErrorCodeConstants.STUDENT_ROLE_ASSIGN_FAILED);
        }
        roles.add(studentRole.getId());
        permissionService.assignUserRole(adminUserDO.getId(), roles);
        //插入学生历史记录表
        String schoolYear = configApi.getConfigValueByKey(SCHOOL_YEAR);
        StudentProfileRecordDO studentProfileRecordDO = new StudentProfileRecordDO();
        studentProfileRecordDO.setStudentNo(createReqVO.getStudentNo());
        studentProfileRecordDO.setStudyYear(schoolYear);
        studentProfileRecordDO.setGradeDeptId(createReqVO.getGradeDeptId());
        studentProfileRecordDO.setClassDeptId(createReqVO.getClassDeptId());
        studentProfileRecordMapper.insert(studentProfileRecordDO);
        
        //登记时间线（添加meta数据）
        Map<String, Object> meta = new HashMap<>();
        meta.put("profileId", studentProfile.getId());
        meta.put("studentNo", studentProfile.getStudentNo());
        meta.put("studentName", studentProfile.getName());
        meta.put("idCard", studentProfile.getIdCard());
        meta.put("gradeDeptId", createReqVO.getGradeDeptId());
        meta.put("classDeptId", createReqVO.getClassDeptId());
        meta.put("userId", studentProfile.getUserId());
        meta.put("sex", studentProfile.getSex());
        meta.put("enrollmentYear", studentProfile.getEnrollmentYear());
        meta.put("createType", "manual"); // 手动创建
        meta.put("creatorId", SecurityFrameworkUtils.getLoginUserId());
        meta.put("schoolYear", schoolYear);
        
        String content = String.format("创建学生档案：%s（%s）", 
            studentProfile.getName(), studentProfile.getStudentNo());
        
        studentTimelineService.saveTimelineWithMeta(studentProfile.getId(), 
            TimelineEventTypeEnum.PROFILE_CREATED.getType(), 
            TimelineEventTypeEnum.PROFILE_CREATED.getName(), 
            "profile_" + studentProfile.getId(),
            content,
            meta);
        // 返回
        return studentProfile.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStudentProfile(@Valid StudentProfileSaveReqVO updateReqVO) {
        // 校验存在
        StudentProfileDO studentProfileDO = validateStudentProfileExists(updateReqVO.getId());
        // 校验学号唯一性
        validateStudentNoUnique(updateReqVO.getId(), updateReqVO.getStudentNo());
        // 校验身份证唯一性
        validateIdCardUnique(updateReqVO.getId(), updateReqVO.getIdCard());
        // 更新学生表
        StudentProfileDO updateObj = BeanUtils.toBean(updateReqVO, StudentProfileDO.class);
        studentProfileMapper.updateById(updateObj);
        //插入用户表
        AdminUserDO adminUserDO = new AdminUserDO();
        adminUserDO.setId(studentProfileDO.getUserId());
        adminUserDO.setUsername(updateReqVO.getStudentNo());
        adminUserDO.setDeptId(updateReqVO.getClassDeptId());
        adminUserDO.setNickname(updateReqVO.getName());
        adminUserDO.setSex(updateReqVO.getSex());
        adminUserDO.setMobile(updateReqVO.getMobile());
        adminUserDO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        adminUserMapper.updateById(adminUserDO);
        //如果有更换年级或者班级，就要插入记录表
        String schoolYear = configApi.getConfigValueByKey(SCHOOL_YEAR);
        if (!studentProfileDO.getGradeDeptId().equals(updateReqVO.getGradeDeptId()) || !studentProfileDO.getClassDeptId().equals(updateReqVO.getClassDeptId())) {
            StudentProfileRecordDO studentProfileRecordDO = new StudentProfileRecordDO();
            studentProfileRecordDO.setStudentNo(updateReqVO.getStudentNo());
            studentProfileRecordDO.setStudyYear(schoolYear);
            studentProfileRecordDO.setGradeDeptId(updateReqVO.getGradeDeptId());
            studentProfileRecordDO.setClassDeptId(updateReqVO.getClassDeptId());
            studentProfileRecordMapper.insert(studentProfileRecordDO);
        }
        //登记时间线
        //....
    }

    @Override
    public void deleteStudentProfile(Long id) {
        // 校验存在
        StudentProfileDO studentProfileDO = validateStudentProfileExists(id);
        // 删除学生档案
        studentProfileMapper.deleteById(id);
        //删除用户档案
        adminUserService.deleteUser(studentProfileDO.getUserId());
        //删除其他内容....
        //登记时间线
        //....
    }

    private StudentProfileDO validateStudentProfileExists(Long id) {
        StudentProfileDO profileDO = studentProfileMapper.selectById(id);
        if (profileDO == null) {
            throw exception(ErrorCodeConstants.STUDENT_PROFILE_NOT_EXISTS);
        } else {
            return profileDO;
        }
    }

    private void validateStudentNoUnique(Long id, String studentNo) {
        StudentProfileDO studentProfile = studentProfileMapper.selectByStudentNo(studentNo);
        if (studentProfile == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的学生档案
        if (id == null) {
            throw exception(ErrorCodeConstants.STUDENT_NO_DUPLICATE);
        }
        if (!Objects.equals(studentProfile.getId(), id)) {
            throw exception(ErrorCodeConstants.STUDENT_NO_DUPLICATE);
        }
    }

    private void validateIdCardUnique(Long id, String idCard) {
        // 如果身份证为空或空字符串，则不校验（由前端验证处理）
        if (idCard == null || idCard.trim().isEmpty()) {
            return;
        }
        StudentProfileDO studentProfile = studentProfileMapper.selectByIdCard(idCard.trim());
        if (studentProfile == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的学生档案
        if (id == null) {
            throw exception(ErrorCodeConstants.STUDENT_IDCARD_DUPLICATE);
        }
        if (!Objects.equals(studentProfile.getId(), id)) {
            throw exception(ErrorCodeConstants.STUDENT_IDCARD_DUPLICATE);
        }
    }

    @Override
    public StudentProfileVO getStudentProfile(Long studentProfileId) {
        return studentProfileMapper.selectInfoByStudentProfileId(studentProfileId);
    }

    @Override
    public PageResult<StudentProfileVO> getStudentProfilePage(StudentProfilePageReqVO pageReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        DeptDataPermissionRespDTO dataPerm = permissionApi.getDeptDataPermission(userId);

        // 计算可见部门ID集合
        java.util.Collection<Long> deptIds = (dataPerm != null && dataPerm.getDeptIds() != null)
                ? dataPerm.getDeptIds() : java.util.Collections.emptyList();
        Long selfUserId = (dataPerm != null && Boolean.TRUE.equals(dataPerm.getSelf())) ? userId : null;

        IPage<StudentProfileVO> page = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        studentProfileMapper.selectPageList(page, pageReqVO, deptIds, selfUserId);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public List<StudentProfileVO> getStudentProfileList(StudentProfilePageReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        DeptDataPermissionRespDTO dataPerm = permissionApi.getDeptDataPermission(userId);
        java.util.Collection<Long> deptIds = (dataPerm != null && dataPerm.getDeptIds() != null)
                ? dataPerm.getDeptIds() : java.util.Collections.emptyList();
        Long selfUserId = (dataPerm != null && Boolean.TRUE.equals(dataPerm.getSelf())) ? userId : null;

        // 设置不分页，获取所有数据
        IPage<StudentProfileVO> page = new Page<>(1, Integer.MAX_VALUE);
        studentProfileMapper.selectPageList(page, reqVO, deptIds, selfUserId);
        return page.getRecords();
    }

    @Override
    public StudentProfileDO getStudentProfileByNo(String studentNo) {
        return studentProfileMapper.selectByStudentNo(studentNo);
    }

    @Override
    public StudentProfileDO getStudentProfileByUserId(Long userId) {
        return studentProfileMapper.selectByUserId(userId);
    }

    @Override
    public StudentProfileDO getStudentProfileByIdCard(String idCard) {
        if (idCard == null || idCard.trim().isEmpty()) {
            return null;
        }
        return studentProfileMapper.selectByIdCard(idCard.trim());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudentProfileImportRespVO importStudentProfile(List<StudentImportExcelVO> studentList, boolean isUpdateSupport) {
        StudentProfileImportRespVO respVO = new StudentProfileImportRespVO();
        // 1.1 参数校验
        if (CollUtil.isEmpty(studentList)) {
            throw exception(ErrorCodeConstants.STUDENT_IMPORT_LIST_IS_EMPTY);
        }
        int successCount = 0;
        int failureCount = 0;
        StringBuilder failReason = new StringBuilder();

        for (StudentImportExcelVO student : studentList) {
            StudentProfileDO studentProfileDO = studentProfileMapper.selectByStudentNo(student.getStudentNo());
            if (Objects.isNull(studentProfileDO)) {
                try {
                    dataImportService.saveStudentInfoByExcel(student);
                    successCount = successCount++;
                } catch (Exception e) {
                    logger.error("学生:" + student.getName() + "导入失败" + "\n");
                    failReason.append("教师:" + student.getName() + "导入失败" + "\n");
                    failureCount = failureCount++;
                }
            } else {
                if (isUpdateSupport) {
                    //暂不实现
                }
            }
        }
        respVO.setSuccessCount(successCount);
        respVO.setFailureCount(failureCount);
        respVO.setFailReason(failReason.toString());
        return respVO;
    }

    @Override
    public void updatePsychologicalStatus(Long id, Integer psychologicalStatus, Integer riskLevel) {
        // 校验存在
        validateStudentProfileExists(id);
        // 更新心理状态
        StudentProfileDO updateObj = new StudentProfileDO();
        updateObj.setId(id);
        updateObj.setPsychologicalStatus(psychologicalStatus);
        updateObj.setRiskLevel(riskLevel);
        studentProfileMapper.updateById(updateObj);
    }

    @Override
    public void graduateStudent(Long id) {
        // 校验存在
        StudentProfileDO studentProfile = studentProfileMapper.selectById(id);
        if (studentProfile == null) {
            throw exception(ErrorCodeConstants.STUDENT_PROFILE_NOT_EXISTS);
        }

        // 更新毕业状态
        StudentProfileDO updateObj = new StudentProfileDO();
        updateObj.setId(id);
        updateObj.setGraduationStatus(1); // 1-已毕业
        studentProfileMapper.updateById(updateObj);
    }

    @Override
    public StudentProfileVO getStudentProfileDetailByUserId(Long userId) {
        return studentProfileMapper.selectInfoByUserId(userId);
    }

    @Override
    public List<StudentProfileDO> getStudentListByClassIds(Collection<Long> classIds){
        if (CollUtil.isEmpty(classIds)) {
            return Collections.emptyList();
        }
        return studentProfileMapper.selectListByClassIds(classIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStudentBasicInfo(@Valid StudentProfileBasicInfoUpdateReqVO updateReqVO) {
        // 校验存在
        validateStudentProfileExists(updateReqVO.getId());
        
        // 构建更新对象
        StudentProfileDO updateObj = new StudentProfileDO();
        updateObj.setId(updateReqVO.getId());
        updateObj.setSex(updateReqVO.getSex());
        updateObj.setEthnicity(updateReqVO.getEthnicity());
        updateObj.setActualAge(updateReqVO.getActualAge());
        updateObj.setBirthDate(updateReqVO.getBirthDate());
        updateObj.setHeight(updateReqVO.getHeight());
        updateObj.setWeight(updateReqVO.getWeight());
        
        // 处理家中孩子情况JSON
        if (updateReqVO.getFamilyChildrenInfo() != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                updateObj.setFamilyChildrenInfo(objectMapper.writeValueAsString(updateReqVO.getFamilyChildrenInfo()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("家中孩子情况JSON序列化失败", e);
            }
        }
        
        // 更新数据库
        studentProfileMapper.updateById(updateObj);
    }

    @Override
    public StudentProfileCompletenessRespVO checkProfileCompleteness(Long id) {
        // 校验存在
        StudentProfileDO studentProfile = validateStudentProfileExists(id);
        
        StudentProfileCompletenessRespVO respVO = new StudentProfileCompletenessRespVO();
        respVO.setId(id);
        respVO.setStudentNo(studentProfile.getStudentNo());
        respVO.setName(studentProfile.getName());
        
        // 定义必填字段
        List<StudentProfileCompletenessRespVO.FieldCompletenessVO> fieldCompleteness = new ArrayList<>();
        List<String> missingFields = new ArrayList<>();
        
        // 检查各字段完善情况（最后一个布尔为 JSON 字段判定）
        checkFieldCompleteness(fieldCompleteness, missingFields, "性别", "sex", studentProfile.getSex(), false, true);
        checkFieldCompleteness(fieldCompleteness, missingFields, "民族", "ethnicity", studentProfile.getEthnicity(), false, true);
        checkFieldCompleteness(fieldCompleteness, missingFields, "实际年龄", "actualAge", studentProfile.getActualAge(), false, true);
        checkFieldCompleteness(fieldCompleteness, missingFields, "出生日期", "birthDate", studentProfile.getBirthDate(), false, true);
        checkFieldCompleteness(fieldCompleteness, missingFields, "身高", "height", studentProfile.getHeight(), false, true);
        checkFieldCompleteness(fieldCompleteness, missingFields, "体重", "weight", studentProfile.getWeight(), false, true);
        checkFieldCompleteness(fieldCompleteness, missingFields, "家中孩子情况", "familyChildrenInfo", studentProfile.getFamilyChildrenInfo(), true, true);
        
        respVO.setFieldCompleteness(fieldCompleteness);
        respVO.setMissingFields(missingFields);
        
        // 仅以必填字段计算完善度百分比
        int totalRequired = (int) fieldCompleteness.stream()
                .filter(StudentProfileCompletenessRespVO.FieldCompletenessVO::getIsRequired)
                .count();
        int filledRequired = (int) fieldCompleteness.stream()
                .filter(f -> Boolean.TRUE.equals(f.getIsRequired()) && Boolean.TRUE.equals(f.getIsFilled()))
                .count();
        int completenessPercentage = totalRequired > 0 ? (filledRequired * 100 / totalRequired) : 100;
        
        respVO.setCompletenessPercentage(completenessPercentage);
        respVO.setIsComplete(completenessPercentage == 100);
        
        return respVO;
    }

    @Override
    public void updateStudentRiskLevel(Long studentProfileId, Integer riskLevel){
        studentProfileMapper.updateRiskLevel(studentProfileId, riskLevel);
    }

    @Override
    public List<StudentProfileSimpleVO> searchSimpleStudentProfilesByStudentNoAndName(String studentNo, String name) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        DeptDataPermissionRespDTO dataPerm = permissionApi.getDeptDataPermission(userId);
        
        // 计算可见部门ID集合
        java.util.Collection<Long> deptIds = (dataPerm != null && dataPerm.getDeptIds() != null)
                ? dataPerm.getDeptIds() : java.util.Collections.emptyList();
        Long selfUserId = (dataPerm != null && Boolean.TRUE.equals(dataPerm.getSelf())) ? userId : null;
        
        return studentProfileMapper.searchSimpleByStudentNoAndName(studentNo, name, deptIds, selfUserId);
    }

    /**
     * 检查字段完善情况
     */
    private void checkFieldCompleteness(List<StudentProfileCompletenessRespVO.FieldCompletenessVO> fieldCompleteness,
                                       List<String> missingFields,
                                       String fieldName,
                                       String fieldCode,
                                       Object fieldValue,
                                       boolean isJson,
                                       boolean isRequired) {
        StudentProfileCompletenessRespVO.FieldCompletenessVO fieldVO = new StudentProfileCompletenessRespVO.FieldCompletenessVO();
        fieldVO.setFieldName(fieldName);
        fieldVO.setFieldCode(fieldCode);
        fieldVO.setIsRequired(isRequired);

        boolean isFilled = isValueFilled(fieldValue, isJson);
        fieldVO.setIsFilled(isFilled);
        
        if (isFilled) {
            fieldVO.setFieldValue(fieldValue.toString());
        } else if (isRequired) {
            missingFields.add(fieldName);
        }
        
        fieldCompleteness.add(fieldVO);
    }

    private boolean isValueFilled(Object value, boolean isJson) {
        if (value == null) {
            return false;
        }
        if (value instanceof CharSequence) {
            String s = value.toString().trim();
            if (s.isEmpty()) {
                return false;
            }
            if (isJson) {
                // 将空对象或空数组视为未填写
                if ("{}".equals(s) || "[]".equals(s)) {
                    return false;
                }
            }
            return true;
        }
        // 数字、日期等非字符串类型，只要非 null 即视为已填写
        return true;
    }

    /**
     * 对密码进行加密
     *
     * @param password 密码
     * @return 加密后的密码
     */
    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

}