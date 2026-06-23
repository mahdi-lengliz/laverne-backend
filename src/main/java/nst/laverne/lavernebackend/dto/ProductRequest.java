package nst.laverne.lavernebackend.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
        String name,
        String sub,
        Integer perfumeSize,
        BigDecimal price,
        Long categoryId,
        String emoji,
        String badge,
        String description,
        Integer stock,
        String imageUrl,
        String imageUrl2,
        String imageUrl3,
        String imageUrl4,
        Boolean collection,
        List<String> notes
) {
}
