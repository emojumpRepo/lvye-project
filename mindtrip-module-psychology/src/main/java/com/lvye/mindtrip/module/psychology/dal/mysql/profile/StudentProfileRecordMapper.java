package com.lvye.mindtrip.module.psychology.dal.mysql.profile;

import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.module.psychology.dal.dataobject.profile.StudentProfileRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-12
 * @Description:学生历史档案表数据库组件
 * @Version: 1.0
 */
@Mapper
public interface StudentProfileRecordMapper extends BaseMapperX<StudentProfileRecordDO> {

}
