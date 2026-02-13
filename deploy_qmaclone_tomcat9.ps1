param(
  [string]$ServiceName = "Tomcat9",
  [string]$TomcatBase = "",
  [string]$SourceWar = "",
  [switch]$SkipBuild,
  [int]$GwtLocalWorkers = 0,
  [bool]$GwtDraftCompile = $true
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Resolve-TomcatBasePath {
  param([string]$InputPath)

  if (-not [string]::IsNullOrWhiteSpace($InputPath)) {
    return $InputPath
  }

  if ($env:CATALINA_BASE -and (Test-Path -LiteralPath $env:CATALINA_BASE)) {
    return $env:CATALINA_BASE
  }

  $candidates = @(
    "C:\Program Files\Apache Software Foundation\Tomcat 9.0",
    "C:\Program Files (x86)\Apache Software Foundation\Tomcat 9.0"
  )

  foreach ($path in $candidates) {
    if (Test-Path -LiteralPath $path) {
      return $path
    }
  }

  throw "Tomcat base directory was not found. Specify -TomcatBase."
}

function Resolve-SourceWarPath {
  param([string]$InputPath)

  if (-not [string]::IsNullOrWhiteSpace($InputPath)) {
    if (-not (Test-Path -LiteralPath $InputPath)) {
      throw "WAR file was not found: $InputPath"
    }
    return (Resolve-Path -LiteralPath $InputPath).Path
  }

  $defaultWar = Join-Path -Path $PSScriptRoot -ChildPath "target\QMAClone-1.0-SNAPSHOT.war"
  if (-not (Test-Path -LiteralPath $defaultWar)) {
    throw "WAR file was not found. Specify -SourceWar or build first. Expected: $defaultWar"
  }
  return (Resolve-Path -LiteralPath $defaultWar).Path
}

function Resolve-MavenCommand {
  $candidates = @(
    "C:\msys64\ucrt64\bin\mvn.cmd",
    "C:\msys64\usr\bin\mvn.cmd",
    "mvn.cmd",
    "mvn"
  )

  foreach ($candidate in $candidates) {
    if ($candidate -match "[:\\]") {
      if (Test-Path -LiteralPath $candidate) {
        return (Resolve-Path -LiteralPath $candidate).Path
      }
      continue
    }

    $command = Get-Command -Name $candidate -ErrorAction SilentlyContinue
    if ($command) {
      return $command.Path
    }
  }

  throw "Maven command was not found. Install Maven or set PATH."
}

function Invoke-ExternalCommand {
  param(
    [string]$FilePath,
    [string[]]$Arguments
  )

  Write-Host "Run: $FilePath $($Arguments -join ' ')"
  & $FilePath @Arguments
  if ($LASTEXITCODE -ne 0) {
    throw "Command failed with exit code ${LASTEXITCODE}: $FilePath $($Arguments -join ' ')"
  }
}

function Test-IsAdministrator {
  $currentIdentity = [Security.Principal.WindowsIdentity]::GetCurrent()
  $principal = New-Object Security.Principal.WindowsPrincipal($currentIdentity)
  return $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

function Restart-ServiceWithElevation {
  param([string]$Name)

  $escapedName = $Name.Replace("'", "''")
  [string[]]$elevatedArguments = @(
    "-NoProfile",
    "-ExecutionPolicy",
    "Bypass",
    "-Command",
    "Restart-Service -Name '$escapedName' -Force"
  )

  try {
    $process = Start-Process -FilePath "powershell.exe" -ArgumentList $elevatedArguments -Verb RunAs -WindowStyle Minimized -Wait -PassThru
  } catch {
    throw "Failed to restart service with UAC elevation. Elevation may have been canceled."
  }

  if ($process.ExitCode -ne 0) {
    throw "Failed to restart service with UAC elevation. Exit code: $($process.ExitCode)"
  }
}

function Sync-GwtArtifacts {
  param(
    [string]$SourceDir,
    [string]$DestinationDir
  )

  if (-not (Test-Path -LiteralPath $SourceDir)) {
    throw "GWT output directory was not found: $SourceDir"
  }

  if (-not (Test-Path -LiteralPath $DestinationDir)) {
    New-Item -ItemType Directory -Path $DestinationDir -Force | Out-Null
  }

  Write-Host "Sync GWT artifacts: $SourceDir -> $DestinationDir"
  robocopy $SourceDir $DestinationDir /MIR /NFL /NDL /NJH /NJS /NP | Out-Null
  if ($LASTEXITCODE -gt 7) {
    throw "robocopy failed with exit code $LASTEXITCODE"
  }
}

function Resolve-GwtLocalWorkers {
  param([int]$RequestedWorkers)

  if ($RequestedWorkers -gt 0) {
    return $RequestedWorkers
  }

  $cpuCount = [Environment]::ProcessorCount
  if ($cpuCount -le 1) {
    return 1
  }

  return $cpuCount - 1
}

$workspaceRoot = $PSScriptRoot
Set-Location -LiteralPath $workspaceRoot

if (-not $SkipBuild) {
  $maven = Resolve-MavenCommand
  $resolvedGwtLocalWorkers = Resolve-GwtLocalWorkers -RequestedWorkers $GwtLocalWorkers
  $draftCompileValue = $GwtDraftCompile.ToString().ToLowerInvariant()

  Invoke-ExternalCommand -FilePath $maven -Arguments @("compile")
  Invoke-ExternalCommand -FilePath $maven -Arguments @(
    "-Dgwt.skipCompilation=false",
    "-Dgwt.localWorkers=$resolvedGwtLocalWorkers",
    "-Dgwt.draftCompile=$draftCompileValue",
    "gwt:compile"
  )

  $gwtOutputDir = Join-Path -Path $workspaceRoot -ChildPath "target\QMAClone-1.0-SNAPSHOT\tv.dyndns.kishibe.qmaclone.QMAClone"
  $gwtWebappDir = Join-Path -Path $workspaceRoot -ChildPath "src\main\webapp\tv.dyndns.kishibe.qmaclone.QMAClone"
  Sync-GwtArtifacts -SourceDir $gwtOutputDir -DestinationDir $gwtWebappDir

  Invoke-ExternalCommand -FilePath $maven -Arguments @("package", "-DskipTests")
}

$resolvedTomcatBase = Resolve-TomcatBasePath -InputPath $TomcatBase
$resolvedSourceWar = Resolve-SourceWarPath -InputPath $SourceWar
$webappsPath = Join-Path -Path $resolvedTomcatBase -ChildPath "webapps"

if (-not (Test-Path -LiteralPath $webappsPath)) {
  throw "Tomcat webapps directory was not found: $webappsPath"
}

$deployedWarPath = Join-Path -Path $webappsPath -ChildPath "QMAClone-1.0-SNAPSHOT.war"
$deployedDirPath = Join-Path -Path $webappsPath -ChildPath "QMAClone-1.0-SNAPSHOT"

Write-Host "TomcatBase: $resolvedTomcatBase"
Write-Host "Webapps:    $webappsPath"
Write-Host "Source WAR: $resolvedSourceWar"
Write-Host "Service:    $ServiceName"

if (Test-Path -LiteralPath $deployedWarPath) {
  Write-Host "Remove existing WAR: $deployedWarPath"
  Remove-Item -LiteralPath $deployedWarPath -Force
}

if (Test-Path -LiteralPath $deployedDirPath) {
  Write-Host "Remove exploded app dir: $deployedDirPath"
  Remove-Item -LiteralPath $deployedDirPath -Recurse -Force
}

Write-Host "Restart service: $ServiceName"
if (Test-IsAdministrator) {
  Restart-Service -Name $ServiceName -Force
} else {
  Write-Host "Run as non-admin user. Restart service with UAC elevation."
  Restart-ServiceWithElevation -Name $ServiceName
}

$deadline = (Get-Date).AddSeconds(60)
while ($true) {
  $service = Get-Service -Name $ServiceName
  if ($service.Status -eq [System.ServiceProcess.ServiceControllerStatus]::Running) {
    break
  }
  if ((Get-Date) -ge $deadline) {
    throw "Service did not reach Running state within timeout: $ServiceName"
  }
  Start-Sleep -Seconds 1
}

Write-Host "Deploy WAR to: $deployedWarPath"
Copy-Item -LiteralPath $resolvedSourceWar -Destination $deployedWarPath -Force

Write-Host "Done."
