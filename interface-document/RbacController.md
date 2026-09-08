# RbacController 接口说明（前端 Agent 使用）

## 1. 通用约定

- 基础路径：`/api/rbac`
- 所有接口都需要登录（请求头：`Authorization: Bearer <token>`）
- 统一响应结构：

```json
{
  "code": 0,
  "message": "ok",
  "data": []
}
```

- `code = 0` 表示成功；非 `0` 表示失败。
- 除登录态校验外，接口还受权限码控制（见各接口“权限要求”）。

## 2. 接口列表

### 2.1 查询用户列表

- 方法/路径：`GET /api/rbac/users`
- 权限要求：`rbac:user:list`
- 请求参数：无
- 成功 `data`：`RbacUserDto[]`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 用户 ID |
| username | string | 用户名 |
| displayName | string | 显示名 |
| email | string | 邮箱 |
| mobile | string | 手机号 |
| status | string | 用户状态（`active` / `disabled`） |
| roleCodes | string[] | 角色编码列表 |

### 2.1.1 查询用户详情

- 方法/路径：`GET /api/rbac/users/{id}`
- 权限要求：`rbac:user:list`
- 路径参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 用户 ID |

- 成功 `data`：`RbacUserDto`
- 常见失败：
  - `code: 404` 用户不存在
  - `code: 401` 未登录、Token 无效或已过期
  - `code: 403` 无权限（缺少 `rbac:user:list`）

### 2.2 创建用户

- 方法/路径：`POST /api/rbac/create`
- 权限要求：`rbac:user:create`
- 请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| username | string | 是 | 用户名 |
| password | string | 否 | 密码（不传时使用系统默认密码） |
| displayName | string | 是 | 显示名 |
| email | string | 否 | 邮箱（需符合邮箱格式） |
| mobile | string | 否 | 手机号 |
| roleIds | number[] | 是 | 角色 ID 列表，不能为空 |

- 成功响应：`data = null`
- 常见失败：
  - `code: 400` 参数校验失败 / 请求参数格式错误 / 角色不存在或已禁用
  - `code: 409` 用户名已存在
  - `code: 401` 未登录、Token 无效或已过期
  - `code: 403` 无权限（缺少 `rbac:user:create`）

### 2.3 修改用户状态

- 方法/路径：`PATCH /api/rbac/users/{id}/status`
- 权限要求：`rbac:user:update`
- 路径参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 用户 ID |

- 请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| status | string | 是 | 仅支持：`active`、`disabled` |

- 成功响应：`data = null`
- 常见失败：
  - `code: 400` 参数校验失败 / 请求参数格式错误
  - `code: 404` 用户不存在
  - `code: 401` 未登录、Token 无效或已过期
  - `code: 403` 无权限（缺少 `rbac:user:update`）

### 2.4 修改用户信息与角色

- 方法/路径：`PUT /api/rbac/users/{id}`
- 权限要求：`rbac:user:update`
- 路径参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 用户 ID |

- 请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| displayName | string | 是 | 显示名 |
| email | string | 否 | 邮箱（需符合邮箱格式） |
| mobile | string | 否 | 手机号 |
| roleIds | number[] | 是 | 角色 ID 列表，不能为空 |

- 成功响应：`data = null`
- 常见失败：
  - `code: 400` 参数校验失败 / 请求参数格式错误 / 角色不存在或已禁用
  - `code: 404` 用户不存在
  - `code: 401` 未登录、Token 无效或已过期
  - `code: 403` 无权限（缺少 `rbac:user:update`）

### 2.5 删除用户（硬删除）

- 方法/路径：`DELETE /api/rbac/users/{id}`
- 权限要求：`rbac:user:delete`
- 路径参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 用户 ID |

- 成功响应：`data = null`
- 常见失败：
  - `code: 404` 用户不存在
  - `code: 401` 未登录、Token 无效或已过期
  - `code: 403` 无权限（缺少 `rbac:user:delete`）

### 2.6 重置用户密码

- 方法/路径：`POST /api/rbac/users/{id}/reset-password`
- 权限要求：`rbac:user:reset-password`
- 路径参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 用户 ID |

- 行为说明：将该用户密码重置为系统配置的默认密码（`auth.default-password`）。
- 成功响应：`data = null`
- 常见失败：
  - `code: 404` 用户不存在
  - `code: 401` 未登录、Token 无效或已过期
  - `code: 403` 无权限（缺少 `rbac:user:reset-password`）

### 2.7 查询角色列表

- 方法/路径：`GET /api/rbac/roles`
- 权限要求：`rbac:role:list`
- 请求参数：无
- 成功 `data`：`RoleDto[]`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 角色 ID |
| code | string | 角色编码 |
| name | string | 角色名称 |
| status | string | 角色状态 |

### 2.8 查询角色详情

- 方法/路径：`GET /api/rbac/roles/{id}`
- 权限要求：`rbac:role:list`
- 路径参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 角色 ID |

- 成功 `data`：`RoleDetailDto`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 角色 ID |
| code | string | 角色编码 |
| name | string | 角色名称 |
| description | string | 角色描述 |
| status | string | 角色状态（`active` / `disabled`） |
| permissionIds | number[] | 角色关联的权限 ID 列表 |

- 常见失败：
  - `code: 404` 角色不存在
  - `code: 401` 未登录、Token 无效或已过期
  - `code: 403` 无权限（缺少 `rbac:role:list`）

### 2.9 创建角色

