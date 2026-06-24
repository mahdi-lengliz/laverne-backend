param(
    [string]$VolumeName = "laverne_uploads_data",
    [string]$ProductsPath = "products",
    [int]$MaxDimension = 1600,
    [int]$Quality = 90,
    [string]$BackupDirectory = "."
)

$ErrorActionPreference = "Stop"

function Write-Step {
    param([string]$Message)
    Write-Host ""
    Write-Host "==> $Message" -ForegroundColor Cyan
}

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "Docker est introuvable. Installez Docker ou lancez Docker Desktop."
}

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$resolvedBackupDirectory = (Resolve-Path -Path $BackupDirectory).Path
$backupFileName = "uploads-backup-$timestamp.tar.gz"

Write-Step "Verification du volume Docker '$VolumeName'"
docker volume inspect $VolumeName *> $null

Write-Step "Sauvegarde du volume vers $backupFileName"
docker run --rm `
    -v "${VolumeName}:/images" `
    -v "${resolvedBackupDirectory}:/backup" `
    alpine:3.20 `
    sh -c "tar czf /backup/$backupFileName -C /images ."

Write-Step "Optimisation des images JPG/JPEG/PNG existantes"
$optimizeCommand = @'
set -e
apk add --no-cache imagemagick >/dev/null
magick_cmd=$(command -v magick || command -v convert)
if [ ! -d '/images/__PRODUCTS_PATH__' ]; then
  echo 'Dossier /images/__PRODUCTS_PATH__ introuvable.'
  exit 0
fi
before=$(du -sb /images/__PRODUCTS_PATH__ | awk '{print $1}')
find /images/__PRODUCTS_PATH__ -type f \( -iname '*.jpg' -o -iname '*.jpeg' -o -iname '*.png' \) -print | while IFS= read -r file; do
  tmp="$file.optimized"
  "$magick_cmd" "$file" -auto-orient -strip -resize '__MAX_DIMENSION__x__MAX_DIMENSION__>' -quality __QUALITY__ "$tmp"
  original_size=$(stat -c%s "$file")
  optimized_size=$(stat -c%s "$tmp")
  if [ "$optimized_size" -lt "$original_size" ]; then
    mv "$tmp" "$file"
    echo "Optimized: $file ($original_size -> $optimized_size bytes)"
  else
    rm "$tmp"
    echo "Kept original: $file"
  fi
done
after=$(du -sb /images/__PRODUCTS_PATH__ | awk '{print $1}')
echo "Total size: $before -> $after bytes"
'@

$optimizeCommand = $optimizeCommand.
    Replace("__PRODUCTS_PATH__", $ProductsPath).
    Replace("__MAX_DIMENSION__", [string]$MaxDimension).
    Replace("__QUALITY__", [string]$Quality)

docker run --rm `
    -v "${VolumeName}:/images" `
    alpine:3.20 `
    sh -c $optimizeCommand

Write-Step "Termine"
Write-Host "Sauvegarde creee : $resolvedBackupDirectory\$backupFileName" -ForegroundColor Green
