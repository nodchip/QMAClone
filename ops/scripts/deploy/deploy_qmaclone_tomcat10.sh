#!/usr/bin/env bash
set -euo pipefail

# nighthawk 向け Tomcat10 配備スクリプト
# 本番ビルド方針: PRETTY / 非ドラフト / optimize=9

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"

SOURCE_WAR_NAME="QMAClone-1.0-SNAPSHOT.war"
SOURCE_WAR_PATH="${REPO_ROOT}/target/${SOURCE_WAR_NAME}"
DEPLOY_WAR_NAME="QMAClone.war"

TOMCAT_WEBAPPS_DIR="${TOMCAT_WEBAPPS_DIR:-/var/lib/tomcat10/webapps}"
APP_CONTEXT_DIR="${TOMCAT_WEBAPPS_DIR}/QMAClone"
LEGACY_CONTEXT_DIR="${TOMCAT_WEBAPPS_DIR}/QMAClone-1.0-SNAPSHOT"
LEGACY_WAR_PATH="${TOMCAT_WEBAPPS_DIR}/QMAClone-1.0-SNAPSHOT.war"
SERVICE_NAME="${SERVICE_NAME:-tomcat10}"
SKIP_BUILD="${SKIP_BUILD:-false}"

run_maven() {
  local -a args=("$@")
  echo "Run: mvn ${args[*]}"
  (cd "${REPO_ROOT}" && mvn "${args[@]}")
}

run_maven_with_forbidden_output_check() {
  local forbidden_regex="$1"
  shift
  local -a args=("$@")
  local log_file
  log_file="$(mktemp)"

  echo "Run: mvn ${args[*]}"
  set +e
  (cd "${REPO_ROOT}" && mvn "${args[@]}") 2>&1 | tee "${log_file}"
  local mvn_exit=${PIPESTATUS[0]}
  set -e

  if [[ ${mvn_exit} -ne 0 ]]; then
    rm -f "${log_file}"
    echo "Maven コマンドが失敗しました (exit=${mvn_exit})" >&2
    exit "${mvn_exit}"
  fi

  if grep -Eiq "${forbidden_regex}" "${log_file}"; then
    rm -f "${log_file}"
    echo "gwt:compile 出力に禁止パターンを検出しました: ${forbidden_regex}" >&2
    exit 1
  fi

  rm -f "${log_file}"
}

if [[ "${SKIP_BUILD}" != "true" ]]; then
  run_maven_with_forbidden_output_check "Ignored[[:space:]]+[0-9]+[[:space:]]+units[[:space:]]+with[[:space:]]+compilation errors" \
    -q -DskipTests "-Dgwt.skipCompilation=false" "-Dgwt.draftCompile=false" "-Dgwt.style=PRETTY" "-Dgwt.optimize=9" gwt:compile
  run_maven -q -DskipTests package
fi

if [[ ! -f "${SOURCE_WAR_PATH}" ]]; then
  echo "WAR が見つかりません: ${SOURCE_WAR_PATH}" >&2
  exit 1
fi

if [[ ! -d "${TOMCAT_WEBAPPS_DIR}" ]]; then
  echo "Tomcat webapps ディレクトリが見つかりません: ${TOMCAT_WEBAPPS_DIR}" >&2
  exit 1
fi

echo "Deploy: ${SOURCE_WAR_PATH} -> ${TOMCAT_WEBAPPS_DIR}/${DEPLOY_WAR_NAME}"
rm -rf "${APP_CONTEXT_DIR}"
rm -rf "${LEGACY_CONTEXT_DIR}"
rm -f "${LEGACY_WAR_PATH}"
install -m 0644 "${SOURCE_WAR_PATH}" "${TOMCAT_WEBAPPS_DIR}/${DEPLOY_WAR_NAME}"

echo "Restart service: ${SERVICE_NAME}"
systemctl restart "${SERVICE_NAME}"

echo "Done."
