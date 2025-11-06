package cn.iocoder.yudao.module.psychology.dal.mysql.interventionplan;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionPlanOngoingRespVO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.interventionplan.InterventionEventDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 干预事件 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InterventionEventMapper extends BaseMapperX<InterventionEventDO> {

    /**
     * 根据学生档案ID查询干预事件列表
     *
     * @param studentProfileId 学生档案ID
     * @return 干预事件列表
     */
    default List<InterventionEventDO> selectListByStudentId(Long studentProfileId) {
        return selectList(InterventionEventDO::getStudentProfileId, studentProfileId);
    }

    /**
     * 分页查询正在进行的干预计划列表
     *
     * @param page 分页对象
     * @return 分页结果
     */
    IPage<InterventionPlanOngoingRespVO> selectOngoingPage(IPage<InterventionPlanOngoingRespVO> page);

}
