package com.lvye.mindtrip.module.psychology.service.profile;

import com.lvye.mindtrip.module.psychology.controller.admin.profile.vo.StudentParentProfileReqVO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.profile.ParentContactDO;

import java.util.List;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-12
 * @Description:学生监护人档案服务层
 * @Version: 1.0
 */
public interface StudentParentProfileService {

    /**
     * 创建学生家长档案
     * @param createReqVO
     */
    void createStudentParentContact(StudentParentProfileReqVO createReqVO);

    /**
     * 修改学生家长档案
     * @param createReqVO
     */
    void updateStudentParentContact(StudentParentProfileReqVO createReqVO);

    /**
     * 删除学生家长档案
     * @param contactId
     */
    void deleteStudentParentContact(Long contactId);

    /**
     * 删除学生家长档案(根据学生档案ID)
     * @param studentProfileId
     */
    void deleteStudentParentContactByStudentProfileId(Long studentProfileId);

    /**
     * 查询家长档案
     * @param studentProfileId
     * @return
     */
    List<ParentContactDO> selectStudentParentContactByStudentProfileId(Long studentProfileId);
}
