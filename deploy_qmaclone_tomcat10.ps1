param(
  [string]$ServiceName = "Tomcat10",
  [string]$TomcatBase = "",
  [string]$SourceWar = "",
  [switch]$SkipBuild,
  [int]$GwtLocalWorkers = 0,
  [bool]$GwtDraftCompile = $true,
  [switch]$ReleaseBuild,
  [string]$HostName = "localhost",
  [int]$StartupWaitTimeoutSeconds = 300
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
    "C:\ProgramData\Tomcat10",
    "C:\Program Files\Apache Software Foundation\Tomcat 10.1",
    "C:\Program Files (x86)\Apache Software Foundation\Tomcat 10.1"
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
    "-NoLogo",
    "-NoProfile",
    "-NonInteractive",
    "-ExecutionPolicy",
    "Bypass",
    "-Command",
    "Restart-Service -Name '$escapedName' -Force"
  )

  try {
    # Prefer hidden launch and fall back to minimized window if needed.
    $process = Start-Process -FilePath "powershell.exe" -ArgumentList $elevatedArguments -Verb RunAs -WindowStyle Hidden -Wait -PassThru
  } catch {
    try {
      $process = Start-Process -FilePath "powershell.exe" -ArgumentList $elevatedArguments -Verb RunAs -WindowStyle Minimized -Wait -PassThru
    } catch {
      throw "Failed to restart service with UAC elevation. Elevation may have been canceled."
    }
  }

  if ($process.ExitCode -ne 0) {
    throw "Failed to restart service with UAC elevation. Exit code: $($process.ExitCode)"
  }
}

