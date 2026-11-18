package com.example.lims.controller;

import com.example.lims.security.JwtUtil;
import com.example.lims.security.UserDetailsImpl;
import com.example.lims.service.AuditService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService,
                          JwtUtil jwtUtil,
                          AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.auditService = auditService;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.get("email"), request.get("password"))
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.get("email"));
        final String jwt = jwtUtil.generateToken(userDetails);

        if (userDetails instanceof UserDetailsImpl details) {
            auditService.log("LOGIN", "USER", details.getUser().getId());
        }

        return Map.of("token", jwt);
    }
}