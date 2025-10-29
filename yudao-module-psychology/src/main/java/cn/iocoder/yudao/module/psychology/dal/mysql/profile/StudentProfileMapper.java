package cn.iocoder.yudao.module.psychology.dal.mysql.profile;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentClassVO;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentProfilePageReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentProfileVO;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentProfileSimpleVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface StudentProfileMapper extends BaseMapperX<StudentProfileDO> {

    default PageResult<StudentProfileDO> selectPage(StudentProfilePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StudentProfileDO>()
                .likeIfPresent(StudentProfileDO::getStudentNo, reqVO.getStudentNo())
                .likeIfPresent(StudentProfileDO::getName, reqVO.getName())
                .eqIfPresent(StudentProfileDO::getGradeDeptId, reqVO.getGradeDeptId())
                .eqIfPresent(StudentProfileDO::getClassDeptId, reqVO.getClassDeptId())
                .eqIfPresent(StudentProfileDO::getGraduationStatus, reqVO.getGraduationStatus())
                .eqIfPresent(StudentProfileDO::getPsychologicalStatus, reqVO.getPsychologicalStatus())
                .eqIfPresent(StudentProfileDO::getRiskLevel, reqVO.getRiskLevel())
                .orderByDesc(StudentProfileDO::getId));
    }

    default StudentProfileDO selectByStudentNo(String studentNo) {
        return selectOne(StudentProfileDO::getStudentNo, studentNo);
    }

    default StudentProfileDO selectByUserId(Long userId) {
        return selectOne(StudentProfileDO::getUserId, userId);
    }

    default StudentProfileDO selectByIdCard(String idCard) {
        return selectOne(StudentProfileDO::getIdCard, idCard);
    }

    default int updateRiskLevel(Long id, Integer riskLevel) {
        return update(new LambdaUpdateWrapper<StudentProfileDO>()
                .eq(StudentProfileDO::getId, id)
                .set(StudentProfileDO::getRiskLevel, riskLevel));
    }

    default int updateSpecialMarks(Long id, String specialMarks) {
        return update(new LambdaUpdateWrapper<StudentProfileDO>()
                .eq(StudentProfileDO::getId, id)
                .set(StudentProfileDO::getSpecialMarks, specialMarks));
    }

    IPage<StudentProfileVO> selectPageList(IPage<StudentProfileVO> page,
                                           @Param("pageReqVO") StudentProfilePageReqVO pageReqVO,
                                           @Param("deptIds") Collection<Long> deptIds,
                                           @Param("selfUserId") Long selfUserId);

    StudentProfileVO selectInfoByStudentProfileId(Long studentProfileId);

    StudentProfileVO selectInfoByUserId(Long userId);

    default List<StudentProfileDO> selectListByClassIds(Collection<Long> deptIds) {
        return selectList(StudentProfileDO::getClassDeptId, deptIds);
    }

    List<StudentClassVO> selectClassStudentCount();

    List<StudentClassVO> selectGradeStudentCount();

    List<StudentProfileSimpleVO> searchSimpleByStudentNoAndName(@Param("studentNo") String studentNo,
                                                               @Param("name") String name,
                                                               @Param("deptIds") Collection<Long> deptIds,
                                                               @Param("selfUserId") Long selfUserId);

    default List<StudentProfileDO> selectGraduatedStudents() {
        return selectList(StudentProfileDO::getGraduationStatus, 1);
    }

    /**
     * 根据年级部门ID和入学年份查询学生列表（用于批量毕业）
     *
     * @param gradeDeptId 年级部门ID
     * @param enrollmentYear 入学年份
     * @param excludeIds 需要排除的学生ID列表
     * @return 学生列表
     */
    List<StudentProfileDO> selectByGradeDeptIdAndEnrollmentYear(@Param("gradeDeptId") Long gradeDeptId,
                                                                 @Param("enrollmentYear") Integer enrollmentYear,
                                                                 @Param("excludeIds") Collection<Long> excludeIds);

    /**
     * 根据年级部门ID和入学年份查询心理状态异常的学生列表
     *
     * @param gradeDeptId 年级部门ID
     * @param enrollmentYear 入学年份
     * @return 心理状态异常的学生列表（包含年级、班级等关联信息）
     */
    List<StudentProfileVO> selectAbnormalStudentsByGradeDeptIdAndEnrollmentYear(@Param("gradeDeptId") Long gradeDeptId,
                                                                                  @Param("enrollmentYear") Integer enrollmentYear);

}



