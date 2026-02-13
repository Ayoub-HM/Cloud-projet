# Deploiement EKS avec ALB Ingress (option 2)

Cette stack expose l'application avec **un seul ALB**:
- `/` vers `appointments-api`
- `/api/auth`, `/auth`, `/auth.js`, `/auth.css` vers `auth-api`

## Prerequis importants

1. AWS Load Balancer Controller installe dans le cluster EKS.
2. Les images ECR existent.
3. Les secrets DB sont renseignes.
4. Postgres est configure en stockage ephemere (`emptyDir`) pour un deploiement simple.
5. `metrics-server` est installe si vous voulez que les HPA CPU fonctionnent.

## Placeholders a remplacer

Dans les manifests:
- `__APPOINTMENTS_IMAGE__`
- `__AUTH_IMAGE__`
- `__SET_IN_CLUSTER__`
- `__APPOINTMENTS_BASE_URL__` (sera mis automatiquement par workflow apres creation ALB)

## Deploiement manuel (si besoin)

`kubectl create namespace demo-test`

`kubectl apply -k k8s/eks`

Recuperer le DNS ALB:
`kubectl get ingress app-ingress -n demo-test`

Puis mettre `AUTH_APP_BASE_URL` avec ce DNS et redemarrer `auth-api`.
