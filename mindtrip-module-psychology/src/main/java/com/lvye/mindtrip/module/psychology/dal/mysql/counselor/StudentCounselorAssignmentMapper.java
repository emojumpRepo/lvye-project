package com.lvye.mindtrip.module.psychology.dal.mysql.counselor;

import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lvye.mindtrip.module.psychology.dal.dataobject.counselor.StudentCounselorAssignmentDO;
import com.lvye.mindtrip.module.psychology.enums.AssignmentStatusEnum;
import com.lvye.mindtrip.module.psychology.enums.AssignmentTypeEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学生咨询师分配关系 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface StudentCounselorAssignmentMapper extends BaseMapperX<StudentCounselorAssignmentDO> {

    /**
     * 根据学生档案ID查询有效的主责咨询师
     *
     * @param studentProfileId 学生档案ID
     * @return 咨询师分配记录
     */
    default StudentCounselorAssignmentDO selectPrimaryCounselor(Long studentProfileId) {
        return selectOne(new LambdaQueryWrapperX<StudentCounselorAssignmentDO>()
                .eq(StudentCounselorAssignmentDO::getStudentProfileId, studentProfileId)
                .eq(StudentCounselorAssignmentDO::getAssignmentType, AssignmentTypeEnum.PRIMARY.getType())
                .eq(StudentCounselorAssignmentDO::getStatus, AssignmentStatusEnum.ACTIVE.getStatus()));
    }

    /**
     * 根据学生档案ID查询所有有效的咨询师
     *
     * @param studentProfileId 学生档案ID
     * @return 咨询师分配记录列表
     */
    default List<StudentCounselorAssignmentDO> selectActiveCounselors(Long studentProfileId) {
        return selectList(new LambdaQueryWrapperX<StudentCounselorAssignmentDO>()
                .eq(StudentCounselorAssignmentDO::getStudentProfileId, studentProfileId)
                .eq(StudentCounselorAssignmentDO::getStatus, AssignmentStatusEnum.ACTIVE.getStatus())
                .orderByAsc(StudentCounselorAssignmentDO::getAssignmentType));
    }

    /**
     * 根据咨询师ID查询负责的学生列表
     *
     * @param counselorUserId 咨询师用户ID
     * @param assignmentType 分配类型（可选）
     * @return 学生分配记录列表
     */
    default List<StudentCounselorAssignmentDO> selectByCounselorUserId(Long counselorUserId, Integer assignmentType) {
        return selectList(new LambdaQueryWrapperX<StudentCounselorAssignmentDO>()
                .eq(StudentCounselorAssignmentDO::getCounselorUserId, counselorUserId)
                .eqIfPresent(StudentCounselorAssignmentDO::getAssignmentType, assignmentType)
                .eq(StudentCounselorAssignmentDO::getStatus, AssignmentStatusEnum.ACTIVE.getStatus()));
    }

    /**
     * 批量查询学生的主责咨询师
     *
     * @param studentProfileIds 学生档案ID列表
     * @return 咨询师分配记录列表
     */
    default List<StudentCounselorAssignmentDO> selectPrimaryCounselorsByStudentIds(List<Long> studentProfileIds) {
        if (studentProfileIds == null || studentProfileIds.isEmpty()) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapperX<StudentCounselorAssignmentDO>()
                .in(StudentCounselorAssignmentDO::getStudentProfileId, studentProfileIds)
                .eq(StudentCounselorAssignmentDO::getAssignmentType, AssignmentTypeEnum.PRIMARY.getType())
                .eq(StudentCounselorAssignmentDO::getStatus, AssignmentStatusEnum.ACTIVE.getStatus()));
    }

    /**
     * 更新学生原有的咨询师分配为失效状态
     *
     * @param studentProfileId 学生档案ID
     * @param assignmentType 分配类型
     * @return 更新的记录数
     */
    int updateToInactive(@Param("studentProfileId") Long studentProfileId, 
                        @Param("assignmentType") Integer assignmentType);

}