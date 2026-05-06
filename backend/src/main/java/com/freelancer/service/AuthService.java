package com.freelancer.service;

import com.freelancer.dto.request.LoginRequest;
import com.freelancer.dto.request.RegisterRequest;
import com.freelancer.dto.response.AuthResponse;
import com.freelancer.dto.response.UserSummaryResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void logout(String token);

    UserSummaryResponse getCurrentUser();
}
