package cn.iocoder.yudao.module.psychology.dal.mysql.profile;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.psychology.dal.dataobject.timeline.StudentTimelineDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-18
 * @Description:学生档案时间线数据库组件
 * @Version: 1.0
 */
@Mapper
public interface StudentTimelineMapper extends BaseMapperX<StudentTimelineDO> {

    default List<StudentTimelineDO> selectListByStudentProfileId(String studentProfileId) {
        return selectList(new LambdaQueryWrapperX<StudentTimelineDO>().eq(StudentTimelineDO::getStudentProfileId, studentProfileId));
    }

}
