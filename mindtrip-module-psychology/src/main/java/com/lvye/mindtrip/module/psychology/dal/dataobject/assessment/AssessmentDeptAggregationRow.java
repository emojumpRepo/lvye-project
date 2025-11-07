package com.lvye.mindtrip.module.psychology.dal.dataobject.assessment;

import lombok.Data;

/**
 * 统计用：按年级/班级分组的计数结果
 */
@Data
public class AssessmentDeptAggregationRow {

    /** 年级部门ID，未知为 -1 */
    private Long gradeDeptId;
    /** 班级部门ID，未知为 -1 */
    private Long classDeptId;
    /** 分组总人数 */
    private Long totalCnt;
    /** 分组完成数 */
    private Long completedCnt;
}

