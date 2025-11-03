package cn.iocoder.yudao.module.psychology.service.intervention;

import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionTemplateCreateReqVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 干预计划模板 Service 接口
 *
 * @author 芋道源码
 */
public interface InterventionTemplateService {

    /**
     * 创建干预计划模板
     *
     * @param createReqVO 创建信息
     * @return 模板ID
     */
    Long createTemplate(@Valid InterventionTemplateCreateReqVO createReqVO);

    /**
     * 查询所有模板和步骤
     *
     * @return 模板列表，包含步骤信息
     */
    List<InterventionTemplateCreateReqVO> getTemplateList();

}
