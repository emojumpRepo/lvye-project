package cn.iocoder.yudao.module.psychology.service.profile;

import cn.iocoder.yudao.module.psychology.dal.dataobject.timeline.StudentTimelineDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.profile.StudentTimelineMapper;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-18
 * @Description:学生时间线服务层
 * @Version: 1.0
 */
@Service
public class StudentTimelineServiceImpl implements StudentTimelineService {

    @Resource
    private StudentTimelineMapper studentTimelineMapper;

    @Override
    @Async
    public void saveTimeline(StudentTimelineDO studentTimelineDO){
        studentTimelineMapper.insert(studentTimelineDO);
    }

    @Override
    public List<StudentTimelineDO> selectListByStudentProfileId(String studentProfileId){
        return studentTimelineMapper.selectListByStudentProfileId(studentProfileId);
    }





}
