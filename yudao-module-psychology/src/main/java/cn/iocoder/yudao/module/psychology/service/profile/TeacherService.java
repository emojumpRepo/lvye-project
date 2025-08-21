package cn.iocoder.yudao.module.psychology.service.profile;

import cn.iocoder.yudao.module.psychology.controller.admin.common.vo.TeacherImportExcelVO;
import cn.iocoder.yudao.module.psychology.controller.admin.common.vo.TeacherProfileImportRespVO;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentImportExcelVO;

import java.util.List;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-21
 * @Description:教师信息服务层
 * @Version: 1.0
 */
public interface TeacherService {

    /**
     * 批量导入学生档案
     * @param studentList
     * @param isUpdateSupport
     * @return
     */
    TeacherProfileImportRespVO importTeacher(List<TeacherImportExcelVO> studentList, boolean isUpdateSupport);

}
