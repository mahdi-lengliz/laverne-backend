package nst.laverne.lavernebackend.service.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import nst.laverne.lavernebackend.dto.ImageUploadResponse;
import nst.laverne.lavernebackend.exception.BadRequestException;
import nst.laverne.lavernebackend.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageServiceImpl implements FileStorageService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final Set<String> PASSTHROUGH_EXTENSIONS = Set.of("webp", "gif");
    private static final int MAX_IMAGE_DIMENSION = 1600;
    private static final float JPEG_QUALITY = 0.9f;
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
            Files.write(target, optimizedImageBytes(file, extension));
            return new ImageUploadResponse("/uploads/products/" + filename);
        } catch (IOException exception) {
            throw new BadRequestException("Impossible d'enregistrer l'image");
        }
    }

    private byte[] optimizedImageBytes(MultipartFile file, String extension) throws IOException {
        byte[] originalBytes = file.getBytes();
        if (PASSTHROUGH_EXTENSIONS.contains(extension)) {
            return originalBytes;
        }

        BufferedImage source = ImageIO.read(new ByteArrayInputStream(originalBytes));
        if (source == null) {
            throw new BadRequestException("Image invalide");
        }

        BufferedImage optimized = resizeIfNeeded(source, extension);
        byte[] optimizedBytes = writeImage(optimized, extension);
        return optimizedBytes.length < originalBytes.length ? optimizedBytes : originalBytes;
    }

    private BufferedImage resizeIfNeeded(BufferedImage source, String extension) {
        int width = source.getWidth();
        int height = source.getHeight();
        int largestDimension = Math.max(width, height);
        if (largestDimension <= MAX_IMAGE_DIMENSION) {
            return source;
        }

        double scale = (double) MAX_IMAGE_DIMENSION / largestDimension;
        int resizedWidth = Math.max(1, (int) Math.round(width * scale));
        int resizedHeight = Math.max(1, (int) Math.round(height * scale));
        BufferedImage resized = new BufferedImage(resizedWidth, resizedHeight, imageType(source, extension));
        Graphics2D graphics = resized.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isJpeg(extension)) {
                graphics.setColor(Color.WHITE);
                graphics.fillRect(0, 0, resizedWidth, resizedHeight);
            }
            graphics.drawImage(source, 0, 0, resizedWidth, resizedHeight, null);
            return resized;
        } finally {
            graphics.dispose();
        }
    }

    private byte[] writeImage(BufferedImage image, String extension) throws IOException {
        if (isJpeg(extension)) {
            return writeJpeg(image, extension);
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (!ImageIO.write(image, extension, outputStream)) {
            throw new BadRequestException("Format image non supporte");
        }
        return outputStream.toByteArray();
    }

    private byte[] writeJpeg(BufferedImage image, String extension) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(extension);
        if (!writers.hasNext()) {
            throw new BadRequestException("Format image non supporte");
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageWriter writer = writers.next();
        try (ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream)) {
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            if (writeParam.canWriteCompressed()) {
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                writeParam.setCompressionQuality(JPEG_QUALITY);
            }
            writer.setOutput(imageOutputStream);
            writer.write(null, new IIOImage(toRgbImage(image), null, null), writeParam);
        } finally {
            writer.dispose();
        }
        return outputStream.toByteArray();
    }

    private BufferedImage toRgbImage(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            return image;
        }
        BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rgbImage.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.drawImage(image, 0, 0, null);
            return rgbImage;
        } finally {
            graphics.dispose();
        }
    }

    private int imageType(BufferedImage source, String extension) {
        if (isJpeg(extension)) {
            return BufferedImage.TYPE_INT_RGB;
        }
        return source.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
    }

    private boolean isJpeg(String extension) {
        return "jpg".equals(extension) || "jpeg".equals(extension);
    }

    private String extensionOf(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
