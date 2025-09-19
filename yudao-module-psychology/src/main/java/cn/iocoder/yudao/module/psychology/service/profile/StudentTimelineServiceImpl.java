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
        AdminUserDO userDO = adminUserMapper.selectById(userId);
        StudentTimelineDO studentTimelineDO = new StudentTimelineDO();
        studentTimelineDO.setStudentProfileId(studentProfileId);
        studentTimelineDO.setEventType(eventType);
        studentTimelineDO.setTitle(title);
        studentTimelineDO.setContent(content);
        studentTimelineDO.setBizId(bizId);
        studentTimelineDO.setOperator(userDO != null ? userDO.getNickname() : "");
        studentTimelineDO.setMeta(meta);
        studentTimelineMapper.insert(studentTimelineDO);
    }

    @Override
    public List<StudentTimelineDO> selectListByStudentProfileId(String studentProfileId){
        return studentTimelineMapper.selectListByStudentProfileId(studentProfileId);
    }

}
