package nst.laverne.lavernebackend.repository;

import nst.laverne.lavernebackend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
