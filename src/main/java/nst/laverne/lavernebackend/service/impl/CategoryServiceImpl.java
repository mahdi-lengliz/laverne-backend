package nst.laverne.lavernebackend.service.impl;

import java.util.List;
import nst.laverne.lavernebackend.dto.CategoryDto;
import nst.laverne.lavernebackend.mapper.CategoryMapper;
import nst.laverne.lavernebackend.repository.CategoryRepository;
import nst.laverne.lavernebackend.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<CategoryDto> findAll() {
        return categoryRepository.findAll().stream().map(categoryMapper::toDto).toList();
    }
}
