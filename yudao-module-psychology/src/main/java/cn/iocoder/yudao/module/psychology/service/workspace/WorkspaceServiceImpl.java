package cn.iocoder.yudao.module.psychology.service.workspace;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment.ConsultationAppointmentPageReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.consultation.vo.appointment.ConsultationAppointmentRespVO;
import cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo.CrisisEventPageReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo.CrisisEventRespVO;
import cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo.InterventionStudentPageReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.intervention.vo.InterventionStudentRespVO;
import cn.iocoder.yudao.module.psychology.controller.admin.workspace.vo.WorkspaceDataPageReqVO;
import cn.iocoder.yudao.module.psychology.controller.admin.workspace.vo.WorkspaceDataTypeEnum;
import cn.iocoder.yudao.module.psychology.dal.dataobject.consultation.CrisisInterventionDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.consultation.CrisisInterventionMapper;
import cn.iocoder.yudao.module.psychology.service.consultation.ConsultationAppointmentService;
import cn.iocoder.yudao.module.psychology.service.intervention.CrisisInterventionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 工作台 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

    @Resource
    private ConsultationAppointmentService consultationAppointmentService;

    @Resource
    private CrisisInterventionService crisisInterventionService;

    @Resource
    private CrisisInterventionMapper crisisInterventionMapper;

    @Override
    public PageResult<?> getWorkspaceDataPage(WorkspaceDataPageReqVO pageReqVO) {
        WorkspaceDataTypeEnum typeEnum = WorkspaceDataTypeEnum.fromCode(pageReqVO.getType());

        switch (typeEnum) {
            case TODAY_CONSULTATIONS:
                return getTodayConsultations(pageReqVO);
            case HIGH_RISK_STUDENTS:
                return getHighRiskStudents(pageReqVO);
            case PENDING_ALERTS:
                return getPendingAlerts(pageReqVO);
            default:
                throw new IllegalArgumentException("未知的工作台数据类型: " + pageReqVO.getType());
        }
    }

    /**
     * 获取今日心理咨询任务
     *
     * @param pageReqVO 分页请求参数
     * @return 今日咨询任务分页数据
     */
    private PageResult<ConsultationAppointmentRespVO> getTodayConsultations(WorkspaceDataPageReqVO pageReqVO) {
        // 构造查询参数
        ConsultationAppointmentPageReqVO queryVO = new ConsultationAppointmentPageReqVO();
        queryVO.setPageNo(pageReqVO.getPageNo());
        queryVO.setPageSize(pageReqVO.getPageSize());

        // 设置今天的时间戳（毫秒）
        LocalDate today = LocalDate.now();
        long todayTimestamp = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        queryVO.setConsultTime(todayTimestamp);

        // 如果指定了咨询师，则过滤
        if (pageReqVO.getCounselorUserId() != null) {
            queryVO.setCounselorUserId(pageReqVO.getCounselorUserId());
        }

        return consultationAppointmentService.getAppointmentPage(queryVO);
    }

    /**
     * 获取重点干预学生（风险等级为5）
     *
     * @param pageReqVO 分页请求参数
     * @return 重点干预学生分页数据
     */
    private PageResult<InterventionStudentRespVO> getHighRiskStudents(WorkspaceDataPageReqVO pageReqVO) {
        // 查询风险等级为5的学生
        return crisisInterventionService.getStudentsByRiskLevel(
                5, // 风险等级5
                null, // classId，工作台不按班级过滤
                pageReqVO.getCounselorUserId(), // 咨询师ID
                pageReqVO.getPageNo(),
                pageReqVO.getPageSize()
        );
    }

    /**
     * 获取待处理预警事件（status为1或2）
     *
     * @param pageReqVO 分页请求参数
     * @return 待处理预警事件分页数据
     */
    private PageResult<CrisisEventRespVO> getPendingAlerts(WorkspaceDataPageReqVO pageReqVO) {
        // 构造查询参数，查询 status=1 和 status=2 的事件
        CrisisEventPageReqVO queryVO1 = new CrisisEventPageReqVO();
        queryVO1.setPageNo(1);
        queryVO1.setPageSize(Integer.MAX_VALUE); // 获取所有数据
        queryVO1.setStatus(1);
        if (pageReqVO.getCounselorUserId() != null) {
            queryVO1.setCounselorUserId(pageReqVO.getCounselorUserId());
        }

        CrisisEventPageReqVO queryVO2 = new CrisisEventPageReqVO();
        queryVO2.setPageNo(1);
        queryVO2.setPageSize(Integer.MAX_VALUE);
        queryVO2.setStatus(2);
        if (pageReqVO.getCounselorUserId() != null) {
            queryVO2.setCounselorUserId(pageReqVO.getCounselorUserId());
        }

        // 查询两种状态的数据
        PageResult<CrisisEventRespVO> result1 = crisisInterventionService.getCrisisEventPage(queryVO1);
        PageResult<CrisisEventRespVO> result2 = crisisInterventionService.getCrisisEventPage(queryVO2);

        // 合并结果
        List<CrisisEventRespVO> allEvents = new ArrayList<>();
        if (CollUtil.isNotEmpty(result1.getList())) {
            allEvents.addAll(result1.getList());
        }
        if (CollUtil.isNotEmpty(result2.getList())) {
            allEvents.addAll(result2.getList());
        }

        // 按创建时间倒序排序
        allEvents.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));

        // 手动分页
        int start = (pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize();
        int end = Math.min(start + pageReqVO.getPageSize(), allEvents.size());

        List<CrisisEventRespVO> pagedList = new ArrayList<>();
        if (start < allEvents.size()) {
            pagedList = allEvents.subList(start, end);
        }

        return new PageResult<>(pagedList, (long) allEvents.size());
    }
}
