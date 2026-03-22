param(
    [Parameter(Mandatory = $true)]
    [string]$HostName,

    [Parameter(Mandatory = $true)]
    [string]$UserName,

    [int]$Port = 22,

    [string]$IdentityFile = "",

    [switch]$ReloadServices
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Base directory for local files copied to the server.
$LocalRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

$LocalNginxConf = Join-Path $LocalRoot "nginx/nginx.conf"
$LocalNginxDefault = Join-Path $LocalRoot "nginx/sites-enabled/default"
$LocalTomcatServerXml = Join-Path $LocalRoot "tomcat10/server.xml"
$LocalTomcatLogrotate = Join-Path $LocalRoot "logrotate/tomcat10"

$requiredFiles = @($LocalNginxConf, $LocalNginxDefault, $LocalTomcatServerXml, $LocalTomcatLogrotate)
foreach ($f in $requiredFiles) {
    if (-not (Test-Path $f)) {
        throw "Required file is missing: $f"
    }
}

function Invoke-ScpUpload {
    param(
        [Parameter(Mandatory = $true)][string]$LocalPath,
        [Parameter(Mandatory = $true)][string]$RemotePath
    )

    $args = @("-P", "$Port")
    if (-not [string]::IsNullOrWhiteSpace($IdentityFile)) {
        $args += @("-i", $IdentityFile)
    }
    $args += @($LocalPath, "$UserName@$HostName`:$RemotePath")

    Write-Host "upload: $LocalPath -> $RemotePath"
    & scp.exe @args
}

function Invoke-Ssh {
    param(
        [Parameter(Mandatory = $true)][string]$CommandText
    )

    $args = @("-p", "$Port")
    if (-not [string]::IsNullOrWhiteSpace($IdentityFile)) {
        $args += @("-i", $IdentityFile)
    }
    $args += @("$UserName@$HostName", $CommandText)
    & ssh.exe @args
}

# Create a staging directory on the server.
Invoke-Ssh -CommandText @"
set -e
mkdir -p ~/qmaclone-config-staging/nginx/sites-enabled
mkdir -p ~/qmaclone-config-staging/tomcat10
mkdir -p ~/qmaclone-config-staging/logrotate
"@

# Upload files into the staging directory.
Invoke-ScpUpload -LocalPath $LocalNginxConf -RemotePath "~/qmaclone-config-staging/nginx/nginx.conf"
Invoke-ScpUpload -LocalPath $LocalNginxDefault -RemotePath "~/qmaclone-config-staging/nginx/sites-enabled/default"
Invoke-ScpUpload -LocalPath $LocalTomcatServerXml -RemotePath "~/qmaclone-config-staging/tomcat10/server.xml"
Invoke-ScpUpload -LocalPath $LocalTomcatLogrotate -RemotePath "~/qmaclone-config-staging/logrotate/tomcat10"

# Backup current files, install the new ones, and validate nginx/logrotate.
Invoke-Ssh -CommandText @"
set -e
timestamp=$(date +%Y%m%d_%H%M%S)
backup_dir=~/qmaclone-config-backup/$timestamp
mkdir -p "$backup_dir/nginx/sites-enabled"
mkdir -p "$backup_dir/tomcat10"
mkdir -p "$backup_dir/logrotate"

sudo cp /etc/nginx/nginx.conf "$backup_dir/nginx/nginx.conf"
sudo cp /etc/nginx/sites-enabled/default "$backup_dir/nginx/sites-enabled/default"
sudo cp /etc/tomcat10/server.xml "$backup_dir/tomcat10/server.xml"
sudo cp /etc/logrotate.d/tomcat10 "$backup_dir/logrotate/tomcat10"

sudo install -m 644 ~/qmaclone-config-staging/nginx/nginx.conf /etc/nginx/nginx.conf
sudo install -m 644 ~/qmaclone-config-staging/nginx/sites-enabled/default /etc/nginx/sites-enabled/default
sudo install -m 644 ~/qmaclone-config-staging/tomcat10/server.xml /etc/tomcat10/server.xml
sudo install -m 644 ~/qmaclone-config-staging/logrotate/tomcat10 /etc/logrotate.d/tomcat10

sudo nginx -t
sudo logrotate --debug /etc/logrotate.conf >/tmp/qmaclone-logrotate-debug.txt
echo "backup: $backup_dir"
"@

if ($ReloadServices) {
    Invoke-Ssh -CommandText @"
set -e
sudo systemctl reload nginx
sudo systemctl restart tomcat10
"@
    Write-Host "Completed: configuration applied, nginx reloaded, tomcat10 restarted."
} else {
    Write-Host "Completed: configuration files were applied without service restarts."
    Write-Host "Run this later if needed: sudo systemctl reload nginx && sudo systemctl restart tomcat10"
}
