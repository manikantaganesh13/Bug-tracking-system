package com.bugtracker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Bug Tracking System API is running! 🚀\n\n" +
               "📋 Available Endpoints:\n\n" +
               "🔐 Authentication:\n" +
               "  • POST /api/auth/login\n" +
               "  • POST /api/auth/login-public\n" +
               "  • POST /api/auth/register\n" +
               "  • GET  /api/auth/test\n" +
               "  • GET  /api/auth/test-all-credentials\n\n" +
               "📁 Projects:\n" +
               "  • GET  /api/projects\n" +
               "  • POST /api/projects (ADMIN)\n\n" +
               "👥 Users:\n" +
               "  • GET  /api/users (ADMIN)\n" +
               "  • POST /api/users/register\n\n" +
               "🐛 Bugs:\n" +
               "  • GET  /api/bugs\n" +
               "  • POST /api/bugs\n\n" +
               "🌐 Frontend:\n" +
               "  • http://localhost:3000\n\n" +
               "🔑 Test Credentials:\n" +
               "  • Admin: admin@gmail.com / admin123\n" +
               "  • Tester: test@example.com / test123";
    }

    @GetMapping("/favicon.ico")
    public void favicon() {
        // Return empty response for favicon to avoid 403 errors
    }
}
