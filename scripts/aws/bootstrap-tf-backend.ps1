param(
  [Parameter(Mandatory = $true)]
  [string]$Region,
  [Parameter(Mandatory = $true)]
  [string]$BucketName,
  [Parameter(Mandatory = $true)]
  [string]$LockTableName
)

$ErrorActionPreference = "Stop"

aws s3api create-bucket --bucket $BucketName --region $Region --create-bucket-configuration LocationConstraint=$Region 2>$null
aws s3api put-bucket-versioning --bucket $BucketName --versioning-configuration Status=Enabled
aws s3api put-bucket-encryption --bucket $BucketName --server-side-encryption-configuration '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'

aws dynamodb create-table `
  --table-name $LockTableName `
  --attribute-definitions AttributeName=LockID,AttributeType=S `
  --key-schema AttributeName=LockID,KeyType=HASH `
  --billing-mode PAY_PER_REQUEST `
  --region $Region 2>$null

Write-Output "Terraform backend ready:"
Write-Output "S3 bucket: $BucketName"
Write-Output "DynamoDB table: $LockTableName"
