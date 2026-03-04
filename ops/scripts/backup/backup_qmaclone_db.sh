#!/usr/bin/env bash
set -euo pipefail

# QMAClone DB バックアップ
# - 対話入力(-p)に依存せず、defaults-extra-file で認証情報を渡す
# - 失敗時に理由が分かるように標準エラーへ明示メッセージを出す

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEFAULT_CNF="${SCRIPT_DIR}/mysql-client.cnf"
MYSQL_CNF="${MYSQL_CNF:-${DEFAULT_CNF}}"
DB_NAME="${DB_NAME:-QMAClone}"
BACKUP_BASE_DIR="${BACKUP_BASE_DIR:-/root/backup}"
DATE_TAG="$(date +%F)"
BACKUP_DIR="${BACKUP_BASE_DIR}/qmaclone-${DATE_TAG}"
OUTPUT_PATH="${BACKUP_DIR}/qmaclone.sql"

if [[ ! -f "${MYSQL_CNF}" ]]; then
  echo "MySQL 認証ファイルが見つかりません: ${MYSQL_CNF}" >&2
  echo "例: MYSQL_CNF=/root/.my.cnf $0" >&2
  exit 1
fi

mkdir -p "${BACKUP_DIR}"

echo "Run: mysqldump (${DB_NAME}) -> ${OUTPUT_PATH}"
if ! mysqldump --defaults-extra-file="${MYSQL_CNF}" --single-transaction --routines --triggers "${DB_NAME}" > "${OUTPUT_PATH}"; then
  echo "mysqldump に失敗しました。認証情報と DB 名を確認してください。" >&2
  exit 1
fi

echo "Done: ${OUTPUT_PATH}"
