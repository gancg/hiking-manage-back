# RouteController 接口说明

## 1. 通用约定

- 基础路径：`/api/routes`，请求头：`Authorization: Bearer <token>`。
- 请求和响应使用驼峰字段名；路线 `id` 是字符串，新增时由客户端提供，更新时不可修改。
- 接口只维护 `routes` 主表字段；删除路线时，会在同一事务内删除五张附属表中该路线的记录。
- 沿用项目响应格式：`{"code":0,"message":"ok","data":null}`。业务校验失败仍返回 HTTP 200，通过 `code` 判断错误；未登录为 HTTP 401，无权限为 HTTP 403。
- 当前数据库尚未配置路线权限码。使用前需通过现有 RBAC 权限管理接口创建下表四个权限，并分配给调用用户的角色（`resourceType=api`、`status=active`）。详情与列表共用 `route:list`；权限判定依据权限码。

## 2. 接口列表

| 方法 | 路径 | 功能 | 权限码 | 成功时 data |
| --- | --- | --- | --- | --- |
| GET | `/api/routes` | 分页查询路线，按字符串 ID 升序 | `route:list` | 分页对象，包含 `records/total/pageNum/pageSize` |
| GET | `/api/routes/{id}` | 查询路线详情 | `route:list` | 路线对象 |
| POST | `/api/routes` | 新增路线 | `route:create` | `null` |
| PUT | `/api/routes/{id}` | 全量更新路线 | `route:update` | `null` |
| DELETE | `/api/routes/{id}` | 删除路线及关联记录 | `route:delete` | `null` |

创建权限时，`resource` 可分别填写 `/api/routes`、`/api/routes`、`/api/routes/{id}`、`/api/routes/{id}`，`action` 分别填写 `GET`、`POST`、`PUT`、`DELETE`；`name` 填写对应中文操作名称。

## 3. 新增、更新参数

请求体为 JSON 对象。POST 与 PUT 共用下表字段，区别是 POST 必须传 `id`，PUT 使用路径 ID。PUT 为全量更新，所有必填字段均需提交；可空字段省略或传 `null` 时清空，带默认值的字段省略时重置为默认值，显式传 `null` 会校验失败。

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | string | 仅新增 | 非空且唯一；建议使用 UUID 或不含路径分隔符的路线编号 |
| `name` | string | 是 | 路线名称，非空白 |
| `startLocation` | string | 是 | 起点，非空白 |
| `endLocation` | string | 是 | 终点，非空白 |
| `latitude` | number/null | 否 | 纬度，-90～90 |
| `longitude` | number/null | 否 | 经度，-180～180 |
| `distanceKm` | number | 是 | 距离（公里），大于 0 |
| `ascentM` | integer | 是 | 爬升（米），不小于 0 |
| `highestAltitudeM` | integer | 是 | 最高海拔（米），不小于 0 |
| `hikingMinutes` | integer | 是 | 徒步耗时（分钟），大于 0 |
| `difficulty` | string | 是 | `easy / moderate / hard / expert` |
| `durationDays` | integer | 是 | 天数，大于 0 |
| `routeType` | string | 是 | 路线类型，非空白 |
| `bestSeasonsJson` | string | 是 | 适宜季节的 JSON 数组文本 |
| `sceneryJson` | string | 是 | 景观的 JSON 数组文本 |
| `risksJson` | string | 是 | 风险的 JSON 数组文本 |
| `transportModesJson` | string | 是 | 交通方式的 JSON 数组文本 |
| `costMinCny` | number | 是 | 费用下限（元），不小于 0 |
| `costMaxCny` | number | 是 | 费用上限（元），不得小于下限 |
| `parking` | string/null | 否 | 停车说明 |
| `supplies` | string/null | 否 | 补给说明 |
| `signal` | string/null | 否 | 信号说明 |
| `camping` | string/null | 否 | 露营说明 |
| `sourceUrl` | string | 是 | 来源链接，非空白 |
| `sourceName` | string | 是 | 来源名称，非空白 |
| `collectedAt` | string | 是 | 采集时间，非空白；建议使用 ISO 8601 文本 |
| `confidence` | number | 是 | 置信度，0～1 |
| `reviewed` | integer | 否 | 审核标记，0 或 1，默认 0 |
| `hasToilet` | integer | 否 | 是否有厕所，0 或 1，默认 0 |
| `hasSupplyShop` | integer | 否 | 是否有补给店，0 或 1，默认 0 |
| `isTraverse` | integer | 否 | 是否穿越，0 或 1，默认 0 |
| `traverseTransferMinutes` | integer | 否 | 穿越接驳时长（分钟），不小于 0，默认 0 |
| `groupTourSearchTermsJson` | string | 否 | 跟团搜索词的 JSON 数组文本，默认 `"[]"` |

