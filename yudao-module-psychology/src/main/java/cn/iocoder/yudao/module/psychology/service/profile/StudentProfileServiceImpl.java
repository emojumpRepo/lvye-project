package cn.iocoder.yudao.module.psychology.service.profile;

import cn.iocoder.yudao.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileRecordDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.profile.StudentProfileMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.profile.StudentProfileRecordMapper;
import cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMapper;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 学生档案 Service 实现类
 */
@Service
@Validated
@Slf4j
public class StudentProfileServiceImpl implements StudentProfileService {

    static final String USER_INIT_PASSWORD_KEY = "system.user.init-password";

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStudentProfile(@Valid StudentProfileSaveReqVO createReqVO) {
        // 校验学号唯一性
        validateStudentNoUnique(null, createReqVO.getStudentNo());
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
        roles.add(roleMapper.selectOne(RoleDO::getName, "学生").getId());
        permissionService.assignUserRole(adminUserDO.getId(), roles);
        //插入学生历史记录表
        StudentProfileRecordDO studentProfileRecordDO = new StudentProfileRecordDO();
        studentProfileRecordDO.setStudentNo(createReqVO.getStudentNo());
//        studentProfileRecordDO.setStudyYear()
        studentProfileRecordDO.setGradeDeptId(createReqVO.getGradeDeptId());
        studentProfileRecordDO.setClassDeptId(createReqVO.getClassDeptId());
        studentProfileRecordMapper.insert(studentProfileRecordDO);
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
        if (!studentProfileDO.getGradeDeptId().equals(updateReqVO.getGradeDeptId()) || !studentProfileDO.getClassDeptId().equals(updateReqVO.getClassDeptId())){
            StudentProfileRecordDO studentProfileRecordDO = new StudentProfileRecordDO();
            studentProfileRecordDO.setStudentNo(updateReqVO.getStudentNo());
            //        studentProfileRecordDO.setStudyYear()
            studentProfileRecordDO.setGradeDeptId(updateReqVO.getGradeDeptId());
            studentProfileRecordDO.setClassDeptId(updateReqVO.getClassDeptId());
            studentProfileRecordMapper.insert(studentProfileRecordDO);
        }
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

    @Override
    public StudentProfileVO getStudentProfile(Long studentProfileId) {
        return studentProfileMapper.selectInfoByStudentProfileId(studentProfileId);
    }

    @Override
    public PageResult<StudentProfileVO> getStudentProfilePage(StudentProfilePageReqVO pageReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        DeptDataPermissionRespDTO deptDataPermissionRespDTO = permissionApi.getDeptDataPermission(userId);

        IPage<StudentProfileVO> page = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        studentProfileMapper.selectPageList(page, pageReqVO);
        return new PageResult<>(page.getRecords(), page.getTotal());
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
    @Transactional(rollbackFor = Exception.class)
    public StudentProfileImportRespVO importStudentProfile(StudentProfileImportReqVO importReqVO) {
        StudentProfileImportRespVO respVO = new StudentProfileImportRespVO();
        
        if (importReqVO.getStudentProfiles() == null || importReqVO.getStudentProfiles().isEmpty()) {
            respVO.setSuccessCount(0);
            respVO.setFailureCount(0);
            return respVO;
        }

        int successCount = 0;
        int failureCount = 0;
        
        for (StudentProfileSaveReqVO profile : importReqVO.getStudentProfiles()) {
            try {
                createStudentProfile(profile);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("[importStudentProfile][导入学生档案失败] 学号: {}, 错误: {}", profile.getStudentNo(), e.getMessage());
            }
        }
        
        respVO.setSuccessCount(successCount);
        respVO.setFailureCount(failureCount);
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