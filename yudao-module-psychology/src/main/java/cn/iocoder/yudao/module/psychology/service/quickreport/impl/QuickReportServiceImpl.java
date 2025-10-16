package cn.iocoder.yudao.module.psychology.service.quickreport.impl;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.psychology.controller.admin.quickreport.vo.*;
import cn.iocoder.yudao.module.psychology.dal.dataobject.profile.StudentProfileDO;
import cn.iocoder.yudao.module.psychology.dal.dataobject.quickreport.QuickReportDO;
import cn.iocoder.yudao.module.psychology.dal.mysql.profile.StudentProfileMapper;
import cn.iocoder.yudao.module.psychology.dal.mysql.quickreport.QuickReportMapper;
import cn.iocoder.yudao.module.psychology.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.psychology.enums.QuickReportStatusEnum;
import cn.iocoder.yudao.module.psychology.enums.TimelineEventTypeEnum;
import cn.iocoder.yudao.module.psychology.service.profile.StudentTimelineService;
import cn.iocoder.yudao.module.psychology.service.quickreport.QuickReportService;
import cn.iocoder.yudao.module.system.api.user.dto.QuickReportHandleUserVO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

/**
 * @Author: MinGoo
 * @CreateTime: 2025-08-31
 * @Description:快速上报服务层
 * @Version: 1.0
 */
@Service
public class QuickReportServiceImpl implements QuickReportService {

    @Autowired
    private QuickReportMapper quickReportMapper;

    @Autowired
    private StudentTimelineService studentTimelineService;

    @Autowired
    private StudentProfileMapper studentProfileMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public void saveQuickReport(QuickReportSaveReqVO saveReqVO){
        Long userId = WebFrameworkUtils.getLoginUserId();
        QuickReportDO quickReportDO = new QuickReportDO();
        quickReportDO.setStudentProfileId(saveReqVO.getStudentProfileId());
        quickReportDO.setReporterId(userId);
        quickReportDO.setReportTitle(saveReqVO.getReportTitle());
        quickReportDO.setReportContent(saveReqVO.getReportContent());
        quickReportDO.setUrgencyLevel(saveReqVO.getUrgencyLevel());
        quickReportDO.setIncidentTime(saveReqVO.getIncidentTime());
        quickReportDO.setReportTime(new Date());
        quickReportDO.setStatus(QuickReportStatusEnum.PENDING.getStatus());
        quickReportDO.setHandlerId(saveReqVO.getHandlerId());
        quickReportDO.setFollowUpRequired(saveReqVO.getFollowUpRequired());
        quickReportDO.setTags(saveReqVO.getTags());
        quickReportDO.setAttachments(saveReqVO.getAttachments());
        quickReportMapper.insert(quickReportDO);
        //时间线
        studentTimelineService.saveTimeline(saveReqVO.getStudentProfileId(), TimelineEventTypeEnum.QUICK_REPORT.getType(), TimelineEventTypeEnum.QUICK_REPORT.getName(), String.valueOf(quickReportDO.getId()));
    }

    @Override
    public PageResult<QuickReportVO> quickReportPage(QuickReportPageReqVO pageReqVO){
        IPage<QuickReportVO> page = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        quickReportMapper.selectPageList(page, pageReqVO);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public PageResult<QuickReportVO> myQuickReportPage(QuickReportPageReqVO pageReqVO){
        Long userId = WebFrameworkUtils.getLoginUserId();
        pageReqVO.setUserId(userId);
        pageReqVO.setHandler(null);
        pageReqVO.setReporter(null);
        IPage<QuickReportVO> page = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        quickReportMapper.selectPageList(page, pageReqVO);
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public void updateQuickReport(QuickReportHandleReqVO handleReqVO){
        QuickReportDO quickReportDO = new QuickReportDO();
        quickReportDO.setId(handleReqVO.getId());
        quickReportDO.setReportTitle(handleReqVO.getReportTitle());
        quickReportDO.setReportContent(handleReqVO.getReportContent());
        quickReportDO.setStatus(handleReqVO.getStatus());
        quickReportDO.setHandleNotes(handleReqVO.getHandleNotes());
        quickReportDO.setHandleTime(handleReqVO.getHandleTime());
        quickReportDO.setTags(handleReqVO.getTags());
        quickReportDO.setAttachments(handleReqVO.getAttachments());
        quickReportMapper.updateById(quickReportDO);
    }

    @Override
    public List<QuickReportHandleUserVO> selectHandleUserList(Long studentProfileId){
        List<QuickReportHandleUserVO> resultList = new ArrayList<>();
        StudentProfileDO studentProfileDO = studentProfileMapper.selectById(studentProfileId);
        if (studentProfileDO == null){
            throw exception(ErrorCodeConstants.STUDENT_PROFILE_NOT_EXISTS);
        }
        Long classId = studentProfileDO.getClassDeptId();
        Long gradeId = studentProfileDO.getGradeDeptId();
        //心理老师
        RoleDO roleDO = roleMapper.selectByCode("psychology_teacher");
        if (roleDO != null){
            List<QuickReportHandleUserVO> userList =  userRoleMapper.selectUserListByRoleIdAndDeptId(roleDO.getId(), null);
            resultList.addAll(userList);
        }
        //年级管理员
        RoleDO gradeTeacherRoleDO = roleMapper.selectByCode("grade_teacher");
        if (gradeTeacherRoleDO != null){
            List<QuickReportHandleUserVO> userList =  userRoleMapper.selectUserListByRoleIdAndDeptId(gradeTeacherRoleDO.getId(), gradeId);
            resultList.addAll(userList);
        }
        //普通老师
        RoleDO teacherRoleDO = roleMapper.selectByCode("teacher");
        if (teacherRoleDO != null){
            List<QuickReportHandleUserVO> userList =  userRoleMapper.selectUserListByRoleIdAndDeptId(teacherRoleDO.getId(), gradeId);
            resultList.addAll(userList);
        }
        //去重
        if (!resultList.isEmpty()){
            List<QuickReportHandleUserVO> uniqueResultList = resultList.stream().collect(collectingAndThen(
                    toCollection(() -> new TreeSet<>(comparingLong(QuickReportHandleUserVO::getUserId))), ArrayList::new));
            return uniqueResultList;
        }
        return resultList;
    }


}
