package cn.iocoder.yudao.module.system.dal.mysql.permission;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.api.user.dto.QuickReportHandleUserVO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserRoleDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapperX<UserRoleDO> {

    default List<UserRoleDO> selectListByUserId(Long userId) {
        return selectList(UserRoleDO::getUserId, userId);
    }

    default void deleteListByUserIdAndRoleIdIds(Long userId, Collection<Long> roleIds) {
        delete(new LambdaQueryWrapper<UserRoleDO>()
                .eq(UserRoleDO::getUserId, userId)
                .in(UserRoleDO::getRoleId, roleIds));
    }

    default void deleteListByUserId(Long userId) {
        delete(new LambdaQueryWrapper<UserRoleDO>().eq(UserRoleDO::getUserId, userId));
    }

    default void deleteListByRoleId(Long roleId) {
        delete(new LambdaQueryWrapper<UserRoleDO>().eq(UserRoleDO::getRoleId, roleId));
    }

    default List<UserRoleDO> selectListByRoleIds(Collection<Long> roleIds) {
        return selectList(UserRoleDO::getRoleId, roleIds);
    }

    default UserRoleDO selectByUserIdAndRoleId(Long userId, Long roleId) {
        return selectOne(new LambdaQueryWrapper<UserRoleDO>().eq(UserRoleDO::getRoleId, roleId).eq(UserRoleDO::getUserId, userId));
    }

    default List<UserRoleDO> selectListByRoleId(Long roleId) {
        return selectList(UserRoleDO::getRoleId, roleId);
    }

    List<QuickReportHandleUserVO> selectUserListByRoleIdAndDeptId(@Param("roleId") Long roleId, @Param("deptId") Long deptId);

    /**
     * 查询非学生角色的用户及其角色信息
     *
     * @return 用户及角色信息列表
     */
    List<cn.iocoder.yudao.module.system.controller.admin.user.vo.user.UserWithRoleInfoRespVO> selectNonStudentUsersWithRoles();

}
