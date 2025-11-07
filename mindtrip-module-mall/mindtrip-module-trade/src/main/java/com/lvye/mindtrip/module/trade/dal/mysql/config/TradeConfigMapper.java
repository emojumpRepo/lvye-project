package com.lvye.mindtrip.module.trade.dal.mysql.config;

import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.module.trade.dal.dataobject.config.TradeConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 交易中心配置 Mapper
 *
 * @author owen
 */
@Mapper
public interface TradeConfigMapper extends BaseMapperX<TradeConfigDO> {

}
