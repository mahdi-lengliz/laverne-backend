package nst.laverne.lavernebackend.mapper;

import java.util.ArrayList;
import nst.laverne.lavernebackend.dto.ProductDto;
import nst.laverne.lavernebackend.dto.ProductRequest;
import nst.laverne.lavernebackend.model.Category;
import nst.laverne.lavernebackend.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public ProductDto toDto(Product product) {
        Long categoryId = product.getCategory().getId();
        String size = product.getPerfumeSize() == null ? null : product.getPerfumeSize() + " ML";
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getSub(),
                product.getPerfumeSize(),
                size,
                product.getPrice(),
                categoryId,
                categoryId,
                product.getCategory().getName(),
                product.getEmoji(),
                product.getBadge(),
                product.getDescription(),
                product.getStock(),
                product.getImageUrl(),
                product.getImageUrl2(),
                product.getImageUrl3(),
                product.getImageUrl4(),
                product.isCollection(),
                product.isCollection(),
                product.getNotes()
        );
    }

    public void updateEntity(Product product, ProductRequest request, Category category) {
        product.setName(request.name());
        product.setSub(request.sub());
        product.setPerfumeSize(request.perfumeSize());
        product.setPrice(request.price());
        product.setCategory(category);
        product.setEmoji(request.emoji());
        product.setBadge(request.badge());
        product.setDescription(request.description());
        product.setStock(request.stock() == null ? 0 : request.stock());
        product.setImageUrl(request.imageUrl());
        product.setImageUrl2(request.imageUrl2());
        product.setImageUrl3(request.imageUrl3());
        product.setImageUrl4(request.imageUrl4());
        product.setCollection(Boolean.TRUE.equals(request.collection()));
        product.setNotes(request.notes() == null ? new ArrayList<>() : new ArrayList<>(request.notes()));
    }
}
