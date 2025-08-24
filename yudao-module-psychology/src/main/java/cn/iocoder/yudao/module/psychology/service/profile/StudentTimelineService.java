package cn.iocoder.yudao.module.psychology.service.profile;

import cn.iocoder.yudao.module.psychology.dal.dataobject.timeline.StudentTimelineDO;

import java.util.List;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-18
 * @Description:学生时间线服务层
 * @Version: 1.0
 */
public interface StudentTimelineService {

    /**
     * 保存时间线
     * @param studentTimelineDO
     */
    void saveTimeline(StudentTimelineDO studentTimelineDO);

    /**
     * 学生档案id
     * @param studentProfileId
     * @return
     */
    List<StudentTimelineDO> selectListByStudentProfileId(String studentProfileId);
}