function Remove-ItemWithElevationFallback {
  param(
    [string]$Path,
    [bool]$Recurse = $false
  )

  if (-not (Test-Path -LiteralPath $Path)) {
    return
  }

  try {
    if ($Recurse) {
      Remove-Item -LiteralPath $Path -Recurse -Force -ErrorAction Stop
    } else {
      Remove-Item -LiteralPath $Path -Force -ErrorAction Stop
    }
    return
  } catch {
    $exception = $_.Exception
    $isUnauthorized = $exception -is [System.UnauthorizedAccessException]
    $isAccessDeniedMessage = $false
    if ($exception -and $exception.Message) {
      $isAccessDeniedMessage = $exception.Message -match "Access to the path .* is denied"
    }

    if ((Test-IsAdministrator) -or (-not ($isUnauthorized -or $isAccessDeniedMessage))) {
      throw
    }
  }

  Write-Warning "Access denied while removing path. Retry with UAC elevation: $Path"
  $escapedPath = $Path.Replace("'", "''")
  $recurseOption = if ($Recurse) { "-Recurse" } else { "" }
  $removeCommand = "Remove-Item -LiteralPath '$escapedPath' -Force $recurseOption"
  [string[]]$elevatedArguments = @(
    "-NoLogo",
    "-NoProfile",
    "-NonInteractive",
    "-ExecutionPolicy",
    "Bypass",
    "-Command",
    $removeCommand
  )

  try {
    # Prefer hidden launch and fall back to minimized window if needed.
    $process = Start-Process -FilePath "powershell.exe" -ArgumentList $elevatedArguments -Verb RunAs -WindowStyle Hidden -Wait -PassThru
  } catch {
    try {
      $process = Start-Process -FilePath "powershell.exe" -ArgumentList $elevatedArguments -Verb RunAs -WindowStyle Minimized -Wait -PassThru
    } catch {
      throw "Failed to remove path with UAC elevation. Elevation may have been canceled: $Path"
    }
  }

  # Treat as success if the target is already gone, even when elevated process returned non-zero.
  if ($process.ExitCode -ne 0 -and (Test-Path -LiteralPath $Path)) {
    throw "Failed to remove path with UAC elevation. Exit code: $($process.ExitCode) path=$Path"
  }

  if (Test-Path -LiteralPath $Path) {
    throw "Failed to remove path even after UAC elevation: $Path"
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

function Resolve-TomcatHttpPort {
  param([string]$TomcatBasePath)

  $serverXmlPath = Join-Path -Path $TomcatBasePath -ChildPath "conf\server.xml"
  if (-not (Test-Path -LiteralPath $serverXmlPath)) {
    return 8080
  }

  try {
    [xml]$serverXml = Get-Content -LiteralPath $serverXmlPath -Raw
    $connectorNodes = $serverXml.SelectNodes("//Connector")
    foreach ($connector in $connectorNodes) {
      $protocol = [string]$connector.protocol
      $port = [string]$connector.port
      if ([string]::IsNullOrWhiteSpace($port)) {
        continue
      }
      if ($protocol -match "AJP") {
        continue
      }
      return [int]$port
    }
  } catch {
    Write-Warning "Failed to parse server.xml. Use default HTTP port 8080."
  }

  return 8080
}

function Get-HttpStatusCode {
  param(
    [string]$Uri,
    [string]$Method = "GET"
  )

  try {
    $response = Invoke-WebRequest -Uri $Uri -Method $Method -UseBasicParsing -TimeoutSec 10
    return [int]$response.StatusCode
  } catch {
    # Exception type differs by PowerShell/.NET version. Avoid assuming .Response exists.
    $ex = $_.Exception
    if ($ex -and $ex.PSObject.Properties.Match('Response').Count -gt 0) {
      $resp = $ex.Response
      if ($resp -and $resp.PSObject.Properties.Match('StatusCode').Count -gt 0) {
        try { return [int]$resp.StatusCode } catch { }
        try { return [int]$resp.StatusCode.value__ } catch { }
      }
    }
    if ($ex -and $ex.PSObject.Properties.Match('StatusCode').Count -gt 0 -and $ex.StatusCode) {
      try { return [int]$ex.StatusCode } catch { }
    }
    return -1
  }
}

function Wait-ForHttpStatusNot404 {
  param(
    [string]$Uri,
    [int]$TimeoutSeconds
  )

  $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
  while ($true) {
    $statusCode = Get-HttpStatusCode -Uri $Uri
    if ($statusCode -ne 404 -and $statusCode -gt 0) {
      Write-Host "Page became available: $Uri (HTTP $statusCode)"
      return
    }

    if ((Get-Date) -ge $deadline) {
      throw "Timed out waiting for non-404 response: $Uri"
    }
    Start-Sleep -Seconds 1
  }
}

function Wait-ForRpcServletReady {
  param(
    [string]$Uri,
    [int]$TimeoutSeconds
  )

  $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
  while ($true) {
    $statusCode = Get-HttpStatusCode -Uri $Uri
    if ($statusCode -in @(200, 204, 302, 401, 403, 405)) {
      Write-Host "RPC endpoint is ready: $Uri (HTTP $statusCode)"
      return
    }

    if ((Get-Date) -ge $deadline) {
      throw "Timed out waiting for RPC endpoint readiness: $Uri (last status: $statusCode)"
    }
    Start-Sleep -Seconds 1
  }
}

function Wait-ForServiceWarmup {
  param(
    [string]$Uri,
    [int]$TimeoutSeconds
  )

  $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
  while ($true) {
    $statusCode = Get-HttpStatusCode -Uri $Uri
    if ($statusCode -eq 200) {
      Write-Host "Service warmup completed: $Uri (HTTP $statusCode)"
      return
    }

    if ((Get-Date) -ge $deadline) {
      throw "Timed out waiting for service warmup: $Uri (last status: $statusCode)"
    }
    Start-Sleep -Seconds 1
  }
}

$workspaceRoot = $PSScriptRoot
Set-Location -LiteralPath $workspaceRoot

if (-not $SkipBuild) {
  $maven = Resolve-MavenCommand
  $resolvedGwtLocalWorkers = Resolve-GwtLocalWorkers -RequestedWorkers $GwtLocalWorkers
  $effectiveGwtDraftCompile = if ($ReleaseBuild) { $false } else { $GwtDraftCompile }
  $draftCompileValue = $effectiveGwtDraftCompile.ToString().ToLowerInvariant()

  Invoke-ExternalCommand -FilePath $maven -Arguments @("compile")
  Invoke-ExternalCommand -FilePath $maven -Arguments @(
    "-Dgwt.skipCompilation=false",
    "-Dgwt.localWorkers=$resolvedGwtLocalWorkers",
    "-Dgwt.draftCompile=$draftCompileValue",
    "-Dgwt.style=PRETTY",
    "-Dgwt.optimize=9",
    "gwt:compile"
  )

  $gwtOutputDir = Join-Path -Path $workspaceRoot -ChildPath "target\QMAClone-1.0-SNAPSHOT\tv.dyndns.kishibe.qmaclone.QMAClone"
  $gwtWebappDir = Join-Path -Path $workspaceRoot -ChildPath "src\main\webapp\tv.dyndns.kishibe.qmaclone.QMAClone"
  Sync-GwtArtifacts -SourceDir $gwtOutputDir -DestinationDir $gwtWebappDir

  Invoke-ExternalCommand -FilePath $maven -Arguments @("package", "-DskipTests")
}

$resolvedTomcatBase = Resolve-TomcatBasePath -InputPath $TomcatBase
$resolvedSourceWar = Resolve-SourceWarPath -InputPath $SourceWar
$resolvedTomcatHttpPort = Resolve-TomcatHttpPort -TomcatBasePath $resolvedTomcatBase
$webappsPath = Join-Path -Path $resolvedTomcatBase -ChildPath "webapps"

if (-not (Test-Path -LiteralPath $webappsPath)) {
  throw "Tomcat webapps directory was not found: $webappsPath"
}

$webAppName = "QMAClone"
$deployedWarPath = Join-Path -Path $webappsPath -ChildPath "$webAppName.war"
$deployedDirPath = Join-Path -Path $webappsPath -ChildPath $webAppName
$legacyWarPath = Join-Path -Path $webappsPath -ChildPath "QMAClone-1.0-SNAPSHOT.war"
$legacyDirPath = Join-Path -Path $webappsPath -ChildPath "QMAClone-1.0-SNAPSHOT"

Write-Host "TomcatBase: $resolvedTomcatBase"
Write-Host "Webapps:    $webappsPath"
Write-Host "Source WAR: $resolvedSourceWar"
Write-Host "Service:    $ServiceName"
Write-Host "Host:       $HostName"
Write-Host "HTTP Port:  $resolvedTomcatHttpPort"

if (Test-Path -LiteralPath $deployedWarPath) {
  Write-Host "Remove existing WAR: $deployedWarPath"
  Remove-ItemWithElevationFallback -Path $deployedWarPath
}

if (Test-Path -LiteralPath $deployedDirPath) {
  Write-Host "Remove exploded app dir: $deployedDirPath"
  Remove-ItemWithElevationFallback -Path $deployedDirPath -Recurse $true
}

if (Test-Path -LiteralPath $legacyWarPath) {
  Write-Host "Remove legacy WAR: $legacyWarPath"
  Remove-ItemWithElevationFallback -Path $legacyWarPath
}

if (Test-Path -LiteralPath $legacyDirPath) {
  Write-Host "Remove legacy exploded app dir: $legacyDirPath"
  Remove-ItemWithElevationFallback -Path $legacyDirPath -Recurse $true
}

Write-Host "Restart service: $ServiceName"
$restartPerformed = $false
if (Test-IsAdministrator) {
  Restart-Service -Name $ServiceName -Force
  $restartPerformed = $true
} else {
  Write-Host "Run as non-admin user. Restart service with UAC elevation."
  try {
    Restart-ServiceWithElevation -Name $ServiceName
    $restartPerformed = $true
  } catch {
    # Continue WAR deployment when service restart elevation fails but service is still running.
    $service = Get-Service -Name $ServiceName -ErrorAction SilentlyContinue
    if (-not $service -or $service.Status -ne [System.ServiceProcess.ServiceControllerStatus]::Running) {
      throw
    }
    Write-Warning "Service restart with UAC elevation failed, but service is running. Continue deployment without restart."
  }
}

if ($restartPerformed) {
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
}

Write-Host "Deploy WAR to: $deployedWarPath"
Copy-Item -LiteralPath $resolvedSourceWar -Destination $deployedWarPath -Force

$contextPath = "/$webAppName"
$baseUri = "http://$($HostName):$resolvedTomcatHttpPort$contextPath/"
$rpcUri = "http://$($HostName):$resolvedTomcatHttpPort$contextPath/tv.dyndns.kishibe.qmaclone.QMAClone/service"
$warmupUri = "${rpcUri}?warmup=1"

Write-Host "Wait until page stops returning 404: $baseUri"
Wait-ForHttpStatusNot404 -Uri $baseUri -TimeoutSeconds $StartupWaitTimeoutSeconds

Write-Host "Wait until RPC servlet is ready: $rpcUri"
Wait-ForRpcServletReady -Uri $rpcUri -TimeoutSeconds $StartupWaitTimeoutSeconds

Write-Host "Run service warmup: $warmupUri"
Wait-ForServiceWarmup -Uri $warmupUri -TimeoutSeconds $StartupWaitTimeoutSeconds

Write-Host "Done. Deployment is complete."
