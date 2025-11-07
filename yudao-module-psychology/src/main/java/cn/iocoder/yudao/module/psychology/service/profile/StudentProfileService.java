package cn.iocoder.yudao.module.psychology.service.profile;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.*;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentProfileSimpleVO;
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
     * 根据身份证号获取学生档案
     *
     * @param idCard 身份证号
     * @return 学生档案
     */
    StudentProfileDO getStudentProfileByIdCard(String idCard);

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

    /**
     * 更新学生基本信息
     *
     * @param updateReqVO 更新信息
     */
    void updateStudentBasicInfo(@Valid StudentProfileBasicInfoUpdateReqVO updateReqVO);

    /**
     * 检查学生档案信息完善情况
     *
     * @param id 学生档案ID
     * @return 信息完善情况
     */
    StudentProfileCompletenessRespVO checkProfileCompleteness(Long id);

    /**
     * 更新学生风险等级
     * @param studentProfileId
     * @param riskLevel
     */
    void updateStudentRiskLevel(Long studentProfileId, Integer riskLevel);

    /**
     * 更新学生特殊标记
     * @param studentProfileId 学生档案ID
     * @param specialMarks 特殊标记（多选，逗号分隔数字键值）
     */
    void updateStudentSpecialMarks(Long studentProfileId, String specialMarks);

    /**
     * 根据学号和姓名模糊查询学生档案简化列表
     *
     * @param studentNo 学号（支持模糊查询）
     * @param name 姓名（支持模糊查询）
     * @return 学生档案简化列表
     */
    List<StudentProfileSimpleVO> searchSimpleStudentProfilesByStudentNoAndName(String studentNo, String name);

    /**
     * 验证该学生是否是心理老师负责的学生
     * 检查学生班级及其所有父级部门，咨询师关联了任意一个部门即返回true
     *
     * @param studentProfileId 学生档案ID
     * @param counselorUserId 咨询师用户ID
     * @return true-是负责的学生，false-不是负责的学生
     */
    Boolean verifyCounselorStudent(Long studentProfileId, Long counselorUserId);

    /**
     * 年级批量毕业
     * 根据年级部门ID和入学年份查找学生，设置毕业状态并记录时间线
     *
     * @param reqVO 批量毕业请求参数
     * @return 成功处理的学生数量
     */
    Integer batchGraduateStudents(BatchGraduateReqVO reqVO);

    /**
     * 检查毕业年级中风险等级异常的学生
     * 根据年级部门ID和入学年份查找风险等级异常（risk_level != 1）的学生
     *
     * @param reqVO 检查请求参数
     * @return 风险等级异常的学生列表
     */
    List<StudentProfileVO> checkAbnormalGraduatingStudents(CheckAbnormalStudentsReqVO reqVO);

    /**
     * 学生换班（批量）
     * 更新学生的年级和班级信息，并记录时间线
     *
     * @param reqVO 换班请求参数
     * @return 成功处理的学生数量
     */
    Integer changeClass(ChangeClassReqVO reqVO);

    /**
     * 根据监护人手机号获取学生档案
     *
     * @param mobile 监护人手机号
     * @return 学生档案
     */
    StudentProfileDO getStudentProfileByMobile(String mobile);

}