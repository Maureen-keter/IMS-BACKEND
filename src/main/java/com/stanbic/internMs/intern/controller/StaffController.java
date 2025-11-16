package com.stanbic.onboarding.admin.utils;

import com.stanbic.onboarding.admin.model.Staff;
import com.stanbic.onboarding.admin.model.Permission;
import com.stanbic.onboarding.admin.repository.CustomerRepository;
import com.stanbic.onboarding.admin.repository.UserRepository;
import com.stanbic.onboarding.admin.repository.StaffRepository;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository; // ✅ NEW for Staff users

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   UserRepository userRepository,
                                   CustomerRepository customerRepository,
                                   StaffRepository staffRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.staffRepository = staffRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth"); // skip authentication for /auth/**
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (DispatcherType.ERROR.equals(request.getDispatcherType())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.debug("JWT Token detected: {}", token);

            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                log.debug("Extracted username from token: {}", username);

                // 🧠 Step 1: Check if token belongs to a Customer
                customerRepository.findByEmailAddress(username).ifPresentOrElse(user -> {
                    authenticateUser(user.getEmailAddress(), user.getPasswordHash(),
                            List.of(new SimpleGrantedAuthority(user.getRole().getName().toUpperCase())),
                            request);
                }, () -> {
                    // 🧠 Step 2: If not a Customer, check if Staff
                    staffRepository.findByUsername(username).ifPresentOrElse(staff -> {
                        Set<Permission> permissions = staff.getPermissions();
                        List<GrantedAuthority> authorities = permissions.stream()
                                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                                .collect(Collectors.toList());

                        authenticateUser(staff.getUsername(), "", authorities, request);
                    }, () -> {
                        // 🧠 Step 3: If not Staff, check if Admin
                        userRepository.findByUsername(username).ifPresent(admin -> {
                            authenticateUser(admin.getUsername(), admin.getPassword(),
                                    List.of(new SimpleGrantedAuthority("ADMIN")),
                                    request);
                        });
                    });
                });
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateUser(String username, String password,
                                  List<GrantedAuthority> authorities,
                                  HttpServletRequest request) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password(password)
                .authorities(authorities)
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("User '{}' authenticated with authorities: {}", username, authorities);
    }
}
