package cn.iocoder.yudao.module.psychology.service.profile;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.psychology.dal.dataobject.timeline.StudentTimelineDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.profile.StudentTimelineMapper;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
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

    @Resource
    private AdminUserMapper adminUserMapper;

    @Override
    @Async
    public void saveTimeline(Long studentProfileId, Integer eventType, String title, String bizId){
        Long userId = SecurityFrameworkUtils.getLoginUserId();;
        AdminUserDO userDO = adminUserMapper.selectById(userId);
        StudentTimelineDO studentTimelineDO = new StudentTimelineDO();
        studentTimelineDO.setStudentProfileId(studentProfileId);
        studentTimelineDO.setEventType(eventType);
        studentTimelineDO.setTitle(title);
        studentTimelineDO.setContent(null);
        studentTimelineDO.setBizId(bizId);
        studentTimelineDO.setOperator(userDO != null ? userDO.getNickname() : "");
        studentTimelineMapper.insert(studentTimelineDO);
    }

    @Override
    public List<StudentTimelineDO> selectListByStudentProfileId(String studentProfileId){
        return studentTimelineMapper.selectListByStudentProfileId(studentProfileId);
    }

}
