# TrafficProfileController 接口说明

## 1. 通用约定

- 对应表：`traffic_profiles`；基础路径：`/api/traffic-profiles`。
- 所有接口需要请求头：`Authorization: Bearer <token>`。
- 新增、更新使用 JSON 请求体，字段名为驼峰，`Content-Type: application/json`。
- 使用本模块独立权限 `traffic-profile:list`、`traffic-profile:create`、`traffic-profile:update`、`traffic-profile:delete` 权限。通过现有 RBAC 分配给调用用户；列表和详情共用查询权限。角色 ID 为 1 已关联本模块全部操作权限。
- 统一响应：`{"code":0,"message":"ok","data":null}`。业务校验失败沿用 HTTP 200，以非零 `code` 表示错误；未登录 HTTP 401，无权限 HTTP 403。
- 字符串 `routeId` 同时为主键和关联路线 ID，每条路线最多一条交通画像。
- `updatedAt` 由服务端在新增、更新时维护，使用 UTC 时间，不接受客户端修改。

## 2. 接口列表

| 方法 | 路径 | 功能 | 权限 | 成功时 data |
| --- | --- | --- | --- | --- |
| GET | `/api/traffic-profiles` | 分页列表 | `traffic-profile:list` | 分页对象，包含 `records/total/pageNum/pageSize` |
| GET | `/api/traffic-profiles/{routeId}` | 详情 | `traffic-profile:list` | 完整对象 |
| POST | `/api/traffic-profiles` | 新增 | `traffic-profile:create` | 保存后的完整对象 |
| PUT | `/api/traffic-profiles/{routeId}` | 全量更新 | `traffic-profile:update` | `null` |
| DELETE | `/api/traffic-profiles/{routeId}` | 删除 | `traffic-profile:delete` | `null` |

列表支持可选参数 `routeId`，例如 `/api/traffic-profiles?routeId=route-001`。不传时不限制所属路线，按字符串 routeId 升序分页。指定路线不存在时返回 `code=404`，空白路线 ID 返回 `code=400`。


分页参数与路线列表一致，可与 `routeId` 同时使用，例如追加 `&pageNum=1&pageSize=10`：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `pageNum` | integer | 否 | 默认 1，必须大于等于 1 |
| `pageSize` | integer | 否 | 默认 10，必须大于等于 1 |

分页参数为空、非整数或小于 1 时返回 `code=400`。`data.records` 为当前页记录，`total` 为筛选后的记录总数，`pageNum`、`pageSize` 为当前分页参数。没有匹配记录或页码超出范围时，`records=[]`，`total` 仍为实际匹配总数。

无匹配记录时的响应示例：

```json
{"code":0,"message":"ok","data":{"records":[],"total":0,"pageNum":1,"pageSize":10}}
```

详情、删除无需请求体。路径参数是路线字符串 ID，例如 `route-001`。删除只移除当前记录，不删除路线及其他附属记录。

## 3. 新增和更新参数

PUT 为全量更新，必须提交全部必填字段。可空字段省略或传 `null` 会清空；有默认值的字段省略会恢复默认值，显式传 `null` 会校验失败。

POST 必须传 `routeId`；PUT 不包含该字段，所属路线由路径确定，不能修改。

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `routeId` | string | 仅新增 | 路线ID；必须关联已存在的路线 |
| `baseOneWayMinutes` | integer | 是 | 基础单程耗时；不小于 0 |
| `weekdayExtraMin` | integer | 否 | 工作日额外耗时下限；不小于 0；默认 0 |
| `weekdayExtraMax` | integer | 否 | 工作日额外耗时上限；不小于 0；默认 0；不得小于对应 ExtraMin |
| `weekendExtraMin` | integer | 否 | 周末额外耗时下限；不小于 0；默认 0 |
| `weekendExtraMax` | integer | 否 | 周末额外耗时上限；不小于 0；默认 0；不得小于对应 ExtraMin |
| `holidayExtraMin` | integer | 否 | 节假日额外耗时下限；不小于 0；默认 0 |
| `holidayExtraMax` | integer | 否 | 节假日额外耗时上限；不小于 0；默认 0；不得小于对应 ExtraMin |
| `morningExtraMinutes` | integer | 否 | 早间额外耗时；不小于 0；默认 0 |
| `eveningExtraMinutes` | integer | 否 | 晚间额外耗时；不小于 0；默认 0 |
| `commonBottlenecksJson` | string | 是 | 常见拥堵点JSON；必须是 JSON 数组文本字符串 |
| `bestDepartureTime` | string/null | 否 | 最佳出发时间；可空，省略或传 null 时清空 |
| `suggestedReturnTime` | string/null | 否 | 建议返程时间；可空，省略或传 null 时清空 |
| `sourceUrl` | string | 是 | 来源链接 |
| `confidence` | number | 是 | 置信度；0～1 |

耗时字段单位为分钟。`commonBottlenecksJson` 传 JSON 数组文本，例如 `"[\"入口路段\"]"`，不能直接传数组；服务端校验单个合法 JSON 数组，不限制数组元素类型。两个建议时间字段为文本，可填写 `07:00`。

新增示例（`route-001` 必须已经存在）：

```json
{
  "routeId": "route-001",
  "baseOneWayMinutes": 60,
  "weekdayExtraMin": 0,
  "weekdayExtraMax": 10,
  "weekendExtraMin": 10,
  "weekendExtraMax": 30,
  "holidayExtraMin": 20,
  "holidayExtraMax": 60,
  "morningExtraMinutes": 10,
  "eveningExtraMinutes": 20,
  "commonBottlenecksJson": "[\"入口路段\"]",
  "bestDepartureTime": "07:00",
  "suggestedReturnTime": "16:00",
  "sourceUrl": "https://example.com/traffic",
  "confidence": 0.8
}
```

更新时去掉示例中的 `routeId`，向 `PUT /api/traffic-profiles/route-001` 提交其余字段，修改需要调整的值。

## 4. 新增、详情响应示例

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "routeId": "route-001",
    "baseOneWayMinutes": 60,
    "weekdayExtraMin": 0,
    "weekdayExtraMax": 10,
    "weekendExtraMin": 10,
    "weekendExtraMax": 30,
    "holidayExtraMin": 20,
    "holidayExtraMax": 60,
    "morningExtraMinutes": 10,
    "eveningExtraMinutes": 20,
    "commonBottlenecksJson": "[\"入口路段\"]",
    "bestDepartureTime": "07:00",
    "suggestedReturnTime": "16:00",
    "sourceUrl": "https://example.com/traffic",
    "confidence": 0.8,
    "updatedAt": "2026-09-07T14:00:00Z"
  }
}
```

列表的 `data` 改为分页对象，其中 `records` 是同结构的对象数组；更新、删除成功时 `data=null`。

## 5. 错误码

| code | 情况 |
| --- | --- |
| 400 | 缺少必填字段、空白路线 ID、枚举或数值范围错误、额外耗时区间错误、拥堵点 JSON 不合法 |
| 401 | 未登录或登录失效（HTTP 401） |
| 403 | 缺少对应路线权限（HTTP 403） |
| 404 | 目标记录不存在，或关联路线不存在 |
| 409 | 该路线已有交通画像，需使用 PUT 更新 |
