$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$dbPropsPath = Join-Path $repoRoot "backend\src\main\resources\db.properties"
$baseSqlPath = Join-Path $repoRoot "database\defence_gis.sql"
$migrationPaths = @(
    (Join-Path $repoRoot "database\migrations\V002__schema_fixes_and_geofencing.sql"),
    (Join-Path $repoRoot "database\migrations\V003__bcrypt_passwords_and_seed_data.sql"),
    (Join-Path $repoRoot "database\migrations\V004__demo_data_expansion.sql"),
    (Join-Path $repoRoot "database\migrations\V005__add_missing_geofence_fields.sql")
)
$psqlPath = "C:\Program Files\PostgreSQL\18\bin\psql.exe"

function Invoke-Psql {
    param(
        [string[]]$Arguments
    )

    & $psqlPath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "psql failed with exit code ${LASTEXITCODE}: $($Arguments -join ' ')"
    }
}

if (!(Test-Path $psqlPath)) {
    throw "psql.exe not found at $psqlPath"
}

if (!(Test-Path $dbPropsPath)) {
    throw "db.properties not found at $dbPropsPath"
}

$props = @{}
Get-Content $dbPropsPath | ForEach-Object {
    if ($_ -match '^\s*#' -or $_ -notmatch '=') { return }
    $parts = $_ -split '=', 2
    $props[$parts[0].Trim()] = $parts[1].Trim()
}

$dbUrl = $props["db.url"]
$dbUser = $props["db.username"]
$dbPassword = $props["db.password"]

if ([string]::IsNullOrWhiteSpace($dbUrl) -or
    [string]::IsNullOrWhiteSpace($dbUser) -or
    $null -eq $dbPassword) {
    throw "db.properties is missing db.url, db.username, or db.password"
}

$jdbcPattern = '^jdbc:postgresql://(?<host>[^:/]+)(:(?<port>\d+))?/(?<db>[^?]+)$'
if ($dbUrl -notmatch $jdbcPattern) {
    throw "Unsupported db.url format: $dbUrl"
}

$dbHost = $Matches["host"]
$dbPort = if ($Matches["port"]) { $Matches["port"] } else { "5432" }
$dbName = $Matches["db"]

$env:PGPASSWORD = $dbPassword

Invoke-Psql @("-h", $dbHost, "-p", $dbPort, "-U", $dbUser, "-d", "postgres", "-v", "ON_ERROR_STOP=1", "-c", "SELECT 1;")
Invoke-Psql @("-h", $dbHost, "-p", $dbPort, "-U", $dbUser, "-d", "postgres", "-v", "ON_ERROR_STOP=1", "-c", "DROP DATABASE IF EXISTS $dbName WITH (FORCE);")
Invoke-Psql @("-h", $dbHost, "-p", $dbPort, "-U", $dbUser, "-d", "postgres", "-v", "ON_ERROR_STOP=1", "-c", "CREATE DATABASE $dbName;")
Invoke-Psql @("-h", $dbHost, "-p", $dbPort, "-U", $dbUser, "-d", $dbName, "-v", "ON_ERROR_STOP=1", "-c", "CREATE EXTENSION IF NOT EXISTS postgis;")
Invoke-Psql @("-h", $dbHost, "-p", $dbPort, "-U", $dbUser, "-d", $dbName, "-v", "ON_ERROR_STOP=1", "-f", $baseSqlPath)

foreach ($migrationPath in $migrationPaths) {
    Invoke-Psql @("-h", $dbHost, "-p", $dbPort, "-U", $dbUser, "-d", $dbName, "-v", "ON_ERROR_STOP=1", "-f", $migrationPath)
}

Write-Host "Database setup completed for $dbName on ${dbHost}:$dbPort"
