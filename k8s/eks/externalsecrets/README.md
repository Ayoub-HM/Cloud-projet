# External Secrets (Optionnel)

Ce dossier permet de brancher les secrets Kubernetes sur AWS Secrets Manager via External Secrets Operator (ESO).

## Prerequis

1. Installer ESO dans le cluster.
2. Creer un ServiceAccount IAM (IRSA) `external-secrets-sa` dans `external-secrets`.
3. Ajouter les secrets AWS:
   - `cloud-projet/appointments` avec `DB_USER`, `DB_PASSWORD`
   - `cloud-projet/auth` avec `DB_USER`, `DB_PASSWORD`, `AUTH_JWT_SECRET`

## Deploiement

`kubectl apply -k k8s/eks/externalsecrets`

## Notes

- Ces manifests sont optionnels et ne sont pas inclus dans `k8s/eks/kustomization.yaml` par defaut.
- Si vous activez ESO, vous pouvez supprimer les valeurs sensibles rendues par CI dans les secrets statiques.
