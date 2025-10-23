package cn.iocoder.yudao.module.system.service.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import cn.iocoder.yudao.framework.common.biz.system.permission.dto.DeptDataPermissionRespDTO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.*;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMenuMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserDeptMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper;
import cn.iocoder.yudao.module.system.dal.redis.RedisKeyConstants;
import cn.iocoder.yudao.module.system.enums.permission.DataScopeEnum;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.function.Supplier;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;

/**
 * 权限 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    @Resource
    private RoleMenuMapper roleMenuMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RoleService roleService;
    @Resource
    private MenuService menuService;
    @Resource
    private DeptService deptService;
    @Resource
    private AdminUserService userService;

    @Resource
    private UserDeptMapper userDeptMapper;

    @Override
    public boolean hasAnyPermissions(Long userId, String... permissions) {
        // 如果为空，说明已经有权限
        if (ArrayUtil.isEmpty(permissions)) {
            return true;
        }

        // 获得当前登录的角色。如果为空，说明没有权限
        List<RoleDO> roles = getEnableUserRoleListByUserIdFromCache(userId);
        if (CollUtil.isEmpty(roles)) {
            return false;
        }

        // 情况一：遍历判断每个权限，如果有一满足，说明有权限
        for (String permission : permissions) {
            if (hasAnyPermission(roles, permission)) {
                return true;
            }
        }

        // 情况二：如果是超管，也说明有权限
        return roleService.hasAnySuperAdmin(convertSet(roles, RoleDO::getId));
    }

    /**
     * 判断指定角色，是否拥有该 permission 权限
     *
     * @param roles 指定角色数组
     * @param permission 权限标识
     * @return 是否拥有
     */
    private boolean hasAnyPermission(List<RoleDO> roles, String permission) {
        List<Long> menuIds = menuService.getMenuIdListByPermissionFromCache(permission);
        // 采用严格模式，如果权限找不到对应的 Menu 的话，也认为没有权限
        if (CollUtil.isEmpty(menuIds)) {
            return false;
        }

        // 判断是否有权限
        Set<Long> roleIds = convertSet(roles, RoleDO::getId);
        for (Long menuId : menuIds) {
            // 获得拥有该菜单的角色编号集合
            Set<Long> menuRoleIds = getSelf().getMenuRoleIdListByMenuIdFromCache(menuId);
            // 如果有交集，说明有权限
            if (CollUtil.containsAny(menuRoleIds, roleIds)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAnyRoles(Long userId, String... roles) {
        // 如果为空，说明已经有权限
        if (ArrayUtil.isEmpty(roles)) {
            return true;
        }

        // 获得当前登录的角色。如果为空，说明没有权限
        List<RoleDO> roleList = getEnableUserRoleListByUserIdFromCache(userId);
        if (CollUtil.isEmpty(roleList)) {
            return false;
        }

        // 判断是否有角色
        Set<String> userRoles = convertSet(roleList, RoleDO::getCode);
        return CollUtil.containsAny(userRoles, Sets.newHashSet(roles));
    }

    // ========== 角色-菜单的相关方法  ==========

    @Override
    @DSTransactional // 多数据源，使用 @DSTransactional 保证本地事务，以及数据源的切换
    @Caching(evict = {
            @CacheEvict(value = RedisKeyConstants.MENU_ROLE_ID_LIST,
            allEntries = true),
            @CacheEvict(value = RedisKeyConstants.PERMISSION_MENU_ID_LIST,
            allEntries = true) // allEntries 清空所有缓存，主要一次更新涉及到的 menuIds 较多，反倒批量会更快
    })
    public void assignRoleMenu(Long roleId, Set<Long> menuIds) {
        // 获得角色拥有菜单编号
        Set<Long> dbMenuIds = convertSet(roleMenuMapper.selectListByRoleId(roleId), RoleMenuDO::getMenuId);
        // 计算新增和删除的菜单编号
        Set<Long> menuIdList = CollUtil.emptyIfNull(menuIds);
        Collection<Long> createMenuIds = CollUtil.subtract(menuIdList, dbMenuIds);
        Collection<Long> deleteMenuIds = CollUtil.subtract(dbMenuIds, menuIdList);
        // 执行新增和删除。对于已经授权的菜单，不用做任何处理
        if (CollUtil.isNotEmpty(createMenuIds)) {
            roleMenuMapper.insertBatch(CollectionUtils.convertList(createMenuIds, menuId -> {
                RoleMenuDO entity = new RoleMenuDO();
                entity.setRoleId(roleId);
                entity.setMenuId(menuId);
                return entity;
            }));
        }
        if (CollUtil.isNotEmpty(deleteMenuIds)) {
            roleMenuMapper.deleteListByRoleIdAndMenuIds(roleId, deleteMenuIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = RedisKeyConstants.MENU_ROLE_ID_LIST,
                    allEntries = true), // allEntries 清空所有缓存，此处无法方便获得 roleId 对应的 menu 缓存们
            @CacheEvict(value = RedisKeyConstants.USER_ROLE_ID_LIST,
                    allEntries = true) // allEntries 清空所有缓存，此处无法方便获得 roleId 对应的 user 缓存们
    })
    public void processRoleDeleted(Long roleId) {
        // 标记删除 UserRole
        userRoleMapper.deleteListByRoleId(roleId);
        // 标记删除 RoleMenu
        roleMenuMapper.deleteListByRoleId(roleId);
    }

    @Override
    @CacheEvict(value = RedisKeyConstants.MENU_ROLE_ID_LIST, key = "#menuId")
    public void processMenuDeleted(Long menuId) {
        roleMenuMapper.deleteListByMenuId(menuId);
    }

    @Override
    public Set<Long> getRoleMenuListByRoleId(Collection<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptySet();
        }

        // 如果是管理员的情况下，获取全部菜单编号
        if (roleService.hasAnySuperAdmin(roleIds)) {
            return convertSet(menuService.getMenuList(), MenuDO::getId);
        }
        // 如果是非管理员的情况下，获得拥有的菜单编号
        return convertSet(roleMenuMapper.selectListByRoleId(roleIds), RoleMenuDO::getMenuId);
    }

    @Override
    @Cacheable(value = RedisKeyConstants.MENU_ROLE_ID_LIST, key = "#menuId")
    public Set<Long> getMenuRoleIdListByMenuIdFromCache(Long menuId) {
        return convertSet(roleMenuMapper.selectListByMenuId(menuId), RoleMenuDO::getRoleId);
    }

    // ========== 用户-角色的相关方法  ==========

    @Override
    @DSTransactional // 多数据源，使用 @DSTransactional 保证本地事务，以及数据源的切换
    @CacheEvict(value = RedisKeyConstants.USER_ROLE_ID_LIST, key = "#userId")
    public void assignUserRole(Long userId, Set<Long> roleIds) {
        // 获得角色拥有角色编号
        Set<Long> dbRoleIds = convertSet(userRoleMapper.selectListByUserId(userId),
                UserRoleDO::getRoleId);
        // 计算新增和删除的角色编号
        Set<Long> roleIdList = CollUtil.emptyIfNull(roleIds);
        Collection<Long> createRoleIds = CollUtil.subtract(roleIdList, dbRoleIds);
        Collection<Long> deleteMenuIds = CollUtil.subtract(dbRoleIds, roleIdList);
        
        // 特殊处理：处理默认心理咨询师和心理老师角色
        createRoleIds = handleSpecialRoles(userId, createRoleIds);
        
        // 执行新增和删除。对于已经授权的角色，不用做任何处理
        if (!CollectionUtil.isEmpty(createRoleIds)) {
            userRoleMapper.insertBatch(CollectionUtils.convertList(createRoleIds, roleId -> {
                UserRoleDO entity = new UserRoleDO();
                entity.setUserId(userId);
                entity.setRoleId(roleId);
                return entity;
            }));
        }
        if (!CollectionUtil.isEmpty(deleteMenuIds)) {
            userRoleMapper.deleteListByUserIdAndRoleIdIds(userId, deleteMenuIds);
        }
    }

    /**
     * 处理特殊角色分配逻辑
     * 1. 默认心理老师角色：互斥，只能有一个用户拥有
     * 2. 心理老师角色：第一个添加该角色的用户自动获得默认心理老师角色
     * 3. 年级管理员角色：如果没有心理老师角色的用户，则自动获得默认心理老师角色
     * 4. 系统管理员角色：如果没有用户拥有默认心理老师角色，则自动获得默认心理老师角色
     *
     * @param userId 用户ID
     * @param createRoleIds 待添加的角色ID集合
     * @return 处理后的角色ID集合
     */
    private Collection<Long> handleSpecialRoles(Long userId, Collection<Long> createRoleIds) {
        if (CollectionUtil.isEmpty(createRoleIds)) {
            return createRoleIds;
        }
        
        // 转换为可变集合
        Set<Long> roleIdSet = new HashSet<>(createRoleIds);
        
        // 获取相关角色对象
        RoleDO defaultPsychologyTeacherRole = roleMapper.selectByCode(RoleCodeEnum.DEFAULT_PSYCHOLOGY_TEACHER.getCode());
        RoleDO psychologyTeacherRole = roleMapper.selectByCode(RoleCodeEnum.PSYCHOLOGY_TEACHER.getCode());
        RoleDO gradeTeacherRole = roleMapper.selectByCode(RoleCodeEnum.GRADE_TEACHER.getCode());
        RoleDO sysAdminRole = roleMapper.selectByCode(RoleCodeEnum.SYS_ADMIN.getCode());
        
        // 1. 处理默认心理老师角色：移除其他用户的该角色（互斥逻辑）
        if (defaultPsychologyTeacherRole != null && roleIdSet.contains(defaultPsychologyTeacherRole.getId())) {
            removeRoleFromOtherUsers(userId, defaultPsychologyTeacherRole.getId(), "直接分配");
        }
        
        // 2. 处理心理老师角色：第一个添加的用户同时获得默认心理老师角色
        if (psychologyTeacherRole != null && roleIdSet.contains(psychologyTeacherRole.getId())) {
            List<UserRoleDO> existingPsychologyTeachers = userRoleMapper.selectListByRoleId(psychologyTeacherRole.getId());
            if (CollectionUtil.isEmpty(existingPsychologyTeachers) && defaultPsychologyTeacherRole != null) {
                roleIdSet.add(defaultPsychologyTeacherRole.getId());
                log.info("[handleSpecialRoles][用户({})是第一个心理老师，自动添加默认心理老师角色]", userId);
                removeRoleFromOtherUsers(userId, defaultPsychologyTeacherRole.getId(), "心理老师自动获得");
            }
        }
        
        // 3. 处理年级管理员角色：如果没有心理老师用户，则自动获得默认心理老师角色
        if (gradeTeacherRole != null && roleIdSet.contains(gradeTeacherRole.getId())) {
            List<UserRoleDO> existingPsychologyTeachers = psychologyTeacherRole != null 
                    ? userRoleMapper.selectListByRoleId(psychologyTeacherRole.getId()) 
                    : Collections.emptyList();
            if (CollectionUtil.isEmpty(existingPsychologyTeachers) && defaultPsychologyTeacherRole != null) {
                roleIdSet.add(defaultPsychologyTeacherRole.getId());
                log.info("[handleSpecialRoles][用户({})是年级管理员且没有心理老师用户，自动添加默认心理老师角色]", userId);
                removeRoleFromOtherUsers(userId, defaultPsychologyTeacherRole.getId(), "年级管理员自动获得");
            }
        }
        
        // 4. 处理系统管理员角色：如果没有用户拥有默认心理老师角色，则自动获得默认心理老师角色
        if (sysAdminRole != null && roleIdSet.contains(sysAdminRole.getId())) {
            List<UserRoleDO> existingDefaultPsychologyTeachers = defaultPsychologyTeacherRole != null 
                    ? userRoleMapper.selectListByRoleId(defaultPsychologyTeacherRole.getId()) 
                    : Collections.emptyList();
            if (CollectionUtil.isEmpty(existingDefaultPsychologyTeachers) && defaultPsychologyTeacherRole != null) {
                roleIdSet.add(defaultPsychologyTeacherRole.getId());
                log.info("[handleSpecialRoles][用户({})是系统管理员且没有用户拥有默认心理老师角色，自动添加默认心理老师角色]", userId);
                removeRoleFromOtherUsers(userId, defaultPsychologyTeacherRole.getId(), "系统管理员自动获得");
            }
        }
        
        return roleIdSet;
    }

    /**
     * 移除其他用户的指定角色（确保角色互斥）
     *
     * @param currentUserId 当前用户ID（排除该用户）
     * @param roleId 要移除的角色ID
     * @param reason 移除原因（用于日志）
     */
    private void removeRoleFromOtherUsers(Long currentUserId, Long roleId, String reason) {
        List<UserRoleDO> existingUserRoles = userRoleMapper.selectListByRoleId(roleId);
        for (UserRoleDO userRole : existingUserRoles) {
            if (!userRole.getUserId().equals(currentUserId)) {
                userRoleMapper.deleteListByUserIdAndRoleIdIds(userRole.getUserId(), 
                        Collections.singleton(roleId));
                log.info("[handleSpecialRoles][移除用户({})的默认心理老师角色（原因：{}）]", 
                        userRole.getUserId(), reason);
            }
        }
    }

    @Override
    @CacheEvict(value = RedisKeyConstants.USER_ROLE_ID_LIST, key = "#userId")
    public void processUserDeleted(Long userId) {
        userRoleMapper.deleteListByUserId(userId);
    }

    @Override
    public Set<Long> getUserRoleIdListByUserId(Long userId) {
        return convertSet(userRoleMapper.selectListByUserId(userId), UserRoleDO::getRoleId);
    }

    @Override
    @Cacheable(value = RedisKeyConstants.USER_ROLE_ID_LIST, key = "#userId")
    public Set<Long> getUserRoleIdListByUserIdFromCache(Long userId) {
        return getUserRoleIdListByUserId(userId);
    }

    @Override
    public Set<Long> getUserRoleIdListByRoleId(Collection<Long> roleIds) {
        return convertSet(userRoleMapper.selectListByRoleIds(roleIds), UserRoleDO::getUserId);
    }

    /**
     * 获得用户拥有的角色，并且这些角色是开启状态的
     *
     * @param userId 用户编号
     * @return 用户拥有的角色
     */
    @VisibleForTesting
    List<RoleDO> getEnableUserRoleListByUserIdFromCache(Long userId) {
        // 获得用户拥有的角色编号
        Set<Long> roleIds = getSelf().getUserRoleIdListByUserIdFromCache(userId);
        // 获得角色数组，并移除被禁用的
        List<RoleDO> roles = roleService.getRoleListFromCache(roleIds);
        roles.removeIf(role -> !CommonStatusEnum.ENABLE.getStatus().equals(role.getStatus()));
        return roles;
    }

    // ========== 用户-部门的相关方法  ==========

    @Override
    public void assignRoleDataScope(Long roleId, Integer dataScope, Set<Long> dataScopeDeptIds) {
        roleService.updateRoleDataScope(roleId, dataScope, dataScopeDeptIds);
    }

    @Override
    @DataPermission(enable = false) // 关闭数据权限，不然就会出现递归获取数据权限的问题
    public DeptDataPermissionRespDTO getDeptDataPermission(Long userId) {
        // 获得用户的角色
        List<RoleDO> roles = getEnableUserRoleListByUserIdFromCache(userId);

        // 如果角色为空，则只能查看自己
        DeptDataPermissionRespDTO result = new DeptDataPermissionRespDTO();
        if (CollUtil.isEmpty(roles)) {
            result.setSelf(true);
            return result;
        }

        // 【改进】获得用户的所有部门编号（主部门 + 多部门），通过 Guava 的 Suppliers 惰性求值
        Supplier<Set<Long>> userDeptIds = Suppliers.memoize(() -> {
            Set<Long> deptIds = new HashSet<>();
            // 1. 添加主部门（需要防止用户不存在的情况）
            AdminUserDO user = userService.getUser(userId);
            if (user != null) {
                CollectionUtils.addIfNotNull(deptIds, user.getDeptId());
            }
            // 2. 添加用户关联的多部门（来自 system_user_dept 表）
            Set<Long> userMultiDeptIds = getUserDeptIdListByUserId(userId);
            if (CollUtil.isNotEmpty(userMultiDeptIds)) {
                deptIds.addAll(userMultiDeptIds);
            }
            return deptIds;
        });
        
        // 遍历每个角色，计算
        for (RoleDO role : roles) {
            // 为空时，跳过
            if (role.getDataScope() == null) {
                continue;
            }
            // 情况一，ALL
            if (Objects.equals(role.getDataScope(), DataScopeEnum.ALL.getScope())) {
                result.setAll(true);
                continue;
            }
            // 情况二，DEPT_CUSTOM
            if (Objects.equals(role.getDataScope(), DataScopeEnum.DEPT_CUSTOM.getScope())) {
                CollUtil.addAll(result.getDeptIds(), role.getDataScopeDeptIds());
                // 【改进】自定义可见部门时，保证可以看到自己所在的所有部门（主部门+多部门）
                // 例如说，登录时，基于 t_user 的 username 查询会可能被 dept_id 过滤掉
                CollUtil.addAll(result.getDeptIds(), userDeptIds.get());
                continue;
            }
            // 情况三，DEPT_ONLY
            if (Objects.equals(role.getDataScope(), DataScopeEnum.DEPT_ONLY.getScope())) {
                // 【改进】添加用户的所有部门（主部门+多部门）
                CollUtil.addAll(result.getDeptIds(), userDeptIds.get());
                continue;
            }
            // 情况四，DEPT_DEPT_AND_CHILD
            if (Objects.equals(role.getDataScope(), DataScopeEnum.DEPT_AND_CHILD.getScope())) {
                // 【改进】遍历用户的所有部门，添加每个部门及其子部门
                Set<Long> allUserDeptIds = userDeptIds.get();
                for (Long deptId : allUserDeptIds) {
                    // 添加该部门的所有子部门
                    CollUtil.addAll(result.getDeptIds(), deptService.getChildDeptIdListFromCache(deptId));
                    // 添加该部门本身
                    CollectionUtils.addIfNotNull(result.getDeptIds(), deptId);
                }
                continue;
            }
            // 情况五，SELF
            if (Objects.equals(role.getDataScope(), DataScopeEnum.SELF.getScope())) {
                result.setSelf(true);
                continue;
            }
            // 未知情况，error log 即可
            log.error("[getDeptDataPermission][LoginUser({}) role({}) 无法处理]", userId, toJsonString(result));
        }
        return result;
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private PermissionServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

    @Override
    @DSTransactional // 多数据源，使用 @DSTransactional 保证本地事务，以及数据源的切换
    @CacheEvict(value = RedisKeyConstants.USER_ROLE_DEPT_ID_LIST, key = "#userId")
    public void assignUserRoleAndDept(Long userId, Set<Long> deptIds, Set<Long> roleIds) {
        // 校验用户是否存在
        userService.validateUserList(Collections.singleton(userId));
        // 校验部门的有效性
        deptService.validateDeptList(deptIds);
        // 校验角色的有效性
        roleService.validateRoleList(roleIds);

        // 获得角色拥有部门编号
        Set<Long> dbDeptIds = convertSet(userDeptMapper.selectListByUserId(userId),
                UserDeptDO::getDeptId);
        // 计算新增和删除的角色编号
        Set<Long> deptIdList = CollUtil.emptyIfNull(deptIds);
        Collection<Long> createDeptIds = CollUtil.subtract(deptIdList, dbDeptIds);
        Collection<Long> deleteDeptIds = CollUtil.subtract(dbDeptIds, deptIdList);
        // 执行新增和删除。对于已经授权的角色，不用做任何处理
        if (!CollectionUtil.isEmpty(createDeptIds)) {
            userDeptMapper.insertBatch(CollectionUtils.convertList(createDeptIds, deptId -> {
                UserDeptDO entity = new UserDeptDO();
                entity.setUserId(userId);
                entity.setDeptId(deptId);
                return entity;
            }));
        }
        if (!CollectionUtil.isEmpty(deleteDeptIds)) {
            userDeptMapper.deleteListByUserIdAndDeptIdIds(userId, deleteDeptIds);
        }
        // 获得角色拥有角色编号
        Set<Long> dbRoleIds = convertSet(userRoleMapper.selectListByUserId(userId),
                UserRoleDO::getRoleId);
        // 计算新增和删除的角色编号
        Set<Long> roleIdList = CollUtil.emptyIfNull(roleIds);
        Collection<Long> createRoleIds = CollUtil.subtract(roleIdList, dbRoleIds);
        Collection<Long> deleteMenuIds = CollUtil.subtract(dbRoleIds, roleIdList);
        // 执行新增和删除。对于已经授权的角色，不用做任何处理
        if (!CollectionUtil.isEmpty(createRoleIds)) {
            userRoleMapper.insertBatch(CollectionUtils.convertList(createRoleIds, roleId -> {
                UserRoleDO entity = new UserRoleDO();
                entity.setUserId(userId);
                entity.setRoleId(roleId);
                return entity;
            }));
        }
        if (!CollectionUtil.isEmpty(deleteMenuIds)) {
            userRoleMapper.deleteListByUserIdAndRoleIdIds(userId, deleteMenuIds);
        }
    }

    @Override
    public Set<Long> getUserDeptIdListByUserId(Long userId) {
        return convertSet(userDeptMapper.selectListByUserId(userId), UserDeptDO::getDeptId);
    }

    @Override
    @Cacheable(value = RedisKeyConstants.USER_ROLE_DEPT_ID_LIST, key = "#userId")
    public Set<Long> getUserDeptIdListByDeptIdFromCache(Long userId) {
        return getUserDeptIdListByUserId(userId);
    }

}
