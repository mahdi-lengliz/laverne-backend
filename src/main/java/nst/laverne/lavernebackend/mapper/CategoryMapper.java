package nst.laverne.lavernebackend.mapper;

import nst.laverne.lavernebackend.dto.CategoryDto;
import nst.laverne.lavernebackend.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoryDto toDto(Category category) {
        return new CategoryDto(category.getId(), category.getName(), category.getEmoji());
    }
}
