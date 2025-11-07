package com.lvye.mindtrip.module.psychology.convert.profile;

import com.lvye.mindtrip.framework.common.pojo.PageResult;
import com.lvye.mindtrip.framework.dict.core.DictFrameworkUtils;
import com.lvye.mindtrip.module.psychology.controller.admin.profile.vo.StudentProfileRespVO;
import com.lvye.mindtrip.module.psychology.controller.admin.profile.vo.StudentProfileSaveReqVO;
import com.lvye.mindtrip.module.psychology.dal.dataobject.profile.StudentProfileDO;
import com.lvye.mindtrip.module.psychology.enums.DictTypeConstants;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 学生档案 Convert
 */
@Mapper(componentModel = "spring")
public abstract class StudentProfileConvert {

    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "updater", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "transMap", ignore = true)
    public abstract StudentProfileDO convert(StudentProfileSaveReqVO bean);

    @Mapping(target = "specialMarkNames", expression = "java(getSpecialMarkNames(bean.getSpecialMarks()))")
    @Mapping(target = "mobile", ignore = true)
    @Mapping(target = "gradeName", ignore = true)
    @Mapping(target = "className", ignore = true)
    public abstract StudentProfileRespVO convert(StudentProfileDO bean);

    public abstract List<StudentProfileRespVO> convertList(List<StudentProfileDO> list);

    public abstract PageResult<StudentProfileRespVO> convertPage(PageResult<StudentProfileDO> page);

    /**
     * 获取特殊标记名称（从逗号分隔的数字键值转换为显示名称）
     * 通过字典工具类获取标签名称
     */
    protected String getSpecialMarkNames(String specialMarks) {
        if (specialMarks == null || specialMarks.trim().isEmpty()) {
            return "";
        }
        return Arrays.stream(specialMarks.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(value -> DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.STUDENT_SPECIAL_MARK, value))
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", "));
    }

}