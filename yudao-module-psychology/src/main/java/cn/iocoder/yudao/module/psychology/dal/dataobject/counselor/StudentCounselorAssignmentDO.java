package cn.iocoder.yudao.module.psychology.dal.dataobject.counselor;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;

/**
 * 学生咨询师分配关系 DO
 *
 * @author 芋道源码
 */
@TableName("lvye_student_counselor_assignment")
@KeySequence("lvye_student_counselor_assignment_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCounselorAssignmentDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 学生档案编号
     */
    private Long studentProfileId;

    /**
     * 负责咨询师管理员编号
     */
    private Long counselorUserId;

    /**
     * 分配类型
     * 
     * 枚举 {@link cn.iocoder.yudao.module.psychology.enums.AssignmentTypeEnum}
     */
    private Integer assignmentType;

    /**
     * 状态
     * 
     * 枚举 {@link cn.iocoder.yudao.module.psychology.enums.AssignmentStatusEnum}
     */
    private Integer status;

    /**
     * 分配开始日期
     */
    private LocalDate startDate;

    /**
     * 分配结束日期
     */
    private LocalDate endDate;

    /**
     * 分配原因
     */
    private String assignmentReason;

}