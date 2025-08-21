package cn.iocoder.yudao.module.psychology.service.profile;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.common.vo.TeacherImportExcelVO;
import cn.iocoder.yudao.module.psychology.controller.admin.common.vo.TeacherProfileImportRespVO;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentImportExcelVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.dept.DeptMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMapper;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-21
 * @Description:教师信息服务层
 * @Version: 1.0
 */
@Service
public class TeacherServiceImpl implements TeacherService{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private AdminUserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private DeptMapper deptMapper;

    @Override
    public TeacherProfileImportRespVO importTeacher(List<TeacherImportExcelVO> teacherList, boolean isUpdateSupport){
        TeacherProfileImportRespVO respVO = new TeacherProfileImportRespVO();
        // 1.1 参数校验
        if (CollUtil.isEmpty(teacherList)) {
            throw exception(ErrorCodeConstants.TEACHER_IMPORT_LIST_IS_EMPTY);
        }
        int successCount = 0;
        int failureCount = 0;
        for(TeacherImportExcelVO teacher : teacherList){
            AdminUserDO adminUserDO = userMapper.selectByUsername(teacher.getJobNo());
            if (Objects.isNull(adminUserDO)) {
                try {
                    this.saveTeacherInfoByExcel(teacher);
                    successCount = successCount++;
                } catch (Exception e) {
                    logger.error("教师:" + teacher.getName() + "导入失败");
                    respVO.setFailReason(e.getMessage());
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
        return null;
    }

    private void saveTeacherInfoByExcel(TeacherImportExcelVO teacher) {
        //检查工号
        validateTeacherNoUnique(teacher.getJobNo());
        //检查角色
        validateRoleExists(teacher.getRole());
        String[] className = teacher.getClassName().split(",");
        String[] headTeacherClassName = teacher.getHeadTeacherClassName().split(",");
        String[] managerClassName = teacher.getManageGradeName().split(",");
        //新建用户

        //赋予角色

        //赋予部门

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
     * 检查角色是否存在
     * @param roleName
     */
    private void validateRoleExists(String roleName) {
        RoleDO roleDO = roleMapper.selectByName(roleName);
        if (roleDO != null) {
            throw exception(ErrorCodeConstants.ROLE_NOT_EXISTS);
        }
    }

}
