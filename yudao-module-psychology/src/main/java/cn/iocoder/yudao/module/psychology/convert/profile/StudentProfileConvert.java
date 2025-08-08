package cn.iocoder.yudao.module.psychology.convert.profile;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentProfileRespVO;
import cn.iocoder.yudao.module.psychology.controller.admin.profile.vo.StudentProfileSaveReqVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 学生档案 Convert
 */
@Mapper
public interface StudentProfileConvert {

    StudentProfileConvert INSTANCE = Mappers.getMapper(StudentProfileConvert.class);

    StudentProfileDO convert(StudentProfileSaveReqVO bean);

    StudentProfileRespVO convert(StudentProfileDO bean);

    List<StudentProfileRespVO> convertList(List<StudentProfileDO> list);

    PageResult<StudentProfileRespVO> convertPage(PageResult<StudentProfileDO> page);

}