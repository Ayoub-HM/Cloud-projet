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
`$env:POSTGRES_PASSWORD="change-me"; docker compose up -d`

Note:
- en local, `docker-compose.yml` force `JPA_DDL_AUTO=update` pour accelerer le demarrage.
- en dehors du mode local, les services utilisent Flyway + `ddl-auto=validate`.

Rebuild uniquement un service modifie:
`docker compose build api`
`docker compose build auth-service`

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
`kubectl apply -f k8s/13-api-hpa.yaml`
`kubectl apply -f k8s/14-auth-hpa.yaml`
`kubectl apply -f k8s/15-api-pdb.yaml`
`kubectl apply -f k8s/16-auth-pdb.yaml`

Haute disponibilite:
- `appointments-api` replica sur `3` pods
- `auth-api` replica sur `3` pods
- Postgres utilise un volume persistant (`PersistentVolumeClaim`).
- autoscaling active via HPA (CPU) pour `appointments-api` et `auth-api`.
- rolling updates + anti-affinite + PDB pour reduire les interruptions.

Prerequis HPA:
- `metrics-server` doit etre installe dans le cluster.

Important securite:
- les mots de passe ne sont plus hardcodes dans le code.
- definir les secrets avant de lancer (`POSTGRES_PASSWORD` en local, valeurs `__SET_IN_CLUSTER__` et `__AUTH_JWT_SECRET__` a remplacer en Kubernetes).

Observabilite/API:
- correlation ID (`X-Request-Id`) ajoute aux logs/reponses.
- OpenAPI disponible sur `/swagger-ui.html` et `/api-docs`.

## Migration AWS EKS + Terraform

Terraform infra:
- `infra/terraform/eks`

Manifestes Kubernetes cibles EKS:
- `k8s/eks`
- Option secrets manager: `k8s/eks/externalsecrets`

### Cible AWS
- Region: `eu-west-3` (Paris)
- Branches/environnements: `test` et `main`
- Exposition: Ingress ALB unique (option 2)

### Etapes rapides (manuel)

1. Provisionner EKS/VPC/ECR:
`cd infra/terraform/eks`
`cp terraform.tfvars.example terraform.tfvars`
`terraform init && terraform apply`

2. Configurer kubectl:
`aws eks update-kubeconfig --region <AWS_REGION> --name <CLUSTER_NAME>`

3. Installer AWS Load Balancer Controller:
`./scripts/aws/install-alb-controller.sh <CLUSTER_NAME> <AWS_REGION> <VPC_ID>`

4. Builder/pusher les images dans ECR:
`./scripts/aws/push-ecr.ps1 -Region <AWS_REGION> -Environment test -Tag latest`

5. Injecter les URLs ECR et les secrets dans `k8s/eks/*.yaml`, puis deploy:
`kubectl apply -k k8s/eks`

### CI/CD GitHub Actions vers AWS

Le workflow `.github/workflows/ci.yml` pousse sur ECR et deploie sur EKS pour les branches `test` et `main`.

Secrets GitHub a configurer:
- `AWS_ROLE_TO_ASSUME` (role IAM pour OIDC GitHub Actions)
- `EKS_CLUSTER_NAME`
- `TF_STATE_BUCKET` (bucket S3 pour l'etat Terraform)
- `TF_LOCK_TABLE` (table DynamoDB pour lock Terraform)
- `DB_PASSWORD_TEST`
- `DB_PASSWORD_MAIN`
- `AUTH_JWT_SECRET_TEST` (optionnel mais recommande)
- `AUTH_JWT_SECRET_MAIN` (optionnel mais recommande)

Astuce mot de passe fort:
- utilise au minimum 16 caracteres (majuscule, minuscule, chiffre, symbole).
