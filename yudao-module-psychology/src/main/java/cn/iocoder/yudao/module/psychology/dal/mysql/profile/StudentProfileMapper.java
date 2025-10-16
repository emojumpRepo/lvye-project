package cn.iocoder.yudao.module.psychology.dal.mysql.profile;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentClassVO;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentProfilePageReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentProfileVO;
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

}



