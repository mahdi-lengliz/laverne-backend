package nst.laverne.lavernebackend.repository;

import java.util.Optional;
import nst.laverne.lavernebackend.model.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    Optional<AdminUser> findByUsername(String username);
}
