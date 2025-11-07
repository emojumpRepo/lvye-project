### 一、心理咨询模块 API 需求

#### 1. 咨询管理 (Counseling Management)

*   **GET `/psychology/consultation/today`**
    *   **功能**: 获取心理老师工作台"今日咨询"的列表和统计数据。
    *   **返回**: 今日咨询总数、已完成数、待完成数，以及按时间排序的咨询任务列表（包含学生信息、时间、状态、是否逾期等）。

*   **POST `/psychology/consultation/appointment/create`**
    *   **功能**: 创建一个新的咨询预约。
    *   **参数**: `studentProfileId`, `appointmentTime`, `durationMinutes`, `consultationType`, `location` (可选), `notes` (可选), `notifyStudent` (布尔), `remindSelf` (布尔)。
    *   **后端逻辑**: 需处理时间冲突检测、学生归属验证，并触发通知。

*   **PUT `/psychology/consultation/appointment/{id}/complete`**
    *   **功能**: 将指定咨询标记为"已完成"。
    *   **参数**: `{ "fillAssessmentNow": boolean }` (用于判断是否立即跳转)。
    *   **后端逻辑**: 更新咨询状态从"已预约"到"已完成"。

*   **PUT `/psychology/consultation/appointment/{id}/adjust-time`**
    *   **功能**: 调整已预约的咨询时间。
    *   **参数**: `{ "newDate": "YYYY-MM-DD", "newTimeSlot": "HH:MM-HH:MM" }`。
    *   **后端逻辑**: 需再次进行时间冲突检测。

*   **PUT `/psychology/consultation/appointment/{id}/cancel`**
    *   **功能**: 取消一个预约。
    *   **参数**: `{ "reason": "...", "customReason": "..." }`。
    *   **后端逻辑**: 更新状态为"已取消"，并记录原因。

#### 2. 咨询评估 (Counseling Assessment)

*   **GET `/psychology/consultation/assessment/{appointmentId}`**
    *   **功能**: 获取指定咨询的评估信息，用于评估表单第一步（信息确认）。
    *   **后端逻辑**: 如果存在草稿，应一并返回草稿内容。

*   **POST `/psychology/consultation/assessment/save`**
    *   **功能**: 提交完整的评估报告。
    *   **参数**: 包含appointmentId、风险等级、问题类型、后续建议、评估内容（富文本或文件URL）等。
    *   **后端逻辑**: 校验必填项，并将咨询记录状态更新为"已闭环"。

*   **POST `/psychology/consultation/assessment/draft`**
    *   **功能**: 保存评估表单草稿。
    *   **参数**: 同上。

*   **GET `/psychology/admin/settings/assessment-overdue-time`**
    *   **功能**: (管理员) 获取评估报告的逾期时间阈值。

*   **PUT `/psychology/admin/settings/assessment-overdue-time`**
    *   **功能**: (管理员) 设置评估报告的逾期时间阈值。

*   **PUT `/psychology/consultation/appointment/{id}/supplement`**
    *   **功能**: 补录咨询记录（用于逾期的预约）。
    *   **参数**: `{ "actualTime": "YYYY-MM-DD HH:MM", "notes": "..." }`。
    *   **后端逻辑**: 直接将状态更新为"已完成"，记录实际咨询时间。

#### 3. 咨询闭环看板 (Counseling Loop Dashboard)

*   **GET `/psychology/consultation/loop-dashboard/page`**
    *   **功能**: 获取咨询闭环看板的数据列表。
    *   **查询参数**: 支持按学生/咨询师搜索、按日期范围筛选、按流程状态（待完成、待评估、已闭环、已逾期）筛选。
    *   **返回**: 包含可视化步骤条状态（咨询、评估）的咨询事件列表。

*   **POST `/psychology/consultation/appointment/{id}/remind`**
    *   **功能**: 向指定咨询的负责人发送催办提醒。

*   **GET `/psychology/consultation/loop-dashboard/export`**
    *   **功能**: 导出咨询闭环看板数据。
    *   **查询参数**: 同loop-dashboard/page接口。
    *   **返回**: Excel文件下载。

---

### 二、危机干预模块 API 需求

#### 1. 五级干预看板 (Five-Level Intervention Dashboard)

*   **GET `/psychology/intervention/dashboard/summary`**
    *   **功能**: 获取五级干预看板的总体统计数据。
    *   **查询参数**: 支持按班级、按咨询师（或我负责的）进行筛选。
    *   **返回**: 返回一个对象，包含每个等级（`major`, `severe`, `general`, `observation`, `normal`, `pending_assessment`）的学生数量。

