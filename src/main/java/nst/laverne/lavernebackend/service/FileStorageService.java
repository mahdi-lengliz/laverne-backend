package nst.laverne.lavernebackend.service;

import nst.laverne.lavernebackend.dto.ImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    ImageUploadResponse storeProductImage(MultipartFile file);
}