- 方法/路径：`POST /api/rbac/roles`
- 权限要求：`rbac:role:create`
- 请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| code | string | 是 | 角色编码（唯一） |
| name | string | 是 | 角色名称 |
| description | string | 否 | 角色描述 |
| status | string | 是 | 仅支持：`active`、`disabled` |
| permissionIds | number[] | 否 | 权限 ID 列表（为空时表示无权限） |

- 成功响应：`data = null`
- 常见失败：
  - `code: 400` 参数校验失败 / 请求参数格式错误 / 权限不存在或已禁用
  - `code: 409` 角色编码已存在
  - `code: 401` 未登录、Token 无效或已过期
  - `code: 403` 无权限（缺少 `rbac:role:create`）

### 2.10 修改角色

- 方法/路径：`PUT /api/rbac/roles/{id}`
- 权限要求：`rbac:role:update`
- 路径参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 角色 ID |

- 请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| code | string | 是 | 角色编码（唯一） |
| name | string | 是 | 角色名称 |
| description | string | 否 | 角色描述 |
| status | string | 是 | 仅支持：`active`、`disabled` |
| permissionIds | number[] | 否 | 权限 ID 列表（为空时表示无权限） |

- 成功响应：`data = null`
- 常见失败：
  - `code: 400` 参数校验失败 / 请求参数格式错误 / 权限不存在或已禁用
  - `code: 404` 角色不存在
  - `code: 409` 角色编码已存在
  - `code: 401` 未登录、Token 无效或已过期
  - `code: 403` 无权限（缺少 `rbac:role:update`）

### 2.11 删除角色

- 方法/路径：`DELETE /api/rbac/roles/{id}`
- 权限要求：`rbac:role:delete`
- 路径参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 角色 ID |

- 成功响应：`data = null`
- 常见失败：
  - `code: 400` 角色已关联用户，无法删除
  - `code: 404` 角色不存在
  - `code: 401` 未登录、Token 无效或已过期
  - `code: 403` 无权限（缺少 `rbac:role:delete`）

### 2.12 查询权限列表

- 方法/路径：`GET /api/rbac/permissions`
- 权限要求：`rbac:permission:list`
- 请求参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| pageNum | number | 否 | 页码，默认 `1`，最小值 `1` |
| pageSize | number | 否 | 每页条数，默认 `10`，最小值 `1` |

- 成功 `data`：分页对象

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| records | `PermissionDto[]` | 当前页权限列表 |
| total | number | 权限总数 |
| pageNum | number | 当前页码 |
| pageSize | number | 每页条数 |

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 权限 ID |
| code | string | 权限编码 |
| name | string | 权限名称 |
| resource | string | 资源标识 |
| action | string | 动作标识 |
| status | string | 权限状态 |

### 2.12.1 查询权限详情

- 方法/路径：`GET /api/rbac/permissions/{id}`
- 权限要求：`rbac:permission:list`
- 路径参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 权限 ID |

- 成功 `data`：`PermissionDetailDto`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 权限 ID |
| code | string | 权限编码 |
| name | string | 权限名称 |
| resourceType | string | 资源类型 |
| resource | string | 资源标识 |
| action | string | 动作标识 |
| parentId | number/null | 父级权限 ID |
| sortOrder | number | 排序值 |
| status | string | 权限状态（`active` / `disabled`） |

### 2.12.2 创建权限

- 方法/路径：`POST /api/rbac/permissions`
- 权限要求：`rbac:permission:create`
- 请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| code | string | 是 | 权限编码（唯一） |
| name | string | 是 | 权限名称 |
| resourceType | string | 是 | 资源类型 |
| resource | string | 是 | 资源标识 |
| action | string | 是 | 动作标识 |
| parentId | number | 否 | 父级权限 ID |
| sortOrder | number | 否 | 排序值，不传默认为 `0` |
| status | string | 是 | 仅支持：`active`、`disabled` |

- 成功响应：`data = null`
- 常见失败：
  - `code: 400` 参数校验失败 / 请求参数格式错误
  - `code: 409` 权限编码已存在
  - `code: 401` 未登录、Token 无效或已过期
  - `code: 403` 无权限（缺少 `rbac:permission:create`）

### 2.12.3 修改权限

- 方法/路径：`PUT /api/rbac/permissions/{id}`
- 权限要求：`rbac:permission:update`
- 路径参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 权限 ID |

- 请求体：同“创建权限”
- 成功响应：`data = null`
- 常见失败：
  - `code: 400` 参数校验失败 / 请求参数格式错误
  - `code: 404` 权限不存在
  - `code: 409` 权限编码已存在
  - `code: 401` 未登录、Token 无效或已过期
  - `code: 403` 无权限（缺少 `rbac:permission:update`）

### 2.12.4 删除权限

- 方法/路径：`DELETE /api/rbac/permissions/{id}`
- 权限要求：`rbac:permission:delete`
- 路径参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 权限 ID |

- 成功响应：`data = null`
- 常见失败：
  - `code: 400` 权限已关联角色，无法删除
  - `code: 404` 权限不存在
  - `code: 401` 未登录、Token 无效或已过期
  - `code: 403` 无权限（缺少 `rbac:permission:delete`）

## 3. 统一错误码（前端可统一处理）

| code | 场景 |
| --- | --- |
| 0 | 成功 |
| 400 | 参数校验失败/请求参数格式错误/业务参数错误 |
| 401 | 未登录或登录失效（Token 无效/过期） |
| 403 | 已登录但无权限访问 |
| 404 | 资源不存在（如用户不存在） |
| 409 | 资源冲突（如用户名已存在） |