*   **GET `/psychology/intervention/dashboard/students/page`**
    *   **功能**: 获取指定干预等级下的学生列表（分页）。
    *   **查询参数**:
        *   `level`: 干预等级 (必需)
        *   `pageNo`: 页码 (可选, 默认 1)
        *   `pageSize`: 每页数量 (可选, 默认 10)
        *   其他筛选条件，如班级、咨询师等。
    *   **返回**: 分页后的学生卡片列表及总数，包含学生就读状态标识。

*   **PUT `/psychology/intervention/student/{studentProfileId}/level`**
    *   **功能**: 调整学生的心理健康风险等级。
    *   **参数**: `{ "targetLevel": "...", "reason": "..." }`。
    *   **后端逻辑**: 记录等级变更历史。

#### 2. 危机事件管理 (Crisis Event Management)

*   **POST `/psychology/intervention/event/create`**
    *   **功能**: 上报一个新的危机事件（快速上报功能）。
    *   **参数**: `studentProfileId`, `eventTime`, `location`, `eventLevel`, `description`, `attachmentUrls` (文件URL列表), `priority` (高/中/低)。
    *   **后端逻辑**: 创建事件记录，根据分配模式自动或手动分配负责人。

*   **GET `/psychology/intervention/event/page`**
    *   **功能**: 获取危机事件列表。
    *   **查询参数**: 支持按状态（待分配/待处理/处理中/已解决/持续关注/已关闭）、等级、优先级、学生、日期范围进行筛选和排序。
    *   **返回**: 事件列表，包含处理进度条、当前负责人等信息。

*   **GET `/psychology/intervention/event/statistics`**
    *   **功能**: 获取危机事件的统计数据。
    *   **返回**: 各状态事件数量统计（待处理、已解决、持续关注、误报/关闭）。

*   **GET `/psychology/intervention/event/{id}`**
    *   **功能**: 获取单个危机事件的详细信息，包括所有处理流程记录。

*   **PUT `/psychology/intervention/event/{id}/assign`**
    *   **功能**: 为危机事件分配负责人。
    *   **参数**: `{ "handlerUserId": "...", "assignReason": "..." }`。
    *   **后端逻辑**: 支持分配给心理老师、班主任、年级主任等，记录分配历史。

*   **PUT `/psychology/intervention/event/{id}/reassign`**
    *   **功能**: 更改危机事件的负责人。
    *   **参数**: `{ "newHandlerUserId": "...", "reason": "..." }`。
    *   **后端逻辑**: 记录负责人变更历史，发送通知给新负责人。

*   **PUT `/psychology/intervention/event/{id}/process`**
    *   **功能**: 选择事件处理方式并开始处理。
    *   **参数**: `{ "processReason": "...", "processMethod": "interview/assessment/continuous_attention/direct_resolve" }`。
    *   **后端逻辑**: 更新事件状态，如果选择持续关注或直接解决，需要立即填写评估。

*   **PUT `/psychology/intervention/event/{id}/close`**
    *   **功能**: 结案一个危机事件。
    *   **参数**: `{ "summary": "...", "finalAssessment": {...} }`。
    *   **后端逻辑**: 包含最终评估（风险等级、问题类型、后续建议），更新学生档案。

*   **POST `/psychology/intervention/event/{id}/stage-assessment`**
    *   **功能**: 提交阶段性评估报告（心理访谈或量表评估后）。
    *   **参数**: `{ "riskLevel": "...", "problemTypes": [...], "followUpSuggestion": "continue_interview/continue_assessment/continuous_attention/resolved" }`。
    *   **后端逻辑**: 根据后续建议决定事件流转（继续处理或结案）。

*   **GET `/psychology/intervention/event/{id}/process-history`**
    *   **功能**: 获取事件的完整处理历史记录。
    *   **返回**: 时间倒序的操作记录列表，包含操作时间、操作人、动作、详细内容。

*   **GET `/psychology/admin/settings/intervention-assignment-mode`**
    *   **功能**: (管理员) 获取危机事件的分配模式。
    *   **返回**: `{ "mode": "auto/manual", "description": "..." }`。

*   **PUT `/psychology/admin/settings/intervention-assignment-mode`**
    *   **功能**: (管理员) 设置危机事件的分配模式（手动或自动）。
    *   **参数**: `{ "mode": "auto/manual" }`。
    *   **后端逻辑**: 自动模式根据学生绑定的心理老师和班主任自动分配，手动模式需要年级管理员手动分配。

*   **GET `/psychology/intervention/event/check-duplicate`**
    *   **功能**: 检测重复上报。
    *   **查询参数**: `studentProfileId`。
    *   **返回**: 24小时内该学生的已有事件记录。