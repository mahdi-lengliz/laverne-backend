package nst.laverne.lavernebackend.service.impl;

import nst.laverne.lavernebackend.dto.AuthRequest;
import nst.laverne.lavernebackend.dto.AuthResponse;
import nst.laverne.lavernebackend.model.AdminUser;
import nst.laverne.lavernebackend.repository.AdminUserRepository;
import nst.laverne.lavernebackend.security.JwtService;
import nst.laverne.lavernebackend.service.AuthService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        AdminUser admin = adminUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Identifiants invalides"));
        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw new BadCredentialsException("Identifiants invalides");
        }
        return new AuthResponse(jwtService.generateToken(admin.getUsername(), admin.getRole()), admin.getUsername(), admin.getRole());
    }
}
