package cn.iocoder.yudao.module.psychology.service.profile;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;


/**
 * 学生档案 Service 接口
 */
public interface StudentProfileService {

    /**
     * 创建学生档案
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStudentProfile(@Valid StudentProfileSaveReqVO createReqVO);

    /**
     * 更新学生档案
     *
     * @param updateReqVO 更新信息
     */
    void updateStudentProfile(@Valid StudentProfileSaveReqVO updateReqVO);

    /**
     * 删除学生档案
     *
     * @param id 编号
     */
    void deleteStudentProfile(Long id);

    /**
     * 获得学生档案
     *
     * @param studentProfileId 学生档案ID
     * @return 学生档案
     */
    StudentProfileVO getStudentProfile(Long studentProfileId);

    /**
     * 获得学生档案分页
     *
     * @param pageReqVO 分页查询
     * @return 学生档案分页
     */
    PageResult<StudentProfileVO> getStudentProfilePage(StudentProfilePageReqVO pageReqVO);

    /**
     * 获得学生档案列表（不分页）
     *
     * @param reqVO 查询条件
     * @return 学生档案列表
     */
    List<StudentProfileVO> getStudentProfileList(StudentProfilePageReqVO reqVO);

    /**
     * 根据学号获取学生档案
     *
     * @param studentNo 学号
     * @return 学生档案
     */
    StudentProfileDO getStudentProfileByNo(String studentNo);

    /**
     * 根据用户ID获取学生档案
     *
     * @param userId 用户编号
     * @return 学生档案
     */
    StudentProfileDO getStudentProfileByUserId(Long userId);

    /**
     * 批量导入学生档案
     * @param studentList
     * @param isUpdateSupport
     * @return
     */
    StudentProfileImportRespVO importStudentProfile(List<StudentImportExcelVO> studentList, boolean isUpdateSupport);

    /**
     * 更新学生心理状态
     *
     * @param id 学生档案编号
     * @param psychologicalStatus 心理状态
     * @param riskLevel 风险等级
     */
    void updatePsychologicalStatus(Long id, Integer psychologicalStatus, Integer riskLevel);

    /**
     * 设置学生毕业状态
     *
     * @param id 学生档案编号
     */
    void graduateStudent(Long id);

    /**
     * 根据用户ID获取学生档案信息详情
     *
     * @param userId 用户编号
     * @return 学生档案
     */
    StudentProfileVO getStudentProfileDetailByUserId(Long userId);

    /**
     * 获得指定班级的学生数组
     *
     * @param classIds 班级数组
     * @return 学生数组
     */
    List<StudentProfileDO> getStudentListByClassIds(Collection<Long> classIds);

}