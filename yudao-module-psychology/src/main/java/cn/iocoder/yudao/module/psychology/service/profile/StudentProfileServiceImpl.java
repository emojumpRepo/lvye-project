package cn.iocoder.yudao.module.psychology.service.profile;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.*;
import cn.iocoder.yudao.module.psychology.convert.profile.StudentProfileConvert;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.profile.StudentProfileMapper;
import cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 学生档案 Service 实现类
 */
@Service
@Validated
@Slf4j
public class StudentProfileServiceImpl implements StudentProfileService {

    @Resource
    private StudentProfileMapper studentProfileMapper;

    @Resource
    private AdminUserApi adminUserApi;

    @Resource
    private DeptApi deptApi;

    @Override
    public Long createStudentProfile(@Valid StudentProfileSaveReqVO createReqVO) {
        // 校验学号唯一性
        validateStudentNoUnique(null, createReqVO.getStudentNo());

        // 插入
        StudentProfileDO studentProfile = BeanUtils.toBean(createReqVO, StudentProfileDO.class);
        studentProfileMapper.insert(studentProfile);
        
        // 返回
        return studentProfile.getId();
    }

    @Override
    public void updateStudentProfile(@Valid StudentProfileSaveReqVO updateReqVO) {
        // 校验存在
        validateStudentProfileExists(updateReqVO.getId());
        // 校验学号唯一性
        validateStudentNoUnique(updateReqVO.getId(), updateReqVO.getStudentNo());

        // 更新
        StudentProfileDO updateObj = BeanUtils.toBean(updateReqVO, StudentProfileDO.class);
        studentProfileMapper.updateById(updateObj);
    }

    @Override
    public void deleteStudentProfile(Long id) {
        // 校验存在
        validateStudentProfileExists(id);
        // 删除
        studentProfileMapper.deleteById(id);
    }

    private void validateStudentProfileExists(Long id) {
        if (studentProfileMapper.selectById(id) == null) {
            throw exception(ErrorCodeConstants.STUDENT_PROFILE_NOT_EXISTS);
        }
    }

    private void validateStudentNoUnique(Long id, String studentNo) {
        StudentProfileDO studentProfile = studentProfileMapper.selectByStudentNo(studentNo);
        if (studentProfile == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的学生档案
        if (id == null) {
            throw exception(ErrorCodeConstants.STUDENT_NO_DUPLICATE);
        }
        if (!Objects.equals(studentProfile.getId(), id)) {
            throw exception(ErrorCodeConstants.STUDENT_NO_DUPLICATE);
        }
    }

    @Override
    public StudentProfileDO getStudentProfile(Long id) {
        return studentProfileMapper.selectById(id);
    }

    @Override
    public PageResult<StudentProfileDO> getStudentProfilePage(StudentProfilePageReqVO pageReqVO) {
        return studentProfileMapper.selectPage(pageReqVO);
    }

    @Override
    public StudentProfileDO getStudentProfileByNo(String studentNo) {
        return studentProfileMapper.selectByStudentNo(studentNo);
    }

    @Override
    public StudentProfileDO getStudentProfileByMemberUserId(Long memberUserId) {
        return studentProfileMapper.selectByMemberUserId(memberUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudentProfileImportRespVO importStudentProfile(StudentProfileImportReqVO importReqVO) {
        StudentProfileImportRespVO respVO = new StudentProfileImportRespVO();
        
        if (importReqVO.getStudentProfiles() == null || importReqVO.getStudentProfiles().isEmpty()) {
            respVO.setSuccessCount(0);
            respVO.setFailureCount(0);
            return respVO;
        }

        int successCount = 0;
        int failureCount = 0;
        
        for (StudentProfileSaveReqVO profile : importReqVO.getStudentProfiles()) {
            try {
                createStudentProfile(profile);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("[importStudentProfile][导入学生档案失败] 学号: {}, 错误: {}", profile.getStudentNo(), e.getMessage());
            }
        }
        
        respVO.setSuccessCount(successCount);
        respVO.setFailureCount(failureCount);
        return respVO;
    }

    @Override
    public void updatePsychologicalStatus(Long id, Integer psychologicalStatus, Integer riskLevel) {
        // 校验存在
        validateStudentProfileExists(id);
        
        // 更新心理状态
        StudentProfileDO updateObj = new StudentProfileDO();
        updateObj.setId(id);
        updateObj.setPsychologicalStatus(psychologicalStatus);
        updateObj.setRiskLevel(riskLevel);
        studentProfileMapper.updateById(updateObj);
    }

    @Override
    public void graduateStudent(Long id) {
        // 校验存在
        StudentProfileDO studentProfile = studentProfileMapper.selectById(id);
        if (studentProfile == null) {
            throw exception(ErrorCodeConstants.STUDENT_PROFILE_NOT_EXISTS);
        }
        
        // 更新毕业状态
        StudentProfileDO updateObj = new StudentProfileDO();
        updateObj.setId(id);
        updateObj.setGraduationStatus(1); // 1-已毕业
        studentProfileMapper.updateById(updateObj);
    }

}