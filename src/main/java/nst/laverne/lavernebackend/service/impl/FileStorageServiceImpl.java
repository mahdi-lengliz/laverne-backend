package nst.laverne.lavernebackend.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import nst.laverne.lavernebackend.dto.ImageUploadResponse;
import nst.laverne.lavernebackend.exception.BadRequestException;
import nst.laverne.lavernebackend.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageServiceImpl implements FileStorageService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private final Path productImagesPath = Paths.get("uploads", "products").toAbsolutePath().normalize();

    @Override
    public ImageUploadResponse storeProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image requise");
        }
        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = extensionOf(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Format image non supporte");
        }
        try {
            Files.createDirectories(productImagesPath);
            String filename = UUID.randomUUID() + "." + extension;
            Path target = productImagesPath.resolve(filename).normalize();
            if (!target.startsWith(productImagesPath)) {
                throw new BadRequestException("Nom de fichier invalide");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return new ImageUploadResponse("/uploads/products/" + filename);
        } catch (IOException exception) {
            throw new BadRequestException("Impossible d'enregistrer l'image");
        }
    }

    private String extensionOf(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
