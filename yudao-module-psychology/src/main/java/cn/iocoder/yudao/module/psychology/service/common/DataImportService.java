package cn.iocoder.yudao.module.psychology.service.common;

import cn.hutool.core.collection.CollectionUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.infra.api.config.ConfigApi;
import cn.iocoder.yudao.module.psychology.controller.admin.common.vo.TeacherImportExcelVO;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentImportExcelVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.ParentContactDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileRecordDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.profile.ParentContactMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.profile.StudentProfileMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.profile.StudentProfileRecordMapper;
import cn.iocoder.yudao.module.psychology.enums.ContactEnum;
import cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserDeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.dept.DeptMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserDeptMapper;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import cn.iocoder.yudao.module.system.enums.common.SexEnum;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-23
 * @Description:数据插入服务层
 * @Version: 1.0
 */
@Service
public class DataImportService {

    private static final String USER_INIT_PASSWORD_KEY = "student.defaultPassword";
    private static final String SCHOOL_YEAR = "school.year";
    private static final String gradeManager = "年级管理员";
    private static final String psychologyTeacher = "心理老师";
    private static final String normalTeacher = "普通老师";

    @Resource
    private AdminUserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private DeptMapper deptMapper;

    @Resource
    private ConfigApi configApi;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private PermissionService permissionService;

    @Resource
    private UserDeptMapper userDeptMapper;

    @Resource
    private StudentProfileMapper studentProfileMapper;

    @Resource
    private ParentContactMapper parentContactMapper;

    @Resource
    private StudentProfileRecordMapper studentProfileRecordMapper;

