package com.lvye.mindtrip.module.psychology.controller.app;

import cn.hutool.core.collection.CollUtil;
import com.lvye.mindtrip.framework.apilog.core.annotation.ApiAccessLog;
import com.lvye.mindtrip.framework.common.enums.CommonStatusEnum;
import com.lvye.mindtrip.framework.common.enums.UserTypeEnum;
import com.lvye.mindtrip.framework.common.pojo.CommonResult;
import com.lvye.mindtrip.framework.common.util.object.BeanUtils;
import com.lvye.mindtrip.module.system.controller.admin.auth.vo.AuthPermissionInfoRespVO;
import com.lvye.mindtrip.module.system.controller.admin.dict.vo.data.DictDataSimpleRespVO;
import com.lvye.mindtrip.module.system.convert.auth.AuthConvert;
import com.lvye.mindtrip.module.system.dal.dataobject.dict.DictDataDO;
import com.lvye.mindtrip.module.system.dal.dataobject.permission.MenuDO;
import com.lvye.mindtrip.module.system.dal.dataobject.permission.RoleDO;
import com.lvye.mindtrip.module.system.dal.dataobject.user.AdminUserDO;
import com.lvye.mindtrip.module.system.service.dict.DictDataService;
import com.lvye.mindtrip.module.system.service.notify.NotifyMessageService;
import com.lvye.mindtrip.module.system.service.permission.MenuService;
import com.lvye.mindtrip.module.system.service.permission.PermissionService;
import com.lvye.mindtrip.module.system.service.permission.RoleService;
import com.lvye.mindtrip.module.system.service.user.AdminUserService;
import com.lvye.mindtrip.module.system.service.dept.DeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.lvye.mindtrip.framework.common.pojo.CommonResult.success;
import static com.lvye.mindtrip.framework.common.util.collection.CollectionUtils.convertSet;
import static com.lvye.mindtrip.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-20
 * @Description:通用
 * @Version: 1.0
 */
@Tag(name = "学生/家长端 - 通用功能")
@RestController
@Validated
public class CommonController {

    @Resource
    private AdminUserService userService;
    @Resource
    private PermissionService permissionService;
    @Resource
    private RoleService roleService;
    @Resource
    private MenuService menuService;
    @Resource
    private DictDataService dictDataService;
    @Resource
    private NotifyMessageService notifyMessageService;
    @Resource
    private DeptService deptService;
    @Resource
    private com.lvye.mindtrip.module.system.service.tenant.TenantService tenantService;

    @GetMapping("/system/auth/get-permission-info")
    @Operation(summary = "获取登录用户的权限信息")
    public CommonResult<AuthPermissionInfoRespVO> getPermissionInfo() {
        // 1.1 获得用户信息
        AdminUserDO user = userService.getUser(getLoginUserId());
        if (user == null) {
            return success(null);
        }

        // 1.2 获得部门名称
        String deptName = null;
        if (user.getDeptId() != null) {
            try {
                var dept = deptService.getDept(user.getDeptId());
                deptName = dept != null ? dept.getName() : null;
            } catch (Exception e) {
                // 如果获取部门信息失败，继续执行，部门名称为null
                deptName = null;
            }
        }

        // 1.3 获得租户名称
        String tenantName = null;
        if (user.getTenantId() != null) {
            try {
                com.lvye.mindtrip.module.system.dal.dataobject.tenant.TenantDO tenant = tenantService.getTenant(user.getTenantId());
                tenantName = tenant != null ? tenant.getName() : null;
            } catch (Exception e) {
                // 如果获取租户信息失败，继续执行，租户名称为null
                tenantName = null;
            }
        }

        // 1.4 获得角色列表
        Set<Long> roleIds = permissionService.getUserRoleIdListByUserId(getLoginUserId());
        if (CollUtil.isEmpty(roleIds)) {
            return success(AuthConvert.INSTANCE.convert(user, Collections.emptyList(), Collections.emptyList(), deptName, tenantName));
        }
        List<RoleDO> roles = roleService.getRoleList(roleIds);
        roles.removeIf(role -> !CommonStatusEnum.ENABLE.getStatus().equals(role.getStatus())); // 移除禁用的角色

        // 1.5 获得菜单列表
        Set<Long> menuIds = permissionService.getRoleMenuListByRoleId(convertSet(roles, RoleDO::getId));
        List<MenuDO> menuList = menuService.getMenuList(menuIds);
        menuList = menuService.filterDisableMenus(menuList);

        // 2. 拼接结果返回
        return success(AuthConvert.INSTANCE.convert(user, roles, menuList, deptName, tenantName));
    }

    @GetMapping(value = "/system/dict-data/simple-list")
    @Operation(summary = "获得全部字典数据列表", description = "一般用于管理后台缓存字典数据在本地")
    // 无需添加权限认证，因为前端全局都需要
    public CommonResult<List<DictDataSimpleRespVO>> getSimpleDictDataList() {
        List<DictDataDO> list = dictDataService.getDictDataList(
                CommonStatusEnum.ENABLE.getStatus(), null);
        return success(BeanUtils.toBean(list, DictDataSimpleRespVO.class));
    }

    @GetMapping("/system/notify-message/get-unread-count")
    @Operation(summary = "获得当前用户的未读站内信数量")
    @ApiAccessLog(enable = false) // 由于前端会不断轮询该接口，记录日志没有意义
    public CommonResult<Long> getUnreadNotifyMessageCount() {
        return success(notifyMessageService.getUnreadNotifyMessageCount(
                getLoginUserId(), UserTypeEnum.ADMIN.getValue()));
    }

}
