package com.lvye.mindtrip.module.crm.dal.mysql.customer;

import com.lvye.mindtrip.framework.mybatis.core.mapper.BaseMapperX;
import com.lvye.mindtrip.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lvye.mindtrip.module.crm.dal.dataobject.customer.CrmCustomerPoolConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户公海配置 Mapper
 *
 * @author Wanwan
 */
@Mapper
public interface CrmCustomerPoolConfigMapper extends BaseMapperX<CrmCustomerPoolConfigDO> {

    default CrmCustomerPoolConfigDO selectOne() {
        return selectOne(new LambdaQueryWrapperX<CrmCustomerPoolConfigDO>().last("LIMIT 1"));
    }

}
