### 一、心理咨询模块 API 需求

#### 1. 咨询管理 (Counseling Management)

*   **GET `/api/counseling/today`**
    *   **功能**: 获取心理老师工作台“今日咨询”的列表和统计数据。
    *   **返回**: 今日咨询总数、已完成数、待完成数，以及按时间排序的咨询任务列表（包含学生信息、时间、状态、是否逾期等）。

*   **POST `/api/counseling/appointments`**
    *   **功能**: 创建一个新的咨询预约。
    *   **参数**: `studentId`, `date`, `timeSlot`, `counselingType`, `location` (可选), `notes` (可选), `notifyStudent` (布尔), `remindSelf` (布尔)。
    *   **后端逻辑**: 需处理时间冲突检测、学生归属验证，并触发通知。

*   **POST `/api/counseling/{id}/complete`**
    *   **功能**: 将指定咨询标记为“已完成”。
    *   **参数**: `{ "fillAssessmentNow": boolean }` (用于判断是否立即跳转)。
    *   **后端逻辑**: 更新咨询状态从“已预约”到“已完成”。

*   **POST `/api/counseling/{id}/adjust-time`**
    *   **功能**: 调整已预约的咨询时间。
    *   **参数**: `{ "newDate": "YYYY-MM-DD", "newTimeSlot": "HH:MM-HH:MM" }`。
    *   **后端逻辑**: 需再次进行时间冲突检测。

*   **POST `/api/counseling/{id}/cancel`**
    *   **功能**: 取消一个预约。
    *   **参数**: `{ "reason": "...", "customReason": "..." }`。
    *   **后端逻辑**: 更新状态为“已取消”，并记录原因。

#### 2. 咨询评估 (Counseling Assessment)

*   **GET `/api/counseling/{id}/assessment`**
    *   **功能**: 获取指定咨询的评估信息，用于评估表单第一步（信息确认）。
    *   **后端逻辑**: 如果存在草稿，应一并返回草稿内容。

*   **POST `/api/counseling/{id}/assessment`**
    *   **功能**: 提交完整的评估报告。
    *   **参数**: 包含风险等级、问题类型、后续建议、评估内容（富文本或文件URL）等。
    *   **后端逻辑**: 校验必填项，并将咨询记录状态更新为“已闭环”。

*   **POST `/api/counseling/{id}/assessment/draft`**
    *   **功能**: 保存评估表单草稿。
    *   **参数**: 同上。

*   **GET `/api/admin/settings/assessment-overdue-time`**
    *   **功能**: (管理员) 获取评估报告的逾期时间阈值。

*   **PUT `/api/admin/settings/assessment-overdue-time`**
    *   **功能**: (管理员) 设置评估报告的逾期时间阈值。

#### 3. 咨询闭环看板 (Counseling Loop Dashboard)

*   **GET `/api/counseling/loop-dashboard`**
    *   **功能**: 获取咨询闭环看板的数据列表。
    *   **查询参数**: 支持按学生/咨询师搜索、按日期范围筛选、按流程状态（待完成、待评估、已闭环、已逾期）筛选。
    *   **返回**: 包含可视化步骤条状态（咨询、评估）的咨询事件列表。

*   **POST `/api/counseling/{id}/remind`**
    *   **功能**: 向指定咨询的负责人发送催办提醒。

---

### 二、危机干预模块 API 需求

#### 1. 五级干预看板 (Five-Level Intervention Dashboard)

*   **GET `/api/intervention/dashboard/summary`**
    *   **功能**: 获取五级干预看板的总体统计数据。
    *   **查询参数**: 支持按班级、按咨询师（或我负责的）进行筛选。
    *   **返回**: 返回一个对象，包含每个等级（`major`, `severe`, `general`, `observation`, `normal`）的学生数量。 e.g., `{ "major": 10, "severe": 25, ... }`

*   **GET `/api/intervention/dashboard/students`**
    *   **功能**: 获取指定干预等级下的学生列表（分页）。
    *   **查询参数**:
        *   `level`: 干预等级 (必需, e.g., `major`, `severe`, `general`, `observation`, `normal`)
        *   `page`: 页码 (可选, 默认 1)
        *   `pageSize`: 每页数量 (可选, 默认 10)
        *   其他筛选条件，如班级、咨询师等。
    *   **返回**: 分页后的学生卡片列表及总数。

*   **PUT `/api/students/{id}/intervention-level`**
    *   **功能**: 调整学生的心理健康风险等级。
    *   **参数**: `{ "targetLevel": "...", "reason": "..." }`。
    *   **后端逻辑**: 记录等级变更历史。

#### 2. 危机事件管理 (Crisis Event Management)

*   **POST `/api/intervention/events`**
    *   **功能**: 上报一个新的危机事件。
    *   **参数**: `studentId`, `eventTime`, `location`, `eventLevel`, `description`, `attachmentUrls` (文件URL列表)。

*   **GET `/api/intervention/events`**
    *   **功能**: 获取危机事件列表。
    *   **查询参数**: 支持按状态、等级、学生、日期范围进行筛选和排序。

*   **GET `/api/intervention/events/{id}`**
    *   **功能**: 获取单个危机事件的详细信息，包括所有处理流程记录。

*   **POST `/api/intervention/events/{id}/assign`**
    *   **功能**: 为危机事件分配负责人。
    *   **参数**: `{ "counselorId": "..." }`。

*   **POST `/api/intervention/events/{id}/process`**
    *   **功能**: 添加一条事件处理记录。
    *   **参数**: `{ "content": "...", "attachmentUrls": [...] }`。

*   **POST `/api/intervention/events/{id}/close`**
    *   **功能**: 结案一个危机事件。
    *   **参数**: `{ "summary": "..." }`。

*   **GET `/api/admin/settings/intervention-assignment-mode`**
    *   **功能**: (管理员) 获取危机事件的分配模式。

*   **PUT `/api/admin/settings/intervention-assignment-mode`**
    *   **功能**: (管理员) 设置危机事件的分配模式（手动或自动）。