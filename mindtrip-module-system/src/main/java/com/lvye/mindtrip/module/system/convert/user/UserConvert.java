package com.lvye.mindtrip.module.system.convert.user;

import com.lvye.mindtrip.framework.common.util.collection.CollectionUtils;
import com.lvye.mindtrip.framework.common.util.collection.MapUtils;
import com.lvye.mindtrip.framework.common.util.object.BeanUtils;
import com.lvye.mindtrip.module.system.controller.admin.dept.vo.dept.DeptSimpleRespVO;
import com.lvye.mindtrip.module.system.controller.admin.dept.vo.post.PostSimpleRespVO;
import com.lvye.mindtrip.module.system.controller.admin.permission.vo.role.RoleSimpleRespVO;
import com.lvye.mindtrip.module.system.controller.admin.user.vo.profile.UserProfileRespVO;
import com.lvye.mindtrip.module.system.controller.admin.user.vo.user.UserRespVO;
import com.lvye.mindtrip.module.system.controller.admin.user.vo.user.UserRoleInfoVO;
import com.lvye.mindtrip.module.system.controller.admin.user.vo.user.UserSimpleRespVO;
import com.lvye.mindtrip.module.system.dal.dataobject.dept.DeptDO;
import com.lvye.mindtrip.module.system.dal.dataobject.dept.PostDO;
import com.lvye.mindtrip.module.system.dal.dataobject.permission.RoleDO;
import com.lvye.mindtrip.module.system.dal.dataobject.user.AdminUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserConvert {

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    default List<UserRespVO> convertList(List<AdminUserDO> list, Map<Long, DeptDO> deptMap) {
        return CollectionUtils.convertList(list, user -> convert(user, deptMap.get(user.getDeptId())));
    }

    default List<UserRespVO> convertList(List<AdminUserDO> list, Map<Long, DeptDO> deptMap, Map<Long, List<RoleDO>> userRolesMap) {
        return CollectionUtils.convertList(list, user -> convert(user, deptMap.get(user.getDeptId()), userRolesMap.get(user.getId())));
    }

    default UserRespVO convert(AdminUserDO user, DeptDO dept) {
        UserRespVO userVO = BeanUtils.toBean(user, UserRespVO.class);
        if (dept != null) {
            userVO.setDeptName(dept.getName());
        }
        return userVO;
    }

    default UserRespVO convert(AdminUserDO user, DeptDO dept, List<RoleDO> roles) {
        UserRespVO userVO = BeanUtils.toBean(user, UserRespVO.class);
        if (dept != null) {
            userVO.setDeptName(dept.getName());
        }
        if (roles != null && !roles.isEmpty()) {
            userVO.setRoleInfo(CollectionUtils.convertList(roles, role -> 
                new UserRoleInfoVO(role.getId(), role.getCode(), role.getName())));
        }
        return userVO;
    }

    default List<UserSimpleRespVO> convertSimpleList(List<AdminUserDO> list, Map<Long, DeptDO> deptMap) {
        return CollectionUtils.convertList(list, user -> {
            UserSimpleRespVO userVO = BeanUtils.toBean(user, UserSimpleRespVO.class);
            MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> userVO.setDeptName(dept.getName()));
            return userVO;
        });
    }

    default UserProfileRespVO convert(AdminUserDO user, List<RoleDO> userRoles,
                                      DeptDO dept, List<PostDO> posts) {
        UserProfileRespVO userVO = BeanUtils.toBean(user, UserProfileRespVO.class);
        userVO.setRoles(BeanUtils.toBean(userRoles, RoleSimpleRespVO.class));
        userVO.setDept(BeanUtils.toBean(dept, DeptSimpleRespVO.class));
        userVO.setPosts(BeanUtils.toBean(posts, PostSimpleRespVO.class));
        return userVO;
    }

}
