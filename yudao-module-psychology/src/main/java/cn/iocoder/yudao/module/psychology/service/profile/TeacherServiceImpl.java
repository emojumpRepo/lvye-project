package cn.iocoder.yudao.module.psychology.service.profile;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.psychology.controller.admin.common.vo.TeacherImportExcelVO;
import cn.iocoder.yudao.module.psychology.controller.admin.common.vo.TeacherProfileImportRespVO;
import cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.psychology.service.common.DataImportService;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

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
    private DataImportService dataImportService;

    @Override
    public TeacherProfileImportRespVO importTeacher(List<TeacherImportExcelVO> teacherList, boolean isUpdateSupport){
        TeacherProfileImportRespVO respVO = new TeacherProfileImportRespVO();
        // 1.1 参数校验
        if (CollUtil.isEmpty(teacherList)) {
            throw exception(ErrorCodeConstants.TEACHER_IMPORT_LIST_IS_EMPTY);
        }
        int successCount = 0;
        int failureCount = 0;
        StringBuilder failReason = new StringBuilder();
        for(TeacherImportExcelVO teacher : teacherList){
            AdminUserDO adminUserDO = userMapper.selectByUsername(teacher.getJobNo());
            if (Objects.isNull(adminUserDO)) {
                try {
                    dataImportService.saveTeacherInfoByExcel(teacher);
                    successCount = successCount++;
                } catch (Exception e) {
                    logger.error("教师:" + teacher.getName() + "导入失败" + "\n");
                    failReason.append("教师:" + teacher.getName() + "导入失败" + "\n");
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
        return null;
    }




}
