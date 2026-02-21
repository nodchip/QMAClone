#!/usr/bin/env bash
set -euo pipefail

# nighthawk 向け Tomcat10 配備スクリプト
# 本番ビルド方針: PRETTY / 非ドラフト / optimize=9

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"

WAR_NAME="QMAClone-1.0-SNAPSHOT.war"
WAR_PATH="${REPO_ROOT}/target/${WAR_NAME}"

TOMCAT_WEBAPPS_DIR="${TOMCAT_WEBAPPS_DIR:-/var/lib/tomcat10/webapps}"
APP_CONTEXT_DIR="${TOMCAT_WEBAPPS_DIR}/QMAClone-1.0-SNAPSHOT"
SERVICE_NAME="${SERVICE_NAME:-tomcat10}"
SKIP_BUILD="${SKIP_BUILD:-false}"

run_maven() {
  local -a args=("$@")
  echo "Run: mvn ${args[*]}"
  (cd "${REPO_ROOT}" && mvn "${args[@]}")
}

if [[ "${SKIP_BUILD}" != "true" ]]; then
  run_maven -q -DskipTests "-Dgwt.skipCompilation=false" "-Dgwt.draftCompile=false" "-Dgwt.style=PRETTY" "-Dgwt.optimize=9" gwt:compile
  run_maven -q -DskipTests package
fi

if [[ ! -f "${WAR_PATH}" ]]; then
  echo "WAR が見つかりません: ${WAR_PATH}" >&2
  exit 1
fi

if [[ ! -d "${TOMCAT_WEBAPPS_DIR}" ]]; then
  echo "Tomcat webapps ディレクトリが見つかりません: ${TOMCAT_WEBAPPS_DIR}" >&2
  exit 1
fi

echo "Deploy: ${WAR_PATH} -> ${TOMCAT_WEBAPPS_DIR}"
rm -rf "${APP_CONTEXT_DIR}"
install -m 0644 "${WAR_PATH}" "${TOMCAT_WEBAPPS_DIR}/${WAR_NAME}"

echo "Restart service: ${SERVICE_NAME}"
systemctl restart "${SERVICE_NAME}"

echo "Done."
