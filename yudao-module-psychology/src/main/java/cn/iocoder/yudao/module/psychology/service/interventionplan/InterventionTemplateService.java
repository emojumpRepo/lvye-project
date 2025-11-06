package cn.iocoder.yudao.module.psychology.service.interventionplan;

import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionTemplateCreateReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionTemplateRespVO;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionTemplateSimpleRespVO;
import cn.iocoder.yudao.module.psychology.controller.admin.interventionplan.vo.InterventionTemplateUpdateReqVO;
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
     * 更新干预计划模板
     *
     * @param updateReqVO 更新信息
     */
    void updateTemplate(@Valid InterventionTemplateUpdateReqVO updateReqVO);

    /**
     * 查询所有模板列表
     *
     * @return 模板列表，包含步骤信息
     */
    List<InterventionTemplateRespVO> getTemplateList();

    /**
     * 根据ID获取模板详情
     *
     * @param id 模板ID
     * @return 模板详情，包含步骤信息
     */
    InterventionTemplateRespVO getTemplateById(Long id);

    /**
     * 删除干预计划模板
     *
     * @param id 模板ID
     */
    void deleteTemplate(Long id);

}
