param(
  [Parameter(Mandatory = $true)]
  [string]$Region,
  [Parameter(Mandatory = $false)]
  [string]$Environment = "test",
  [Parameter(Mandatory = $false)]
  [string]$Tag = "latest"
)

$ErrorActionPreference = "Stop"

$AccountId = (aws sts get-caller-identity --query Account --output text)
if (-not $AccountId) {
  throw "Unable to resolve AWS account ID."
}

$Prefix = "$AccountId.dkr.ecr.$Region.amazonaws.com/cloud-projet-$Environment"
$AppointmentsRepo = "$Prefix/appointments-api"
$AuthRepo = "$Prefix/auth-api"

aws ecr get-login-password --region $Region | docker login --username AWS --password-stdin "$AccountId.dkr.ecr.$Region.amazonaws.com"

docker build -t "${AppointmentsRepo}:${Tag}" -f app/Dockerfile app
docker push "${AppointmentsRepo}:${Tag}"

docker build -t "${AuthRepo}:${Tag}" -f auth-service/Dockerfile auth-service
docker push "${AuthRepo}:${Tag}"

Write-Output "Appointments image: ${AppointmentsRepo}:${Tag}"
Write-Output "Auth image: ${AuthRepo}:${Tag}"
