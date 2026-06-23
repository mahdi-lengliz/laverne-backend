package nst.laverne.lavernebackend.service;

import java.util.List;
import nst.laverne.lavernebackend.dto.ProductDto;
import nst.laverne.lavernebackend.dto.ProductRequest;

public interface ProductService {
    List<ProductDto> findAll();

    ProductDto findById(Long id);

    ProductDto create(ProductRequest request);

    ProductDto update(Long id, ProductRequest request);

    void delete(Long id);
}
