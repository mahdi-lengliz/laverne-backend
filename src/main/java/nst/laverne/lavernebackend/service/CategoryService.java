package nst.laverne.lavernebackend.service;

import java.util.List;
import nst.laverne.lavernebackend.dto.CategoryDto;

public interface CategoryService {
    List<CategoryDto> findAll();
}
