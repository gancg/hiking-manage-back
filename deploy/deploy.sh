#!/usr/bin/env bash
set -euo pipefail

# 服务器需预先安装 Docker 和 Compose，并允许部署用户运行 Docker。
cd /opt/hiking-manage-back
APP_IMAGE="${1:?请传入带摘要的 GHCR 镜像地址}"
[[ "$APP_IMAGE" =~ ^ghcr\.io/[a-z0-9._/-]+@sha256:[a-f0-9]{64}$ ]]
export APP_IMAGE

# 生产配置保存在服务器，避免写入镜像或上传到代码仓库。
test -s .env || { echo "缺少 /opt/hiking-manage-back/.env"; exit 1; }
docker volume inspect hiking-data >/dev/null
docker compose --env-file .env -f compose.yaml config --quiet
docker compose --env-file .env -f compose.yaml pull app

# 必须提前导入可用数据库，并保证镜像内的 app 用户能够读写。
docker compose --env-file .env -f compose.yaml run --rm --no-deps --entrypoint bash app -c '
  [[ "$SPRING_DATASOURCE_URL" == jdbc:sqlite:* ]] || exit 1
  db_file="${SPRING_DATASOURCE_URL#jdbc:sqlite:}"
  test -s "$db_file" && test -r "$db_file" && test -w "$db_file" && test -w "$(dirname -- "$db_file")"
' || { echo "配置的数据库不存在或 app 用户没有读写权限，请检查共享数据卷和 SPRING_DATASOURCE_URL。"; exit 1; }

# 启动并等待端口检查通过，失败时直接报告错误。
docker compose --env-file .env -f compose.yaml up -d --wait --wait-timeout 180 app
docker compose --env-file .env -f compose.yaml ps
