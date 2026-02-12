# Terraform EKS Stack

Ce dossier provisionne:
- VPC (subnets publics/prives + NAT)
- Cluster EKS + node group managed
- Provider OIDC (IRSA-ready)
- 2 repositories ECR (`appointments-api`, `auth-api`)

Note:
- Le workflow CI/CD deploie ensuite `test` et `main` dans des namespaces Kubernetes separes du meme cluster.

## Prerequis

- AWS CLI configure (`aws configure`)
- Terraform >= 1.6
- IAM user/role avec droits EKS, EC2, IAM, ECR, VPC

## Deploiement

1. Copier le fichier d'exemple:
`cp terraform.tfvars.example terraform.tfvars`

2. Adapter les valeurs dans `terraform.tfvars`.

3. Initialiser et appliquer:
`terraform init -backend-config="bucket=<S3_BUCKET>" -backend-config="key=cloud-projet/eks/terraform.tfstate" -backend-config="region=<AWS_REGION>" -backend-config="dynamodb_table=<DDB_TABLE>" -backend-config="encrypt=true"`
`terraform plan`
`terraform apply`

4. Configurer kubectl:
`aws eks update-kubeconfig --region <AWS_REGION> --name <CLUSTER_NAME>`

## Outputs utiles

- `appointments_ecr_repository_url`
- `auth_ecr_repository_url`
- `configure_kubectl_command`

## Backend Terraform (obligatoire pour CI)

La pipeline GitHub Actions utilise un backend distant S3 + DynamoDB:
- Bucket S3: `TF_STATE_BUCKET`
- Table DynamoDB (lock): `TF_LOCK_TABLE`
