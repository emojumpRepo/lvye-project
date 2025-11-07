package com.lvye.mindtrip.module.psychology.service.counselor;

import com.lvye.mindtrip.module.psychology.dal.dataobject.counselor.StudentCounselorAssignmentDO;

import java.time.LocalDate;
import java.util.List;

/**
 * 学生咨询师分配关系 Service 接口
 *
 * @author 芋道源码
 */
public interface StudentCounselorAssignmentService {

    /**
     * 分配主责咨询师给学生
     *
     * @param studentProfileId 学生档案ID
     * @param counselorUserId 咨询师用户ID
     * @param assignmentReason 分配原因
     * @return 分配记录ID
     */
    Long assignPrimaryCounselor(Long studentProfileId, Long counselorUserId, String assignmentReason);

    /**
     * 分配临时咨询师给学生
     *
     * @param studentProfileId 学生档案ID
     * @param counselorUserId 咨询师用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param assignmentReason 分配原因
     * @return 分配记录ID
     */
    Long assignTemporaryCounselor(Long studentProfileId, Long counselorUserId, 
                                  LocalDate startDate, LocalDate endDate, String assignmentReason);

    /**
     * 获取学生的主责咨询师
     *
     * @param studentProfileId 学生档案ID
     * @return 咨询师分配记录，如果没有则返回null
     */
    StudentCounselorAssignmentDO getPrimaryCounselor(Long studentProfileId);

    /**
     * 获取学生的所有有效咨询师
     *
     * @param studentProfileId 学生档案ID
     * @return 咨询师分配记录列表
     */
    List<StudentCounselorAssignmentDO> getActiveCounselors(Long studentProfileId);

    /**
     * 获取咨询师负责的学生列表
     *
     * @param counselorUserId 咨询师用户ID
     * @param assignmentType 分配类型（可选）
     * @return 学生分配记录列表
     */
    List<StudentCounselorAssignmentDO> getCounselorStudents(Long counselorUserId, Integer assignmentType);

    /**
     * 批量获取学生的主责咨询师
     *
     * @param studentProfileIds 学生档案ID列表
     * @return 咨询师分配记录列表
     */
    List<StudentCounselorAssignmentDO> batchGetPrimaryCounselors(List<Long> studentProfileIds);

    /**
     * 结束咨询师分配关系
     *
     * @param assignmentId 分配记录ID
     */
    void endAssignment(Long assignmentId);

    /**
     * 更新咨询师分配信息
     *
     * @param assignmentId 分配记录ID
     * @param endDate 新的结束日期
     * @param assignmentReason 更新原因
     */
    void updateAssignment(Long assignmentId, LocalDate endDate, String assignmentReason);

    /**
     * 检查学生是否有主责咨询师
     *
     * @param studentProfileId 学生档案ID
     * @return 是否有主责咨询师
     */
    boolean hasPrimaryCounselor(Long studentProfileId);

    /**
     * 获取学生的主责咨询师ID
     * 如果没有主责咨询师，返回默认咨询师ID
     *
     * @param studentProfileId 学生档案ID
     * @param defaultCounselorUserId 默认咨询师ID
     * @return 咨询师用户ID
     */
    Long getCounselorUserIdOrDefault(Long studentProfileId, Long defaultCounselorUserId);

}