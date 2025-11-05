package cn.iocoder.yudao.module.psychology.service.profile;

import cn.iocoder.yudao.module.psychology.dal.dataobject.timeline.StudentTimelineDO;

import java.util.List;
import java.util.Map;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-18
 * @Description:学生时间线服务层
 * @Version: 1.0
 */
public interface StudentTimelineService {

    /**
     * 保存时间线（旧方法，保持兼容）
     * @param studentProfileId
     * @param eventType
     * @param title
     * @param bizId
     */
    void saveTimeline(Long studentProfileId, Integer eventType, String title, String bizId);

    /**
     * 保存时间线（支持meta数据）
     * @param studentProfileId 学生档案ID
     * @param eventType 事件类型
     * @param title 事件标题
     * @param bizId 关联业务ID
     * @param content 事件内容
     * @param meta 扩展元数据
     */
    void saveTimelineWithMeta(Long studentProfileId, Integer eventType, String title, 
                              String bizId, String content, Map<String, Object> meta);

    /**
     * 学生档案id
     * @param studentProfileId
     * @return
     */
    List<StudentTimelineDO> selectListByStudentProfileId(String studentProfileId);

    /**
     * 根据业务ID查询时间线数据
     * @param bizId 业务ID
     * @return 时间线数据列表
     */
    List<StudentTimelineDO> selectListByBizId(String bizId);
}
