[CmdletBinding()]
param(
  [Parameter(ValueFromRemainingArguments = $true)]
  [object[]]$RemainingArgs
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Write-Warning "deploy_qmaclone_tomcat9.ps1 は互換ラッパーです。deploy_qmaclone_tomcat10.ps1 を使用してください。"

& (Join-Path -Path $PSScriptRoot -ChildPath "deploy_qmaclone_tomcat10.ps1") @RemainingArgs
exit $LASTEXITCODE
