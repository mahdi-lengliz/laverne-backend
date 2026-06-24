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
$pythonScriptName = "optimize-uploads-$timestamp.py"
$pythonScriptPath = Join-Path $resolvedBackupDirectory $pythonScriptName

Write-Step "Verification du volume Docker '$VolumeName'"
docker volume inspect $VolumeName *> $null

Write-Step "Sauvegarde du volume vers $backupFileName"
docker run --rm `
    -v "${VolumeName}:/images" `
    -v "${resolvedBackupDirectory}:/backup" `
    alpine:3.20 `
    sh -c "tar czf /backup/$backupFileName -C /images ."

Write-Step "Optimisation des images JPG/JPEG/PNG existantes"
$pythonScript = @'
from pathlib import Path
from PIL import Image, ImageOps
import os
import sys

products_path = Path("/images") / "__PRODUCTS_PATH__"
max_dimension = __MAX_DIMENSION__
quality = __QUALITY__
extensions = {".jpg", ".jpeg", ".png"}

if not products_path.exists():
    print(f"Dossier {products_path} introuvable.")
    sys.exit(0)

def directory_size(path: Path) -> int:
    return sum(file.stat().st_size for file in path.rglob("*") if file.is_file())

def resized_image(image: Image.Image) -> Image.Image:
    image = ImageOps.exif_transpose(image)
    width, height = image.size
    largest = max(width, height)
    if largest <= max_dimension:
        return image.copy()
    ratio = max_dimension / largest
    new_size = (max(1, round(width * ratio)), max(1, round(height * ratio)))
    return image.resize(new_size, Image.Resampling.LANCZOS)

def save_optimized(image: Image.Image, source: Path, target: Path) -> None:
    suffix = source.suffix.lower()
    if suffix in {".jpg", ".jpeg"}:
        if image.mode in ("RGBA", "LA", "P"):
            background = Image.new("RGB", image.size, "white")
            if image.mode == "P":
                image = image.convert("RGBA")
            background.paste(image, mask=image.getchannel("A") if image.mode in ("RGBA", "LA") else None)
            image = background
        else:
            image = image.convert("RGB")
        image.save(target, "JPEG", quality=quality, optimize=True, progressive=True)
        return
    image.save(target, "PNG", optimize=True, compress_level=9)

before = directory_size(products_path)
processed = 0

for source in products_path.rglob("*"):
    if not source.is_file() or source.suffix.lower() not in extensions:
        continue
    tmp = source.with_name(source.name + ".optimized")
    try:
        with Image.open(source) as image:
            optimized = resized_image(image)
            save_optimized(optimized, source, tmp)
        original_size = source.stat().st_size
        optimized_size = tmp.stat().st_size
        if optimized_size < original_size:
            os.replace(tmp, source)
            print(f"Optimized: {source} ({original_size} -> {optimized_size} bytes)")
        else:
            tmp.unlink(missing_ok=True)
            print(f"Kept original: {source}")
        processed += 1
    except Exception as exc:
        tmp.unlink(missing_ok=True)
        print(f"Skipped: {source} ({exc})")

after = directory_size(products_path)
print(f"Processed files: {processed}")
print(f"Total size: {before} -> {after} bytes")
'@

$pythonScript = $pythonScript.
    Replace("__PRODUCTS_PATH__", $ProductsPath).
    Replace("__MAX_DIMENSION__", [string]$MaxDimension).
    Replace("__QUALITY__", [string]$Quality)

try {
    [System.IO.File]::WriteAllText($pythonScriptPath, $pythonScript.Replace("`r`n", "`n"), [System.Text.UTF8Encoding]::new($false))

    docker run --rm `
        -v "${VolumeName}:/images" `
        -v "${resolvedBackupDirectory}:/work" `
        python:3.12-slim `
        sh -c "python -m pip install --no-cache-dir pillow >/dev/null && python /work/$pythonScriptName"
} finally {
    if (Test-Path $pythonScriptPath) {
        Remove-Item $pythonScriptPath -Force
    }
}

Write-Step "Termine"
Write-Host "Sauvegarde creee : $resolvedBackupDirectory\$backupFileName" -ForegroundColor Green
