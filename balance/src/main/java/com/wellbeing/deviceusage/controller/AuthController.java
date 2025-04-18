package com.wellbeing.deviceusage.controller;

import com.wellbeing.deviceusage.dto.auth.JwtResponse;
import com.wellbeing.deviceusage.dto.auth.LoginRequest;
import com.wellbeing.deviceusage.dto.auth.MessageResponse;
import com.wellbeing.deviceusage.dto.auth.SignupRequest;
import com.wellbeing.deviceusage.model.ERole;
import com.wellbeing.deviceusage.model.Role;
import com.wellbeing.deviceusage.model.User;
import com.wellbeing.deviceusage.repository.RoleRepository;
import com.wellbeing.deviceusage.repository.UserRepository;
import com.wellbeing.deviceusage.security.jwt.JwtUtils;
import com.wellbeing.deviceusage.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
        @Autowired
        AuthenticationManager authenticationManager;

        @Autowired
        UserRepository userRepository;

        @Autowired
        RoleRepository roleRepository;

        @Autowired
        PasswordEncoder encoder;

        @Autowired
        JwtUtils jwtUtils;

        @PostMapping("/signin")
        public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

                System.out.println("dsadsadfsafdewagfwqegffgewgrsehy54thtreshtbgfsdgrehrtbeasghrhtedrsegrsawerag");
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                String jwt = jwtUtils.generateJwtToken(authentication);

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                List<String> roles = userDetails.getAuthorities().stream()
                        .map(item -> item.getAuthority())
                        .collect(Collectors.toList());

                return ResponseEntity.ok(new JwtResponse(jwt,
                        userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getEmail(),
                        roles));
        }

        @PostMapping("/signup")
        public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
                if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                        return ResponseEntity
                                .badRequest()
                                .body(new MessageResponse("Error: Username is already taken!"));
                }

                if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                        return ResponseEntity
                                .badRequest()
                                .body(new MessageResponse("Error: Email is already in use!"));
                }

                // Create new user's account
                User user = new User();
                user.setUsername(signUpRequest.getUsername());
                user.setEmail(signUpRequest.getEmail());
                user.setPassword(encoder.encode(signUpRequest.getPassword()));

                Set<String> strRoles = signUpRequest.getRole();
                Set<Role> roles = new HashSet<>();

                if (strRoles == null) {
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                } else {
                        strRoles.forEach(role -> {
                                switch (role) {
                                        case "admin":
                                                Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                                                roles.add(adminRole);
                                                break;
                                        default:
                                                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                                                roles.add(userRole);
                                }
                        });
                }

                user.setRoles(roles);
                userRepository.save(user);

                return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        }

        @PostMapping("/signout")
        public ResponseEntity<?> logoutUser(@RequestHeader("Authorization") String authHeader) {
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String jwt = authHeader.substring(7);
                        jwtUtils.blacklistToken(jwt);
                        return ResponseEntity.ok(new MessageResponse("Logout successful!"));
                }

                return ResponseEntity.badRequest().body(new MessageResponse("No token provided"));
        }
}