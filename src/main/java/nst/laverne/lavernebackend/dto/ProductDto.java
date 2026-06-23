package nst.laverne.lavernebackend.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductDto(
        Long id,
        String name,
        String sub,
        Integer perfumeSize,
        String size,
        BigDecimal price,
        Long categoryId,
        Long cat,
        String categoryName,
        String emoji,
        String badge,
        String description,
        Integer stock,
        String imageUrl,
        String imageUrl2,
        String imageUrl3,
        String imageUrl4,
        boolean collection,
        boolean isCollection,
        List<String> notes
) {
}
