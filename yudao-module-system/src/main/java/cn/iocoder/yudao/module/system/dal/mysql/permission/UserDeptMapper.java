package cn.iocoder.yudao.module.system.dal.mysql.permission;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserDeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserRoleDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
*@Author: MinGoo
*@CreateTime: 2025-08-09
*@Description: 用戶-部门数据层
*@Version: 1.0
*/
@Mapper
public interface UserDeptMapper extends BaseMapperX<UserDeptDO> {

    default List<UserDeptDO> selectListByUserId(Long userId) {
        return selectList(UserDeptDO::getUserId, userId);
    }

    default void deleteListByUserIdAndDeptIdIds(Long userId, Collection<Long> deptIds) {
        delete(new LambdaQueryWrapper<UserDeptDO>()
                .eq(UserDeptDO::getUserId, userId)
                .in(UserDeptDO::getDeptId, deptIds));
    }

    default void deleteListByUserId(Long userId) {
        delete(new LambdaQueryWrapper<UserDeptDO>().eq(UserDeptDO::getUserId, userId));
    }

    default void deleteListByDeptId(Long deptId) {
        delete(new LambdaQueryWrapper<UserDeptDO>().eq(UserDeptDO::getDeptId, deptId));
    }

    default List<UserDeptDO> selectListByDeptIds(Collection<Long> deptIds) {
        return selectList(UserDeptDO::getDeptId, deptIds);
    }

    default List<UserDeptDO> selectListByDeptId(Long deptId) {
        return selectList(UserDeptDO::getDeptId, deptId);
    }
}
