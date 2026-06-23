package nst.laverne.lavernebackend.controller;

import java.util.List;
import nst.laverne.lavernebackend.dto.OrderDto;
import nst.laverne.lavernebackend.dto.OrderStatusRequest;
import nst.laverne.lavernebackend.dto.ImageUploadResponse;
import nst.laverne.lavernebackend.dto.ProductDto;
import nst.laverne.lavernebackend.dto.ProductRequest;
import nst.laverne.lavernebackend.dto.StatsDto;
import nst.laverne.lavernebackend.service.FileStorageService;
import nst.laverne.lavernebackend.service.OrderService;
import nst.laverne.lavernebackend.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final ProductService productService;
    private final OrderService orderService;
    private final FileStorageService fileStorageService;

    public AdminController(ProductService productService, OrderService orderService, FileStorageService fileStorageService) {
        this.productService = productService;
        this.orderService = orderService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/orders")
    public List<OrderDto> orders() {
        return orderService.findAll();
    }

    @PatchMapping("/orders/{id}/status")
    public OrderDto updateStatus(@PathVariable Long id, @RequestBody OrderStatusRequest request) {
        return orderService.updateStatus(id, request.status());
    }

    @GetMapping("/stats")
    public StatsDto stats() {
        return orderService.stats();
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto createProduct(@RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/products/{id}")
    public ProductDto updateProduct(@PathVariable Long id, @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        productService.delete(id);
    }

    @PostMapping("/uploads/product-image")
    public ImageUploadResponse uploadProductImage(@RequestParam("file") MultipartFile file) {
        return fileStorageService.storeProductImage(file);
    }
}
