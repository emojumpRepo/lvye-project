package com.lvye.mindtrip.module.psychology.service.profile;

import com.lvye.mindtrip.framework.security.core.util.SecurityFrameworkUtils;
import com.lvye.mindtrip.module.psychology.dal.dataobject.profile.StudentProfileDO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.timeline.StudentTimelineDO;
import com.lvye.mindtrip.module.psychology.dal.mysql.profile.StudentProfileMapper;
import com.lvye.mindtrip.module.psychology.dal.mysql.profile.StudentTimelineMapper;
import com.lvye.mindtrip.module.system.dal.dataobject.user.AdminUserDO;
import com.lvye.mindtrip.module.system.dal.mysql.user.AdminUserMapper;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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

    @Resource
    private StudentProfileMapper studentProfileMapper;

    @Override
    @Async
    public void saveTimeline(Long studentProfileId, Integer eventType, String title, String bizId){
        // 调用新方法，保持兼容
        saveTimelineWithMeta(studentProfileId, eventType, title, bizId, null, null);
    }

    @Override
    @Async
    public void saveTimelineWithMeta(Long studentProfileId, Integer eventType, String title,
                                     String bizId, String content, Map<String, Object> meta){
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        boolean isFromStudentProfile = false;

        // 如果获取不到当前登录用户ID，则从学生档案中获取
        if (userId == null) {
            StudentProfileDO studentProfile = studentProfileMapper.selectById(studentProfileId);
            if (studentProfile != null) {
                userId = studentProfile.getUserId();
                isFromStudentProfile = true;
            }
        }

        AdminUserDO userDO = adminUserMapper.selectById(userId);
        StudentTimelineDO studentTimelineDO = new StudentTimelineDO();
        studentTimelineDO.setStudentProfileId(studentProfileId);
        studentTimelineDO.setEventType(eventType);
        studentTimelineDO.setTitle(title);
        studentTimelineDO.setContent(content);
        studentTimelineDO.setBizId(bizId);
        studentTimelineDO.setOperator(userDO != null ? userDO.getNickname() : "");
        studentTimelineDO.setMeta(meta);

        // 如果是通过学生档案获取的userId，需要手动设置creator和updater
        if (isFromStudentProfile && userId != null) {
            studentTimelineDO.setCreator(userId.toString());
            studentTimelineDO.setUpdater(userId.toString());
        }

        studentTimelineMapper.insert(studentTimelineDO);
    }

    @Override
    public List<StudentTimelineDO> selectListByStudentProfileId(String studentProfileId){
        return studentTimelineMapper.selectListByStudentProfileId(studentProfileId);
    }

    @Override
    public List<StudentTimelineDO> selectListByBizId(String bizId){
        return studentTimelineMapper.selectListByBizId(bizId);
    }

}
