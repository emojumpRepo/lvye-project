package com.lvye.mindtrip.module.mp.dal.mysql.message;

import com.lvye.mindtrip.framework.common.pojo.PageResult;
import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lvye.mindtrip.module.mp.controller.admin.message.vo.message.MpMessagePageReqVO;
import com.lvye.mindtrip.module.mp.dal.dataobject.message.MpMessageDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MpMessageMapper extends BaseMapperX<MpMessageDO> {

    default PageResult<MpMessageDO> selectPage(MpMessagePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MpMessageDO>()
                .eqIfPresent(MpMessageDO::getAccountId, reqVO.getAccountId())
                .eqIfPresent(MpMessageDO::getType, reqVO.getType())
                .eqIfPresent(MpMessageDO::getOpenid, reqVO.getOpenid())
                .eqIfPresent(MpMessageDO::getUserId, reqVO.getUserId())
                .betweenIfPresent(MpMessageDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MpMessageDO::getId));
    }

}
