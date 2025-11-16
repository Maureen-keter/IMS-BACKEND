package com.stanbic.internMs.intern.service;

import com.stanbic.internMs.intern.exception.ValidationException;
import com.stanbic.internMs.intern.repository.*;
import com.stanbic.internMs.intern.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final CohortRepository cohortRepository;
    private final RotationRepository rotationRepository;
    private final DepartmentRepository departmentRepository;
    private final PerformanceReviewRepository performanceReviewRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(
            UserRepository userRepository, CohortRepository cohortRepository, PerformanceReviewRepository performanceReviewRepository,
            RotationRepository rotationRepository, DepartmentRepository departmentRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.cohortRepository = cohortRepository;
        this.departmentRepository = departmentRepository;
        this.performanceReviewRepository = performanceReviewRepository;
        this.rotationRepository = rotationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public ResponseEntity<Map<String, String>> login(String userID, String password) {
        log.info("UserID: " + userID + "password: " + password);
        return userRepository.findByUserID(userID)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(user -> {

                            String jwt = jwtUtil.generateToken(user.getUserID());
                            return ResponseEntity.ok(Map.of(
                                    "access_token", jwt,
                                    "token_type", "Bearer"
                            ));
                        }
                )
                .orElseThrow(() -> new ValidationException(Map.of("id", "Invalid credentials.")));
    }
}

