# AuthController 接口说明（前端 Agent 使用）

## 1. 通用约定

- 基础路径：`/api/auth`
- 除登录接口外，均需携带请求头：`Authorization: Bearer <token>`
- 统一响应结构：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

- `code = 0` 表示成功；非 `0` 表示失败。

## 2. 接口列表

### 2.1 登录

- 方法/路径：`POST /api/auth/login`
- 鉴权：否
- 请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |

- 成功 `data`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| token | string | JWT Token |
| tokenType | string | 固定为 `Bearer` |
| expiresAtEpochSecond | number | 过期时间（Unix 秒级时间戳） |
| needChangePassword | boolean | 是否需要修改密码（`true` 表示当前仍为默认密码） |
| passwordTip | string/null | 密码提示文案，`needChangePassword=true` 时返回 |
| user | object | 当前用户信息 |

`user` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | number | 用户 ID |
| username | string | 用户名 |
| displayName | string | 显示名 |
| status | string | 用户状态（如 `active` / `disabled`） |
| roles | string[] | 角色编码列表 |
| permissions | string[] | 权限编码列表 |

- 成功响应示例：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresAtEpochSecond": 1780000000,
    "needChangePassword": true,
    "passwordTip": "当前密码为默认密码，请尽快修改密码",
    "user": {
      "id": 1,
      "username": "admin",
      "displayName": "系统管理员",
      "status": "active",
      "roles": ["super_admin"],
      "permissions": ["rbac:user:list", "rbac:user:create"]
    }
  }
}
```

- 常见失败：
  - `code: 400` 参数校验失败（如用户名/密码为空）
  - `code: 401` 用户名或密码错误
  - `code: 403` 账号已禁用

### 2.2 登出

- 方法/路径：`POST /api/auth/logout`
- 鉴权：是（需登录）
- 请求体：无
- 成功响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": null
}
```

- 常见失败：
  - `code: 401` 未登录、Token 无效或已过期

### 2.3 获取当前登录用户信息

- 方法/路径：`GET /api/auth/me`
- 鉴权：是（需登录）
- 请求体：无
- 成功 `data` 结构：与登录返回中的 `user` 一致
- 常见失败：
  - `code: 401` 未登录、Token 无效或已过期
  - `code: 404` 用户不存在

### 2.4 修改当前用户密码

- 方法/路径：`POST /api/auth/change-password`
- 鉴权：是（需登录）
- 请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| oldPassword | string | 是 | 旧密码 |
| newPassword | string | 是 | 新密码 |

- 成功响应：`data = null`
- 常见失败：
  - `code: 400` 参数校验失败 / 旧密码错误 / 新旧密码不能相同
  - `code: 401` 未登录、Token 无效或已过期
  - `code: 404` 用户不存在