    /**
     * 保存老师信息
     * @param teacher
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveTeacherInfoByExcel(TeacherImportExcelVO teacher) {
        //检查工号
        validateTeacherNoUnique(teacher.getJobNo());
        //检查手机号
        validateMobileNoUnique(teacher.getMobile());
        //检查角色
        RoleDO roleDO = validateRoleExists(teacher.getRole());
        String[] classNames = teacher.getClassName().split(",");
        String[] headTeacherClassName = teacher.getHeadTeacherClassName().split(",");
        String[] managerClassNames = teacher.getManageGradeName().split(",");
        List<Long> classList = new ArrayList<>();
        switch (teacher.getRole()){
            case normalTeacher -> {

            }
            case psychologyTeacher -> {
                for (String className : classNames){
                    DeptDO deptDO = validateClassOrGradeExists(className);
                    classList.add(deptDO.getId());
                }
            }
            case gradeManager -> {
                for (String className : managerClassNames){
                    DeptDO deptDO = validateClassOrGradeExists(className);
                    classList.add(deptDO.getId());
                }
            }
        }
        //新建用户
        AdminUserDO adminUserDO = new AdminUserDO();
        adminUserDO.setUsername(teacher.getJobNo());
        adminUserDO.setDeptId(null);
        adminUserDO.setNickname(teacher.getName());
        adminUserDO.setSex(SexEnum.UNKNOWN.getSex());
        adminUserDO.setMobile(teacher.getMobile());
        adminUserDO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        String initPassword = configApi.getConfigValueByKey(USER_INIT_PASSWORD_KEY);
        adminUserDO.setPassword(encodePassword(initPassword));
        userMapper.insert(adminUserDO);
        //赋予角色
        Set<Long> roles = new HashSet<>();
        roles.add(roleDO.getId());
        permissionService.assignUserRole(adminUserDO.getId(), roles);
        //赋予部门
        if (!CollectionUtil.isEmpty(classList)) {
            userDeptMapper.insertBatch(CollectionUtils.convertList(classList, deptId -> {
                UserDeptDO entity = new UserDeptDO();
                entity.setUserId(adminUserDO.getId());
                entity.setDeptId(deptId);
                return entity;
            }));
        }
    }

    /**
     * 保存学生信息
     * @param student
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveStudentInfoByExcel(StudentImportExcelVO student) {
        // 校验学号唯一性
        validateStudentNoUnique(null, student.getStudentNo());
        DeptDO gradeDept = deptMapper.selectByName(student.getGradeName());
        DeptDO classDept = deptMapper.selectByName(student.getClassName());
        if (gradeDept == null || classDept == null) {
            throw exception(ErrorCodeConstants.STUDENT_GRADE_OR_CLASS_IS_EMPTY);
        }
        if (classDept.getParentId().equals(gradeDept.getId())) {
            throw exception(ErrorCodeConstants.STUDENT_GRADE_OR_CLASS_NOT_MATCH);
        }
        //插入用户表
        AdminUserDO adminUserDO = new AdminUserDO();
        adminUserDO.setUsername(student.getStudentNo());
        adminUserDO.setDeptId(classDept.getId());
        adminUserDO.setNickname(student.getName());
        adminUserDO.setSex(SexEnum.getName(student.getSex()));
        adminUserDO.setMobile(student.getMobile());
        adminUserDO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        String initPassword = configApi.getConfigValueByKey(USER_INIT_PASSWORD_KEY);
        adminUserDO.setPassword(encodePassword(initPassword));
        userMapper.insert(adminUserDO);
        // 插入学生档案表
        StudentProfileDO studentProfile = BeanUtils.toBean(student, StudentProfileDO.class);
        studentProfile.setUserId(adminUserDO.getId());
        studentProfile.setGraduationStatus(0);
        studentProfile.setGraduationStatus(1);
        studentProfileMapper.insert(studentProfile);
        //设置学生角色
        Set<Long> roles = new HashSet<>();
        roles.add(roleMapper.selectOne(RoleDO::getName, "学生").getId());
        permissionService.assignUserRole(adminUserDO.getId(), roles);
        //插入学生历史记录表
        String schoolYear = configApi.getConfigValueByKey(SCHOOL_YEAR);
        StudentProfileRecordDO studentProfileRecordDO = new StudentProfileRecordDO();
        studentProfileRecordDO.setStudentNo(student.getStudentNo());
        studentProfileRecordDO.setStudyYear(schoolYear);
        studentProfileRecordDO.setGradeDeptId(gradeDept.getId());
        studentProfileRecordDO.setClassDeptId(classDept.getId());
        studentProfileRecordMapper.insert(studentProfileRecordDO);
        //插入学生家长信息
        ParentContactDO parentContactDO = new ParentContactDO();
        parentContactDO.setStudentProfileId(studentProfile.getId());
        parentContactDO.setName(student.getParentName());
        parentContactDO.setMobile(student.getParentMobile());
        parentContactDO.setRelation(ContactEnum.getCode(student.getRelation()).getCode());
        parentContactMapper.insert(parentContactDO);
    }

    /**
     * 检查工号是否唯一
     * @param jobNo
     */
    private void validateTeacherNoUnique(String jobNo) {
        AdminUserDO adminUserDO = userMapper.selectByUsername(jobNo);
        if (adminUserDO != null) {
            throw exception(ErrorCodeConstants.TEACHER_NO_DUPLICATE);
        }
    }

    /**
     * 检查手机号是否唯一
     * @param mobile
     */
    private void validateMobileNoUnique(String mobile) {
        AdminUserDO adminUserDO = userMapper.selectByMobile(mobile);
        if (adminUserDO != null) {
            throw exception(ErrorCodeConstants.MOBILE_NO_DUPLICATE);
        }
    }


    /**
     * 检查角色是否存在
     * @param roleName
     */
    private RoleDO validateRoleExists(String roleName) {
        RoleDO roleDO = roleMapper.selectByName(roleName);
        if (roleDO == null) {
            throw exception(ErrorCodeConstants.ROLE_NOT_EXISTS);
        }
        return roleDO;
    }

    /**
     * 检查班级/年级是否存在
     * @param className
     */
    private DeptDO validateClassOrGradeExists(String className) {
        DeptDO deptDO = deptMapper.selectByName(className);
        if (deptDO == null) {
            throw exception(ErrorCodeConstants.DEPT_NOT_EXISTS);
        }
        return deptDO;
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

    /**
     * 校验学号是否存在
     * @param id
     * @param studentNo
     */
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
}
