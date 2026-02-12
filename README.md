# MediSante+ Telemedecine Platform

Architecture microservices en 2 services:
- `appointments-service` (`app/`): gestion des rendez-vous (teleconsultations)
- `auth-service` (`auth-service/`): authentification (login + validation de token)

## Build Docker

Service rendez-vous:
`docker build -t appointments-api:local -f app/Dockerfile app`

Service auth:
`docker build -t auth-api:local -f auth-service/Dockerfile auth-service`

## Docker Compose

Lance Postgres + les 2 microservices:
`$env:POSTGRES_PASSWORD="change-me"; docker compose up --build`

## Kubernetes

Appliquer les manifests dans l'ordre:
`kubectl apply -f k8s/00-namespace.yaml`
`kubectl apply -f k8s/01-postgres-secret.yaml`
`kubectl apply -f k8s/02-postgres-pvc.yaml`
`kubectl apply -f k8s/03-postgres-deployment.yaml`
`kubectl apply -f k8s/04-postgres-service.yaml`
`kubectl apply -f k8s/05-api-configmap.yaml`
`kubectl apply -f k8s/06-api-secret.yaml`
`kubectl apply -f k8s/07-api-deployment.yaml`
`kubectl apply -f k8s/08-api-service.yaml`
`kubectl apply -f k8s/09-auth-configmap.yaml`
`kubectl apply -f k8s/10-auth-secret.yaml`
`kubectl apply -f k8s/11-auth-deployment.yaml`
`kubectl apply -f k8s/12-auth-service.yaml`

Haute disponibilite:
- `appointments-api` replica sur `3` pods
- `auth-api` replica sur `3` pods

Important securite:
- les mots de passe ne sont plus hardcodes dans le code.
- definir les secrets avant de lancer (`POSTGRES_PASSWORD` en local, valeurs `__SET_IN_CLUSTER__` a remplacer en Kubernetes).

## Migration AWS EKS + Terraform

Terraform infra:
- `infra/terraform/eks`

Manifestes Kubernetes cibles EKS:
- `k8s/eks`

### Etapes rapides

1. Provisionner EKS/VPC/ECR:
`cd infra/terraform/eks`
`cp terraform.tfvars.example terraform.tfvars`
`terraform init && terraform apply`

2. Configurer kubectl:
`aws eks update-kubeconfig --region <AWS_REGION> --name <CLUSTER_NAME>`

3. Builder/pusher les images dans ECR:
`./scripts/aws/push-ecr.ps1 -Region <AWS_REGION> -Environment test -Tag latest`

4. Injecter les URLs ECR et les secrets dans `k8s/eks/*.yaml`, puis deploy:
`kubectl apply -k k8s/eks`
