package com.bugtracker.controller;

import com.bugtracker.dto.RegisterRequest;
import com.bugtracker.model.User;
import com.bugtracker.repository.UserRepository;
import com.bugtracker.security.JwtTokenProvider;
import com.bugtracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    // Helper method to generate token directly
    private String generateTokenForUser(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 86400000); // 24 hours
        
        // Use a secure key that's at least 256 bits (32 bytes)
        String secureKey = "mySecretKeyForJWTTokenGenerationThatIsSecureEnoughForHS256Algorithm";
        
        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(secureKey.getBytes()))
                .compact();
    }

    @GetMapping("/test-token")
    public ResponseEntity<?> testToken() {
        try {
            String testToken = generateTokenForUser("test@example.com");
            return ResponseEntity.ok("Token generated successfully: " + testToken.substring(0, 50) + "...");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Token generation failed: " + e.getMessage());
        }
    }

    @GetMapping("/test-auth/{email}/{password}")
    public ResponseEntity<?> testAuth(@PathVariable String email, @PathVariable String password) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            PasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            
            boolean passwordMatches = encoder.matches(password, user.getPassword());
            boolean isBCrypt = user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$") || user.getPassword().startsWith("$2y$");
            
            return ResponseEntity.ok("Email: " + email + 
                    ", Password matches: " + passwordMatches + 
                    ", Is BCrypt: " + isBCrypt + 
                    ", Password hash: " + user.getPassword());
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/test-all-users")
    public ResponseEntity<?> testAllUsers() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== ALL USERS STATUS ===\n\n");
            
            java.util.List<User> allUsers = userRepository.findAll();
            PasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            
            for (User user : allUsers) {
                boolean isBCrypt = user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$") || user.getPassword().startsWith("$2y$");
                boolean works = false;
                String testPassword = "test123";
                
                // Try different passwords for different users
                if (user.getEmail().equals("admin@gmail.com")) {
                    testPassword = "admin123";
                } else if (user.getEmail().contains("test")) {
                    testPassword = "test123";
                } else {
                    testPassword = "test123"; // Default for unknown users
                }
                
                works = encoder.matches(testPassword, user.getPassword());
                
                result.append("ID: ").append(user.getId()).append("\n");
                result.append("Name: ").append(user.getName()).append("\n");
                result.append("Email: ").append(user.getEmail()).append("\n");
                result.append("Role: ").append(user.getRole()).append("\n");
                result.append("Test Password: ").append(testPassword).append("\n");
                result.append("Is BCrypt: ").append(isBCrypt).append("\n");
                result.append("Status: ").append(works ? "✅ WORKING" : "❌ NOT WORKING").append("\n");
                result.append("----------------------------------------\n\n");
            }
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/fix-admin-password")
    public ResponseEntity<?> fixAdminPassword() {
        try {
            // Find the admin user
            User user = userRepository.findByEmail("admin@gmail.com")
                    .orElseThrow(() -> new RuntimeException("Admin user not found"));
            
            // Encode the password with BCrypt
            String encodedPassword = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("admin123");
            user.setPassword(encodedPassword);
            userRepository.save(user);
            
            return ResponseEntity.ok("Admin password fixed! Use email: admin@gmail.com, password: admin123");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/fix-password")
    public ResponseEntity<?> fixPassword() {
        try {
            // Find the admin user
            User user = userRepository.findByEmail("admin@gmail.com")
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Encode the password with BCrypt
            String encodedPassword = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("admin123");
            user.setPassword(encodedPassword);
            userRepository.save(user);
            
            return ResponseEntity.ok("Password fixed! Use email: admin@gmail.com, password: admin123");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestParam String email, @RequestParam String newPassword) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Encode password with BCrypt
            String encodedPassword = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(newPassword);
            user.setPassword(encodedPassword);
            userRepository.save(user);
            
            return ResponseEntity.ok("Password updated successfully for " + email);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/test/{email}")
    public ResponseEntity<?> testUser(@PathVariable String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            String passwordHash = user.getPassword();
            boolean isBCrypt = passwordHash.startsWith("$2a$") || passwordHash.startsWith("$2b$") || passwordHash.startsWith("$2y$");
            
            return ResponseEntity.ok("User found: " + user.getName() + 
                    ", Email: " + user.getEmail() + 
                    ", Role: " + user.getRole() +
                    ", Password length: " + passwordHash.length() +
                    ", Is BCrypt: " + isBCrypt +
                    ", Password hash: " + passwordHash);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> testDatabase() {
        try {
            long userCount = userRepository.count();
            java.util.List<User> users = userRepository.findAll();
            StringBuilder userInfo = new StringBuilder();
            userInfo.append("Database connected. Users count: ").append(userCount).append("\n");
            
            for (User user : users) {
                userInfo.append("User: ID=").append(user.getId())
                        .append(", Name=").append(user.getName())
                        .append(", Email=").append(user.getEmail())
                        .append(", Role=").append(user.getRole()).append("\n");
            }
            
            return ResponseEntity.ok(userInfo.toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Database error: " + e.getMessage());
        }
    }

    @GetMapping("/test-register")
    public ResponseEntity<?> testRegistration() {
        try {
            // Test creating a user with a unique email
            String timestamp = String.valueOf(System.currentTimeMillis());
            User testUser = userService.createUser(new User(
                "Test User " + timestamp,
                "test" + timestamp + "@example.com",
                "test123",
                User.Role.TESTER
            ));
            
            return ResponseEntity.ok("Test user created: " + testUser.getEmail() + " with ID: " + testUser.getId());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Registration test failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            User createdUser = userService.createUser(new User(
                registerRequest.getName(),
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getRoleAsEnum()
            ));
            
            return ResponseEntity.ok(createdUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    @GetMapping("/test-login-public")
    public ResponseEntity<?> testLoginPublic() {
        try {
            // Test the login-public endpoint with known credentials
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail("test@example.com");
            loginRequest.setPassword("test123");
            
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found: " + loginRequest.getEmail()));
            
            PasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            
            if (!encoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.status(403).body("Invalid credentials");
            }
            
            // Generate token directly with username
            String jwt = generateTokenForUser(user.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("type", "Bearer");
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("name", user.getName());
            response.put("role", user.getRole());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Login test error: " + e.getMessage());
        }
    }

    @PostMapping("/login-public")
    public ResponseEntity<?> loginPublic(@RequestBody LoginRequest loginRequest) {
        try {
            // This endpoint bypasses authentication for testing
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found: " + loginRequest.getEmail()));
            
            // Simple password check for testing
            org.springframework.security.crypto.password.PasswordEncoder encoder = 
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            
            if (!encoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.status(403).body("Invalid credentials");
            }
            
            // Generate token directly with username (simpler approach)
            String jwt = generateTokenForUser(user.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("type", "Bearer");
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("name", user.getName());
            response.put("role", user.getRole());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Login error: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("type", "Bearer");
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("name", user.getName());
        response.put("role", user.getRole());

        return ResponseEntity.ok(response);
    }

    public static class LoginRequest {
        private String email;
        private String password;

        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