五个 `*Json` 字段传字符串，例如 `"[\"春季\",\"秋季\"]"`，不能直接传数组。服务端校验其内容为单个合法 JSON 数组，不额外限定数组元素类型。`updatedAt` 由服务端在新增、更新时生成 UTC 时间，不属于可写请求字段。

新增示例：

```json
{
  "id": "route-001",
  "name": "山林环线",
  "startLocation": "南入口",
  "endLocation": "南入口",
  "latitude": 30.2,
  "longitude": 120.1,
  "distanceKm": 10.5,
  "ascentM": 600,
  "highestAltitudeM": 1000,
  "hikingMinutes": 240,
  "difficulty": "moderate",
  "durationDays": 1,
  "routeType": "loop",
  "bestSeasonsJson": "[\"春季\",\"秋季\"]",
  "sceneryJson": "[\"山景\"]",
  "risksJson": "[]",
  "transportModesJson": "[\"self_drive\"]",
  "costMinCny": 20,
  "costMaxCny": 100,
  "parking": "入口停车场",
  "supplies": null,
  "signal": null,
  "camping": null,
  "sourceUrl": "https://example.com/routes/001",
  "sourceName": "人工录入",
  "collectedAt": "2026-09-07T00:00:00Z",
  "confidence": 0.8,
  "reviewed": 0,
  "hasToilet": 1,
  "hasSupplyShop": 0,
  "isTraverse": 0,
  "traverseTransferMinutes": 0,
  "groupTourSearchTermsJson": "[]"
}
```

更新示例：向 `PUT /api/routes/route-001` 提交上面的业务字段，去掉 `id`，修改需要调整的值。

## 4. 查询响应与删除行为

详情 `data` 包含上表所有字段及服务端维护的 `updatedAt`，如 `"2026-09-07T14:00:00Z"`；列表 `data.records` 为同结构的数组。`*Json` 字段仍为字符串，标记字段仍为数字 0/1。查询不包含附属表数据。

列表请求示例：`GET /api/routes?pageNum=1&pageSize=10`。分页参数与 `/api/rbac/permissions` 一致：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `pageNum` | integer | 否 | 页码，默认 1，必须大于等于 1 |
| `pageSize` | integer | 否 | 每页条数，默认 10，必须大于等于 1 |

列表 `data` 从数组改为分页对象：`records` 为当前页路线数组，`total` 为路线总数，`pageNum`、`pageSize` 为当前分页参数。无数据或页码超出范围时，`records` 为 `[]`，`total` 仍返回实际总数。

无数据时的响应示例：

```json
{"code":0,"message":"ok","data":{"records":[],"total":0,"pageNum":1,"pageSize":10}}
```

DELETE 不需要请求体。删除同一路线的 `route_cost_items`、`transport_cost_items`、`route_parking_points`、`traffic_profiles`、`trip_feedback` 记录后，再删除主表记录；任一步失败时整个事务回滚。

## 5. 错误码

| code | 情况 |
| --- | --- |
| `400` | 缺失必填字段、字段格式或数值范围不合法、分页参数为空或不是大于等于 1 的整数、费用上限小于下限、JSON 文本不是合法数组 |
| `401` | 未登录或登录已失效（HTTP 401） |
| `403` | 缺少对应路线权限（HTTP 403） |
| `404` | 查询详情、更新或删除时路线不存在 |
| `409` | 新增时路线 ID 已存在 |
