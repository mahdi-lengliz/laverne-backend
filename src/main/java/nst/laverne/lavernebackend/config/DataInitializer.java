package nst.laverne.lavernebackend.config;

import nst.laverne.lavernebackend.model.AdminUser;
import nst.laverne.lavernebackend.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedAdminUser(AdminUserRepository adminUserRepository,
                                    PasswordEncoder passwordEncoder,
                                    @Value("${admin.default.username}") String adminUsername,
                                    @Value("${admin.default.password}") String adminPassword) {
        return args -> {
            if (adminUserRepository.findByUsername(adminUsername).isPresent()) {
                return;
            }

            AdminUser admin = new AdminUser();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            adminUserRepository.save(admin);
        };
    }
}
