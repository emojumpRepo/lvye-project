package com.lvye.mindtrip.module.system.service.dept;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.lvye.mindtrip.framework.common.enums.CommonStatusEnum;
import com.lvye.mindtrip.framework.common.util.object.BeanUtils;
import com.lvye.mindtrip.framework.datapermission.core.annotation.DataPermission;
import com.lvye.mindtrip.module.system.controller.admin.dept.vo.dept.DeptListReqVO;
import com.lvye.mindtrip.module.system.controller.admin.dept.vo.dept.DeptSaveReqVO;
import com.lvye.mindtrip.module.system.dal.dataobject.dept.DeptDO;
import com.lvye.mindtrip.module.system.dal.dataobject.permission.UserDeptDO;
import com.lvye.mindtrip.module.system.dal.mysql.dept.DeptMapper;
import com.lvye.mindtrip.module.system.dal.mysql.permission.UserDeptMapper;
import com.lvye.mindtrip.module.system.dal.redis.RedisKeyConstants;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.*;

import static com.lvye.mindtrip.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.lvye.mindtrip.framework.common.util.collection.CollectionUtils.convertSet;
import static com.lvye.mindtrip.module.system.enums.ErrorCodeConstants.*;

/**
 * 部门 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class DeptServiceImpl implements DeptService {

    @Resource
    private DeptMapper deptMapper;
    @Resource
    private UserDeptMapper userDeptMapper;

    @Override
    @CacheEvict(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST,
            allEntries = true) // allEntries 清空所有缓存，因为操作一个部门，涉及到多个缓存
    public Long createDept(DeptSaveReqVO createReqVO) {
        if (createReqVO.getParentId() == null) {
            createReqVO.setParentId(DeptDO.PARENT_ID_ROOT);
        }
        // 校验父部门的有效性
        validateParentDept(null, createReqVO.getParentId());
        // 校验部门名的唯一性
        validateDeptNameUnique(null, createReqVO.getParentId(), createReqVO.getName());

        // 插入部门
        DeptDO dept = BeanUtils.toBean(createReqVO, DeptDO.class);
        deptMapper.insert(dept);
        
        // 插入部门负责人关联关系
        saveLeaderUserIds(dept.getId(), createReqVO.getLeaderUserIds());
        
        return dept.getId();
    }

    @Override
    @CacheEvict(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST,
            allEntries = true) // allEntries 清空所有缓存，因为操作一个部门，涉及到多个缓存
    public void updateDept(DeptSaveReqVO updateReqVO) {
        if (updateReqVO.getParentId() == null) {
            updateReqVO.setParentId(DeptDO.PARENT_ID_ROOT);
        }
        // 校验自己存在
        validateDeptExists(updateReqVO.getId());
        // 校验父部门的有效性
        validateParentDept(updateReqVO.getId(), updateReqVO.getParentId());
        // 校验部门名的唯一性
        validateDeptNameUnique(updateReqVO.getId(), updateReqVO.getParentId(), updateReqVO.getName());

        // 更新部门
        DeptDO updateObj = BeanUtils.toBean(updateReqVO, DeptDO.class);
        deptMapper.updateById(updateObj);
        
        // 更新部门负责人关联关系：先删除旧的，再插入新的
        saveLeaderUserIds(updateReqVO.getId(), updateReqVO.getLeaderUserIds());
    }

    @Override
    @CacheEvict(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST,
            allEntries = true) // allEntries 清空所有缓存，因为操作一个部门，涉及到多个缓存
    public void deleteDept(Long id) {
        // 校验是否存在
        validateDeptExists(id);
        // 校验是否有子部门
        if (deptMapper.selectCountByParentId(id) > 0) {
            throw exception(DEPT_EXITS_CHILDREN);
        }
        // 删除部门
        deptMapper.deleteById(id);
        // 删除部门负责人关联关系
        userDeptMapper.deleteListByDeptId(id);
    }

    @Override
    @CacheEvict(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST,
            allEntries = true) // allEntries 清空所有缓存，因为操作一个部门，涉及到多个缓存
    public void deleteDeptList(List<Long> ids) {
        // 校验是否有子部门
        for (Long id : ids) {
            if (deptMapper.selectCountByParentId(id) > 0) {
                throw exception(DEPT_EXITS_CHILDREN);
            }
        }

        // 批量删除部门
        deptMapper.deleteByIds(ids);
        // 删除部门负责人关联关系
        ids.forEach(id -> userDeptMapper.deleteListByDeptId(id));
    }

    @VisibleForTesting
    void validateDeptExists(Long id) {
        if (id == null) {
            return;
        }
        DeptDO dept = deptMapper.selectById(id);
        if (dept == null) {
            throw exception(DEPT_NOT_FOUND);
        }
    }

    @VisibleForTesting
    void validateParentDept(Long id, Long parentId) {
        if (parentId == null || DeptDO.PARENT_ID_ROOT.equals(parentId)) {
            return;
        }
        // 1. 不能设置自己为父部门
        if (Objects.equals(id, parentId)) {
            throw exception(DEPT_PARENT_ERROR);
        }
        // 2. 父部门不存在
        DeptDO parentDept = deptMapper.selectById(parentId);
        if (parentDept == null) {
            throw exception(DEPT_PARENT_NOT_EXITS);
        }
        // 3. 递归校验父部门，如果父部门是自己的子部门，则报错，避免形成环路
        if (id == null) { // id 为空，说明新增，不需要考虑环路
            return;
        }
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            // 3.1 校验环路
            parentId = parentDept.getParentId();
            if (Objects.equals(id, parentId)) {
                throw exception(DEPT_PARENT_IS_CHILD);
            }
            // 3.2 继续递归下一级父部门
            if (parentId == null || DeptDO.PARENT_ID_ROOT.equals(parentId)) {
                break;
            }
            parentDept = deptMapper.selectById(parentId);
            if (parentDept == null) {
                break;
            }
        }
    }

    @VisibleForTesting
    void validateDeptNameUnique(Long id, Long parentId, String name) {
        DeptDO dept = deptMapper.selectByParentIdAndName(parentId, name);
        if (dept == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的部门
        if (id == null) {
            throw exception(DEPT_NAME_DUPLICATE);
        }
        if (ObjectUtil.notEqual(dept.getId(), id)) {
            throw exception(DEPT_NAME_DUPLICATE);
        }
    }

    @Override
    public DeptDO getDept(Long id) {
        DeptDO dept = deptMapper.selectById(id);
        if (dept != null) {
            fillLeaderUserIds(Collections.singletonList(dept));
        }
        return dept;
    }

    @Override
    public List<DeptDO> getDeptList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<DeptDO> deptList = deptMapper.selectByIds(ids);
        fillLeaderUserIds(deptList);
        return deptList;
    }

    @Override
    public List<DeptDO> getDeptList(DeptListReqVO reqVO) {
        List<DeptDO> list = deptMapper.selectList(reqVO);
        list.sort(Comparator.comparing(DeptDO::getSort));
        fillLeaderUserIds(list);
        return list;
    }

    @Override
    public List<DeptDO> getChildDeptList(Collection<Long> ids) {
        List<DeptDO> children = new LinkedList<>();
        // 遍历每一层
        Collection<Long> parentIds = ids;
        for (int i = 0; i < Short.MAX_VALUE; i++) { // 使用 Short.MAX_VALUE 避免 bug 场景下，存在死循环
            // 查询当前层，所有的子部门
            List<DeptDO> depts = deptMapper.selectListByParentId(parentIds);
            // 1. 如果没有子部门，则结束遍历
            if (CollUtil.isEmpty(depts)) {
                break;
            }
            // 2. 如果有子部门，继续遍历
            children.addAll(depts);
            parentIds = convertSet(depts, DeptDO::getId);
        }
        fillLeaderUserIds(children);
        return children;
    }

    @Override
    public List<DeptDO> getDeptListByLeaderUserId(Long id) {
        // 从两个地方查询：1. leaderUserId 字段，2. system_user_dept 表
        List<DeptDO> deptList = deptMapper.selectListByLeaderUserId(id);
        
        // 从 system_user_dept 表中查询该用户作为负责人的部门
        List<UserDeptDO> userDeptList = userDeptMapper.selectListByUserId(id);
        if (CollUtil.isNotEmpty(userDeptList)) {
            Set<Long> deptIds = convertSet(userDeptList, UserDeptDO::getDeptId);
            List<DeptDO> deptListFromUserDept = deptMapper.selectByIds(deptIds);
            
            // 合并结果并去重
            Set<Long> existingDeptIds = convertSet(deptList, DeptDO::getId);
            for (DeptDO dept : deptListFromUserDept) {
                if (!existingDeptIds.contains(dept.getId())) {
                    deptList.add(dept);
                }
            }
        }
        
        fillLeaderUserIds(deptList);
        return deptList;
    }

    @Override
    @DataPermission(enable = false) // 禁用数据权限，避免建立不正确的缓存
    @Cacheable(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST, key = "#id")
    public Set<Long> getChildDeptIdListFromCache(Long id) {
        List<DeptDO> children = getChildDeptList(id);
        return convertSet(children, DeptDO::getId);
    }

    @Override
    public void validateDeptList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 获得科室信息
        Map<Long, DeptDO> deptMap = getDeptMap(ids);
        // 校验
        ids.forEach(id -> {
            DeptDO dept = deptMap.get(id);
            if (dept == null) {
                throw exception(DEPT_NOT_FOUND);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(dept.getStatus())) {
                throw exception(DEPT_NOT_ENABLE, dept.getName());
            }
        });
    }

    @Override
    public DeptDO getDeptByName(String name){
        return deptMapper.selectByName(name);
    }

    /**
     * 保存部门负责人关联关系
     *
     * @param deptId 部门ID
     * @param leaderUserIds 负责人用户ID列表
     */
    private void saveLeaderUserIds(Long deptId, List<Long> leaderUserIds) {
        // 先删除旧的关联关系
        userDeptMapper.deleteListByDeptId(deptId);
        
        // 插入新的关联关系
        if (CollUtil.isNotEmpty(leaderUserIds)) {
            List<UserDeptDO> userDeptList = leaderUserIds.stream()
                    .map(userId -> {
                        UserDeptDO userDept = new UserDeptDO();
                        userDept.setUserId(userId);
                        userDept.setDeptId(deptId);
                        return userDept;
                    })
                    .collect(java.util.stream.Collectors.toList());
            userDeptList.forEach(userDept -> userDeptMapper.insert(userDept));
        }
    }

    /**
     * 为部门列表填充负责人用户ID列表
     *
     * @param deptList 部门列表
     */
    private void fillLeaderUserIds(List<DeptDO> deptList) {
        if (CollUtil.isEmpty(deptList)) {
            return;
        }
        
        // 获取所有部门ID
        Set<Long> deptIds = convertSet(deptList, DeptDO::getId);
        
        // 查询所有部门的负责人关联关系
        List<UserDeptDO> userDeptList = userDeptMapper.selectListByDeptIds(deptIds);
        
        // 按部门ID分组
        Map<Long, List<Long>> deptUserMap = userDeptList.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        UserDeptDO::getDeptId,
                        java.util.stream.Collectors.mapping(UserDeptDO::getUserId, java.util.stream.Collectors.toList())
                ));
        
        // 填充到部门对象中
        deptList.forEach(dept -> {
            List<Long> userIds = deptUserMap.get(dept.getId());
            dept.setLeaderUserIds(userIds != null ? userIds : Collections.emptyList());
        });
    }

}
