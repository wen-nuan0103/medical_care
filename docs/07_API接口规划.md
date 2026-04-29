# 在线医疗健康平台 —— API 接口规划

> 文档版本：v1.0  
> 文档日期：2026-04-27  
> 接口风格：RESTful API + WebSocket

## 1. 通用规范

### 1.1 基础路径

```text
/api
```

### 1.2 请求头

```text
Authorization: Bearer <token>
Content-Type: application/json
```

### 1.3 通用响应

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 1.4 分页响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

### 1.5 常见错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录或 Token 失效 |
| 403 | 无权限 |
| 404 | 数据不存在 |
| 409 | 状态冲突，例如库存不足、处方不可支付 |
| 500 | 系统异常 |

## 2. 登录认证接口

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| POST | `/api/auth/login` | 登录 | 全部 |
| POST | `/api/auth/logout` | 退出登录 | 全部 |
| GET | `/api/auth/me` | 获取当前用户信息 | 全部 |
| GET | `/api/auth/menus` | 获取当前角色菜单 | 全部 |

## 3. 用户与医生接口

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET | `/api/doctors` | 医生列表 | 患者、管理员 |
| GET | `/api/doctors/{id}` | 医生详情 | 患者、管理员 |
| POST | `/api/admin/doctors` | 新增医生 | 管理员 |
| PUT | `/api/admin/doctors/{id}` | 修改医生 | 管理员 |
| PUT | `/api/admin/doctors/{id}/status` | 修改医生服务状态 | 管理员 |
| GET | `/api/admin/users` | 用户列表 | 管理员 |
| PUT | `/api/admin/users/{id}/status` | 启用或禁用用户 | 管理员 |

## 4. 私人医生卡接口

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET | `/api/doctors/{doctorId}/card-plans` | 查询医生服务卡配置 | 患者、医生 |
| POST | `/api/doctor/card-plans` | 医生新增服务卡配置 | 医生 |
| PUT | `/api/doctor/card-plans/{id}` | 医生修改服务卡配置 | 医生 |
| POST | `/api/patient/private-cards/purchase` | 患者购买私人医生卡 | 患者 |
| GET | `/api/patient/private-cards` | 查询我的私人医生卡 | 患者 |
| GET | `/api/doctor/private-card-patients` | 医生查看购买自己的患者 | 医生 |

购买服务卡请求示例：

```json
{
  "doctorId": 1,
  "planId": 3
}
```

## 5. 在线问诊接口

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| POST | `/api/consultations` | 创建问诊会话 | 患者 |
| GET | `/api/consultations` | 查询问诊列表 | 患者、医生 |
| GET | `/api/consultations/{id}` | 查询问诊详情 | 患者、医生 |
| PUT | `/api/consultations/{id}/end` | 结束问诊 | 患者、医生 |
| GET | `/api/consultations/{id}/messages` | 查询历史消息 | 患者、医生 |
| POST | `/api/consultations/{id}/gift-time` | 医生赠送聊天时长 | 医生 |

赠送时长请求示例：

```json
{
  "minutes": 10,
  "reason": "患者病情说明较复杂，补充沟通时间"
}
```

## 6. 电子处方接口

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| POST | `/api/doctor/prescriptions` | 创建处方草稿 | 医生 |
| PUT | `/api/doctor/prescriptions/{id}` | 修改处方 | 医生 |
| POST | `/api/doctor/prescriptions/{id}/submit` | 提交审核 | 医生 |
| GET | `/api/prescriptions/{id}` | 查看处方详情 | 患者、医生、药剂师、管理员 |
| GET | `/api/patient/prescriptions` | 患者查看处方 | 患者 |
| GET | `/api/doctor/prescriptions` | 医生查看处方 | 医生 |

处方提交请求示例：

```json
{
  "sessionId": 1001,
  "patientId": 2001,
  "diagnosis": "急性上呼吸道感染",
  "items": [
    {
      "drugId": 3001,
      "quantity": 1,
      "dosage": "0.5g",
      "frequency": "每日 3 次",
      "durationDays": 5,
      "usageMethod": "口服"
    }
  ]
}
```

## 7. 药剂师审核接口

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET | `/api/pharmacist/audits/pending` | 待审核处方列表 | 药剂师 |
| GET | `/api/pharmacist/audits/{prescriptionId}/check` | 自动检查库存和相互作用 | 药剂师 |
| POST | `/api/pharmacist/audits/{prescriptionId}` | 提交审核结果 | 药剂师 |
| GET | `/api/pharmacist/audits/history` | 审核历史 | 药剂师 |

审核请求示例：

```json
{
  "auditResult": "APPROVED",
  "advice": "建议饭后服用，如出现皮疹或明显不适请及时复诊。"
}
```

## 8. 药品库与商城接口

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET | `/api/drugs` | 药品列表 | 全部 |
| GET | `/api/drugs/{id}` | 药品详情 | 全部 |
| POST | `/api/pharmacist/drugs` | 新增药品 | 药剂师、管理员 |
| PUT | `/api/pharmacist/drugs/{id}` | 修改药品 | 药剂师、管理员 |
| PUT | `/api/pharmacist/drugs/{id}/stock` | 调整库存 | 药剂师、管理员 |
| GET | `/api/pharmacist/drug-interactions` | 相互作用规则列表 | 药剂师、管理员 |
| POST | `/api/pharmacist/drug-interactions` | 新增相互作用规则 | 药剂师、管理员 |

