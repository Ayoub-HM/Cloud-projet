# DevOps Demo POC (Notes API)

Minimal project for DevOps beginners:
- Java 17 + Spring Boot (REST)
- PostgreSQL
- Docker + docker-compose
- Kubernetes manifests (kind/minikube)
- GitHub Actions (build/test + push image)

Endpoints:
- GET  /api/notes
- GET  /api/notes/{id}
- POST /api/notes
- DELETE /api/notes/{id}

## Docker Hub push (GitHub Actions)

Set these GitHub repository secrets:
- `DOCKERHUB_USERNAME`: your Docker Hub username
- `DOCKERHUB_TOKEN`: a Docker Hub access token (not your password)

Then push to `main`.
The workflow will publish:
- `docker.io/<DOCKERHUB_USERNAME>/notes-api:latest`
- `docker.io/<DOCKERHUB_USERNAME>/notes-api:<git-sha>`
