# LAVERNE Production Deployment

## Docker Hub images

- `mahdi-lengliz/laverne-backend:latest`
- `mahdi-lengliz/laverne-frontend:latest`

## GitHub Actions secrets

Add these secrets in both GitHub repositories:

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`

Use a Docker Hub access token, not your Docker Hub password.

## Server setup

Copy `.env.example` to `.env` and replace every `change_me` value.

```bash
cp .env.example .env
nano .env
```

Start or update the stack:

```bash
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d
```

View logs:

```bash
docker compose -f docker-compose.prod.yml logs -f
```

## Persistent data

The compose file keeps these Docker volumes:

- `postgres_data` for the database
- `uploads_data` for uploaded product images

## Optimize existing uploaded images

After deployment, existing product images can be optimized in the Docker uploads volume.
The script creates a backup before changing files.

```powershell
.\scripts\optimize-existing-uploads.ps1
```

If your Docker Compose project generated another volume name, pass it explicitly:

```powershell
.\scripts\optimize-existing-uploads.ps1 -VolumeName laverne_uploads_data
```

The script optimizes JPG, JPEG, and PNG files, keeps the same filenames, and skips WebP/GIF files.
