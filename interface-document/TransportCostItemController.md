# TransportCostItemController 接口说明

## 1. 通用约定

- 对应表：`transport_cost_items`；基础路径：`/api/transport-cost-items`。
- 所有接口需要请求头：`Authorization: Bearer <token>`。
- 新增、更新使用 JSON 请求体，字段名为驼峰，`Content-Type: application/json`。
- 使用本模块独立权限 `transport-cost-item:list`、`transport-cost-item:create`、`transport-cost-item:update`、`transport-cost-item:delete` 权限。通过现有 RBAC 分配给调用用户；列表和详情共用查询权限。角色 ID 为 1 已关联本模块全部操作权限。
- 统一响应：`{"code":0,"message":"ok","data":null}`。业务校验失败沿用 HTTP 200，以非零 `code` 表示错误；未登录 HTTP 401，无权限 HTTP 403。
- `id` 为服务端生成的自增整数，`routeId` 为所属路线的字符串 ID。
- `updatedAt` 由服务端在新增、更新时维护，使用 UTC 时间，不接受客户端修改。

## 2. 接口列表

| 方法 | 路径 | 功能 | 权限 | 成功时 data |
| --- | --- | --- | --- | --- |
| GET | `/api/transport-cost-items` | 分页列表 | `transport-cost-item:list` | 分页对象，包含 `records/total/pageNum/pageSize` |
| GET | `/api/transport-cost-items/{id}` | 详情 | `transport-cost-item:list` | 完整对象 |
| POST | `/api/transport-cost-items` | 新增 | `transport-cost-item:create` | 保存后的完整对象，包含自增 id |
| PUT | `/api/transport-cost-items/{id}` | 全量更新 | `transport-cost-item:update` | `null` |
| DELETE | `/api/transport-cost-items/{id}` | 删除 | `transport-cost-item:delete` | `null` |

列表支持可选参数 `routeId`，例如 `/api/transport-cost-items?routeId=route-001`。不传时不限制所属路线，按自增 ID 升序分页。指定路线不存在时返回 `code=404`，空白路线 ID 返回 `code=400`。


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

详情、删除无需请求体。路径参数是记录的自增整数 ID，例如 `1`。删除只移除当前记录，不删除路线及其他附属记录。

## 3. 新增和更新参数

PUT 为全量更新，必须提交全部必填字段。可空字段省略或传 `null` 会清空；有默认值的字段省略会恢复默认值，显式传 `null` 会校验失败。

POST 与 PUT 的业务字段相同，均不传 `id`。PUT 可修改 `routeId`，目标路线必须存在。

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `routeId` | string | 是 | 路线ID；必须关联已存在的路线 |
| `name` | string | 是 | 费用名称 |
| `billingUnit` | string | 是 | 计费单位；person / vehicle / group |
| `minCny` | number | 是 | 费用下限；不小于 0 |
| `maxCny` | number | 是 | 费用上限；不小于 0；不得小于 minCny |
| `sourceUrl` | string | 是 | 来源链接 |
| `costType` | string | 是 | 交通费用类型；fuel / toll / train / bus / other |
| `transportMode` | string | 是 | 交通方式；self_drive / public_transit / carpool / group_tour |

费用单位为人民币元；校验 `0 ≤ minCny ≤ maxCny`。费用明细变更不会自动重算 `routes.costMinCny/costMaxCny`。

新增示例（`route-001` 必须已经存在）：

```json
{
  "routeId": "route-001",
  "name": "油费",
  "costType": "fuel",
  "transportMode": "self_drive",
  "billingUnit": "vehicle",
  "minCny": 30,
  "maxCny": 100,
  "sourceUrl": "https://example.com/transport"
}
```

更新时向 `PUT /api/transport-cost-items/1` 提交上述业务字段，修改需要调整的值。

## 4. 新增、详情响应示例

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "id": 1,
    "routeId": "route-001",
    "name": "油费",
    "costType": "fuel",
    "transportMode": "self_drive",
    "billingUnit": "vehicle",
    "minCny": 30,
    "maxCny": 100,
    "sourceUrl": "https://example.com/transport",
    "updatedAt": "2026-09-07T14:00:00Z"
  }
}
```

列表的 `data` 改为分页对象，其中 `records` 是同结构的对象数组；更新、删除成功时 `data=null`。

## 5. 错误码

| code | 情况 |
| --- | --- |
| 400 | 缺少必填字段、空白路线 ID、枚举或数值范围错误、费用上限小于下限 |
| 401 | 未登录或登录失效（HTTP 401） |
| 403 | 缺少对应路线权限（HTTP 403） |
| 404 | 目标记录不存在，或关联路线不存在 |
