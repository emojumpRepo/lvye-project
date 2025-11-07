package com.lvye.mindtrip.module.psychology.service.counselor;

import com.lvye.mindtrip.module.psychology.dal.dataobject.counselor.StudentCounselorAssignmentDO;
import com.lvye.mindtrip.module.psychology.dal.mysql.counselor.StudentCounselorAssignmentMapper;
import com.lvye.mindtrip.module.psychology.enums.AssignmentStatusEnum;
import com.lvye.mindtrip.module.psychology.enums.AssignmentTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;

import static com.lvye.mindtrip.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.lvye.mindtrip.module.psychology.enums.ErrorCodeConstants.*;

/**
 * 学生咨询师分配关系 Service 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Service
@Validated
public class StudentCounselorAssignmentServiceImpl implements StudentCounselorAssignmentService {

    @Resource
    private StudentCounselorAssignmentMapper studentCounselorAssignmentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long assignPrimaryCounselor(Long studentProfileId, Long counselorUserId, String assignmentReason) {
        // 1. 将学生原有的主责咨询师设置为失效
        studentCounselorAssignmentMapper.updateToInactive(studentProfileId, AssignmentTypeEnum.PRIMARY.getType());

        // 2. 创建新的分配关系
        StudentCounselorAssignmentDO assignment = StudentCounselorAssignmentDO.builder()
                .studentProfileId(studentProfileId)
                .counselorUserId(counselorUserId)
                .assignmentType(AssignmentTypeEnum.PRIMARY.getType())
                .status(AssignmentStatusEnum.ACTIVE.getStatus())
                .startDate(LocalDate.now())
                .assignmentReason(assignmentReason)
                .build();
        
        studentCounselorAssignmentMapper.insert(assignment);
        log.info("[assignPrimaryCounselor][学生({}) 分配主责咨询师({})]", studentProfileId, counselorUserId);
        
        return assignment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long assignTemporaryCounselor(Long studentProfileId, Long counselorUserId, 
                                         LocalDate startDate, LocalDate endDate, String assignmentReason) {
        // 验证日期
        if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
            throw exception(COUNSELOR_ASSIGNMENT_DATE_INVALID);
        }

        // 创建临时分配关系
        StudentCounselorAssignmentDO assignment = StudentCounselorAssignmentDO.builder()
                .studentProfileId(studentProfileId)
                .counselorUserId(counselorUserId)
                .assignmentType(AssignmentTypeEnum.TEMPORARY.getType())
                .status(AssignmentStatusEnum.ACTIVE.getStatus())
                .startDate(startDate != null ? startDate : LocalDate.now())
                .endDate(endDate)
                .assignmentReason(assignmentReason)
                .build();
        
        studentCounselorAssignmentMapper.insert(assignment);
        log.info("[assignTemporaryCounselor][学生({}) 分配临时咨询师({})]", studentProfileId, counselorUserId);
        
        return assignment.getId();
    }

    @Override
    public StudentCounselorAssignmentDO getPrimaryCounselor(Long studentProfileId) {
        return studentCounselorAssignmentMapper.selectPrimaryCounselor(studentProfileId);
    }

    @Override
    public List<StudentCounselorAssignmentDO> getActiveCounselors(Long studentProfileId) {
        return studentCounselorAssignmentMapper.selectActiveCounselors(studentProfileId);
    }

    @Override
    public List<StudentCounselorAssignmentDO> getCounselorStudents(Long counselorUserId, Integer assignmentType) {
        return studentCounselorAssignmentMapper.selectByCounselorUserId(counselorUserId, assignmentType);
    }

    @Override
    public List<StudentCounselorAssignmentDO> batchGetPrimaryCounselors(List<Long> studentProfileIds) {
        return studentCounselorAssignmentMapper.selectPrimaryCounselorsByStudentIds(studentProfileIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endAssignment(Long assignmentId) {
        StudentCounselorAssignmentDO assignment = studentCounselorAssignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw exception(COUNSELOR_ASSIGNMENT_NOT_EXISTS);
        }

        // 更新状态为失效
        StudentCounselorAssignmentDO updateObj = new StudentCounselorAssignmentDO();
        updateObj.setId(assignmentId);
        updateObj.setStatus(AssignmentStatusEnum.INACTIVE.getStatus());
        updateObj.setEndDate(LocalDate.now());
        
        studentCounselorAssignmentMapper.updateById(updateObj);
        log.info("[endAssignment][结束咨询师分配关系({})]", assignmentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAssignment(Long assignmentId, LocalDate endDate, String assignmentReason) {
        StudentCounselorAssignmentDO assignment = studentCounselorAssignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw exception(COUNSELOR_ASSIGNMENT_NOT_EXISTS);
        }

        // 验证日期
        if (endDate != null && assignment.getStartDate() != null && endDate.isBefore(assignment.getStartDate())) {
            throw exception(COUNSELOR_ASSIGNMENT_DATE_INVALID);
        }

        // 更新分配信息
        StudentCounselorAssignmentDO updateObj = new StudentCounselorAssignmentDO();
        updateObj.setId(assignmentId);
        updateObj.setEndDate(endDate);
        updateObj.setAssignmentReason(assignmentReason);
        
        studentCounselorAssignmentMapper.updateById(updateObj);
        log.info("[updateAssignment][更新咨询师分配关系({})]", assignmentId);
    }

    @Override
    public boolean hasPrimaryCounselor(Long studentProfileId) {
        StudentCounselorAssignmentDO primaryCounselor = getPrimaryCounselor(studentProfileId);
        return primaryCounselor != null;
    }

    @Override
    public Long getCounselorUserIdOrDefault(Long studentProfileId, Long defaultCounselorUserId) {
        StudentCounselorAssignmentDO primaryCounselor = getPrimaryCounselor(studentProfileId);
        
        if (primaryCounselor != null) {
            return primaryCounselor.getCounselorUserId();
        }
        
        // 如果没有主责咨询师，返回默认咨询师ID
        log.debug("[getCounselorUserIdOrDefault][学生({}) 没有主责咨询师，使用默认咨询师({})]", 
                  studentProfileId, defaultCounselorUserId);
        return defaultCounselorUserId;
    }

}