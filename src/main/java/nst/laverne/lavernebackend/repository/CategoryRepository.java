package nst.laverne.lavernebackend.repository;

import java.util.Optional;
import nst.laverne.lavernebackend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
}