## 9. 医保卡与购药接口

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| POST | `/api/patient/insurance-cards` | 绑定模拟医保卡 | 患者 |
| GET | `/api/patient/insurance-cards` | 查询我的医保卡 | 患者 |
| POST | `/api/patient/medicine-orders/from-prescription` | 根据处方创建购药订单 | 患者 |
| GET | `/api/patient/medicine-orders` | 查询我的购药订单 | 患者 |
| GET | `/api/patient/medicine-orders/{id}` | 查询订单详情 | 患者 |
| POST | `/api/patient/medicine-orders/{id}/pay-insurance` | 使用医保卡支付 | 患者 |

医保支付请求示例：

```json
{
  "insuranceCardId": 5001
}
```

## 10. 病患跟踪接口

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| POST | `/api/patient/health-tracks` | 新增健康跟踪记录 | 患者 |
| GET | `/api/patient/health-tracks` | 查看自己的健康跟踪 | 患者 |
| GET | `/api/doctor/patients/{patientId}/health-tracks` | 医生查看患者跟踪 | 医生 |
| POST | `/api/doctor/follow-ups` | 医生创建随访计划 | 医生 |
| GET | `/api/doctor/follow-ups` | 医生查看随访计划 | 医生 |

## 11. 用药提醒接口

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| GET | `/api/patient/medication-plans` | 查看用药计划 | 患者 |
| GET | `/api/patient/medication-reminders/today` | 查看今日提醒 | 患者 |
| POST | `/api/patient/medication-reminders/{id}/taken` | 标记已服药 | 患者 |
| POST | `/api/patient/medication-reminders/{id}/missed` | 标记漏服 | 患者 |
| POST | `/api/patient/medication-reminders/{id}/snooze` | 稍后提醒 | 患者 |
| GET | `/api/doctor/patients/{patientId}/medication-records` | 医生查看用药执行情况 | 医生 |

## 12. WebSocket 事件规划

### 12.1 连接地址

```text
/ws?token=<jwt>
```

### 12.2 聊天事件

| 事件 | 方向 | 说明 |
|------|------|------|
| `chat.send` | 客户端 -> 服务端 | 发送聊天消息 |
| `chat.receive` | 服务端 -> 客户端 | 接收聊天消息 |
| `chat.read` | 客户端 -> 服务端 | 消息已读 |
| `chat.time.warning` | 服务端 -> 客户端 | 剩余时长不足提醒 |
| `chat.ended` | 服务端 -> 客户端 | 问诊结束 |

聊天消息示例：

```json
{
  "sessionId": 1001,
  "receiverId": 2002,
  "messageType": "TEXT",
  "content": "医生您好，我今天咳嗽加重了。"
}
```

### 12.3 通知事件

| 事件 | 方向 | 说明 |
|------|------|------|
| `prescription.audit.result` | 服务端 -> 客户端 | 处方审核结果 |
| `medicine.stock.warning` | 服务端 -> 客户端 | 库存预警 |
| `medication.reminder` | 服务端 -> 客户端 | 用药提醒 |
| `order.payment.result` | 服务端 -> 客户端 | 支付结果 |
| `ai.generation.completed` | 服务端 -> 客户端 | AI 生成任务完成 |

## 13. Spring AI 智能辅助接口

| 方法 | 路径 | 说明 | 角色 |
|------|------|------|------|
| POST | `/api/ai/consultations/{sessionId}/summary` | 生成问诊摘要草稿 | 医生 |
| POST | `/api/ai/prescriptions/{prescriptionId}/explain` | 生成处方通俗解释 | 医生 |
| POST | `/api/ai/prescriptions/{prescriptionId}/audit-suggestion` | 生成药师审核辅助提示 | 药剂师 |
| POST | `/api/ai/medication-plans/{planId}/reminder-text` | 生成用药提醒文案 | 患者、医生 |
| POST | `/api/ai/suggestions/{id}/confirm` | 人工确认 AI 建议 | 医生、药剂师 |
| POST | `/api/ai/suggestions/{id}/reject` | 拒绝 AI 建议 | 医生、药剂师 |
| GET | `/api/ai/suggestions` | 查询 AI 调用记录 | 医生、药剂师、管理员 |
| POST | `/api/admin/ai/knowledge-documents` | 新增 AI 知识文档 | 管理员 |
| GET | `/api/admin/ai/knowledge-documents` | 查询 AI 知识文档 | 管理员 |

AI 审核辅助响应示例：

```json
{
  "suggestionId": 9001,
  "scenario": "PHARMACY_AUDIT",
  "riskLevel": "MEDIUM",
  "summary": "处方中存在需要关注的用药组合，建议药剂师重点核对剂量和患者过敏史。",
  "suggestions": [
    "核对患者是否存在青霉素类药物过敏史",
    "提醒患者按疗程服药，不适时及时复诊"
  ],
  "references": [
    {
      "title": "阿莫西林药品说明",
      "docType": "DRUG_MANUAL"
    }
  ],
  "disclaimer": "AI 结果仅供辅助，最终审核结果以药剂师确认为准。"
}
```

## 14. 接口开发优先级

| 优先级 | 接口范围 |
|--------|----------|
| P0 | 登录、医生列表、购卡、问诊、处方、审核、医保支付、药品库存 |
| P1 | 赠送时长、药物相互作用规则、病患跟踪、用药提醒、Spring AI 辅助 |
| P2 | 统计报表、复杂筛选、消息队列异步通知 |
