package com.lvye.mindtrip.module.iot.dal.mysql.device;

import com.lvye.mindtrip.framework.common.pojo.PageResult;
import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lvye.mindtrip.module.iot.controller.admin.device.vo.group.IotDeviceGroupPageReqVO;
import com.lvye.mindtrip.module.iot.dal.dataobject.device.IotDeviceGroupDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 设备分组 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotDeviceGroupMapper extends BaseMapperX<IotDeviceGroupDO> {

    default PageResult<IotDeviceGroupDO> selectPage(IotDeviceGroupPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotDeviceGroupDO>()
                .likeIfPresent(IotDeviceGroupDO::getName, reqVO.getName())
                .betweenIfPresent(IotDeviceGroupDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotDeviceGroupDO::getId));
    }

    default List<IotDeviceGroupDO> selectListByStatus(Integer status) {
        return selectList(IotDeviceGroupDO::getStatus, status);
    }

    default IotDeviceGroupDO selectByName(String name) {
        return selectOne(IotDeviceGroupDO::getName, name);
    }

}