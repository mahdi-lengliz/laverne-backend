package nst.laverne.lavernebackend.service;

import nst.laverne.lavernebackend.dto.AuthRequest;
import nst.laverne.lavernebackend.dto.AuthResponse;

public interface AuthService {
    AuthResponse login(AuthRequest request);
}
