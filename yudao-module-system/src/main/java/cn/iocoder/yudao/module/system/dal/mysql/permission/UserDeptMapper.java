package cn.iocoder.yudao.module.system.dal.mysql.permission;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserDeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserRoleDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
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

    /**
     * 查询指定用户是否关联了给定部门列表中的任意一个部门
     *
     * @param userId 用户ID
     * @param deptIds 部门ID列表
     * @return 用户部门关联列表（如果有关联则返回记录，否则返回空列表）
     */
    default List<UserDeptDO> selectByUserIdAndDeptIds(Long userId, Collection<Long> deptIds) {
        if (userId == null || deptIds == null || deptIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<UserDeptDO>()
                .eq(UserDeptDO::getUserId, userId)
                .in(UserDeptDO::getDeptId, deptIds));
    }
}
