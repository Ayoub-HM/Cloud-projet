# Deploiement applicatif sur EKS

## 1) Remplacer les images ECR

Dans `07-api-deployment.yaml` et `11-auth-deployment.yaml`, remplacer:
- `__APPOINTMENTS_IMAGE__`
- `__AUTH_IMAGE__`

Exemple:
- `<ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/cloud-projet-test/appointments-api:latest`
- `<ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/cloud-projet-test/auth-api:latest`

## 2) Mettre le mot de passe DB

Mettre une vraie valeur dans:
- `01-postgres-secret.yaml` (`POSTGRES_PASSWORD`)
- `06-api-secret.yaml` (`DB_PASSWORD`)
- `10-auth-secret.yaml` (`DB_PASSWORD`)

## 3) Appliquer les manifests

`kubectl apply -k k8s/eks`

## 4) Recuperer les URLs externes

`kubectl get svc -n demo`

Tu obtiendras 2 LoadBalancers:
- `appointments-api`
- `auth-api`

## 5) Configurer l'URL de redirection auth

Mettre dans `09-auth-configmap.yaml`:
- `AUTH_APP_BASE_URL: http://<EXTERNAL_DNS_APPOINTMENTS>`

Puis reappliquer:
- `kubectl apply -f k8s/eks/09-auth-configmap.yaml`
- `kubectl rollout restart deployment/auth-api -n demo`
