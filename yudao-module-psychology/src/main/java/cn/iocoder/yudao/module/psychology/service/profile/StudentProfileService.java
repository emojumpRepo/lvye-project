package cn.iocoder.yudao.module.psychology.service.profile;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;

import javax.validation.Valid;

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
     * @param id 编号
     * @return 学生档案
     */
    StudentProfileDO getStudentProfile(Long id);

    /**
     * 获得学生档案分页
     *
     * @param pageReqVO 分页查询
     * @return 学生档案分页
     */
    PageResult<StudentProfileDO> getStudentProfilePage(StudentProfilePageReqVO pageReqVO);

    /**
     * 根据学号获取学生档案
     *
     * @param studentNo 学号
     * @return 学生档案
     */
    StudentProfileDO getStudentProfileByNo(String studentNo);

    /**
     * 根据会员用户编号获取学生档案
     *
     * @param memberUserId 会员用户编号
     * @return 学生档案
     */
    StudentProfileDO getStudentProfileByMemberUserId(Long memberUserId);

    /**
     * 批量导入学生档案
     *
     * @param importReqVO 导入请求
     * @return 导入结果
     */
    StudentProfileImportRespVO importStudentProfile(StudentProfileImportReqVO importReqVO);

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

}