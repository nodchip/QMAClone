param(
    [Parameter(Mandatory = $true)]
    [string]$HostName,

    [Parameter(Mandatory = $true)]
    [string]$UserName,

    [int]$Port = 22,

    [string]$IdentityFile = ""
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Base directory for local copies.
$LocalRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

# Fixed list of remote files to mirror locally.
$Targets = @(
    @{ Remote = "/etc/nginx/nginx.conf"; Local = "nginx/nginx.conf" },
    @{ Remote = "/etc/nginx/sites-enabled/default"; Local = "nginx/sites-enabled/default" },
    @{ Remote = "/etc/tomcat10/server.xml"; Local = "tomcat10/server.xml" },
    @{ Remote = "/etc/logrotate.d/tomcat10"; Local = "logrotate/tomcat10" }
)

function Invoke-ScpDownload {
    param(
        [Parameter(Mandatory = $true)][string]$RemotePath,
        [Parameter(Mandatory = $true)][string]$LocalPath
    )

    $args = @("-P", "$Port")
    if (-not [string]::IsNullOrWhiteSpace($IdentityFile)) {
        $args += @("-i", $IdentityFile)
    }
    $args += @("$UserName@$HostName`:$RemotePath", $LocalPath)

    Write-Host "download: $RemotePath -> $LocalPath"
    & scp.exe @args
}

foreach ($t in $Targets) {
    $destination = Join-Path $LocalRoot $t.Local
    $destinationDir = Split-Path -Parent $destination
    if (-not (Test-Path $destinationDir)) {
        New-Item -ItemType Directory -Path $destinationDir -Force | Out-Null
    }
    Invoke-ScpDownload -RemotePath $t.Remote -LocalPath $destination
}

Write-Host "Completed: downloaded server configuration files."
