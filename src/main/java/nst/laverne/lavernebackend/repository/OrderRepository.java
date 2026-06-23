package nst.laverne.lavernebackend.repository;

import java.util.List;
import nst.laverne.lavernebackend.model.CustomerOrder;
import nst.laverne.lavernebackend.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    long countByStatus(OrderStatus status);

    List<CustomerOrder> findAllByOrderByCreatedAtDesc();
}
