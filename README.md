# MediSante+ Telemedecine Platform

Spring Boot + PostgreSQL demo application for a French telemedicine company.

Tech stack:
- Java 21 + Spring Boot (REST + static frontend)
- PostgreSQL
- Docker + docker-compose
- Kubernetes manifests (kind/minikube)
- GitHub Actions (build/test + push image)

Main endpoints:
- GET `/api/medisante/home` (content for the landing page with service visuals)
- GET `/api/medisante/teleconsultations` (list scheduled remote consultations)
- POST `/api/medisante/teleconsultations` (create a new remote consultation request)

## Docker Hub push (GitHub Actions)

Set these GitHub repository secrets:
- `DOCKERHUB_USERNAME`: your Docker Hub username
- `DOCKERHUB_TOKEN`: a Docker Hub access token (not your password)

Then push to `main` or `test`.
The workflow will publish tags such as:
- `docker.io/<DOCKERHUB_USERNAME>/notes-api:latest` (from default branch)
- `docker.io/<DOCKERHUB_USERNAME>/notes-api:main` or `:test`
- `docker.io/<DOCKERHUB_USERNAME>/notes-api:sha-<git-sha>`
