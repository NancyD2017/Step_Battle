package com.example.demo.controller

import com.example.demo.dto.*
import com.example.demo.model.User
import com.example.demo.repository.UserRepository
import com.example.demo.service.JwtService
import com.example.demo.service.PasswordResetService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val passwordResetService: PasswordResetService
) {

    /**
     * Регистрация нового пользователя
     * POST /auth/register
     */
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<Any> {
        // Валидация
        if (request.username.length < 3) {
            return ResponseEntity.badRequest()
                .body(ErrorResponse("VALIDATION_ERROR", "Username must be at least 3 characters"))
        }

        if (request.password.length < 6) {
            return ResponseEntity.badRequest()
                .body(ErrorResponse("VALIDATION_ERROR", "Password must be at least 6 characters"))
        }

        if (!request.email.contains("@")) {
            return ResponseEntity.badRequest()
                .body(ErrorResponse("VALIDATION_ERROR", "Invalid email format"))
        }

        // Проверка на существование
        if (userRepository.existsByUsername(request.username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse("USERNAME_EXISTS", "Username already exists"))
        }

        if (userRepository.existsByEmail(request.email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse("EMAIL_EXISTS", "Email already exists"))
        }

        // Создание пользователя
        val user = User(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            coins = 100, // Начальный бонус!
            goal = 8000 // Стандартная цель
        )

        userRepository.save(user)

        val token = jwtService.generateToken(user.username)

        return ResponseEntity.ok(mapOf(
            "token" to token,
            "user" to mapOf(
                "id" to user.id,
                "username" to user.username,
                "email" to user.email,
                "coins" to user.coins
            ),
            "message" to "Registration successful! Welcome to StepBattle!"
        ))
    }

    /**
     * Вход в систему
     * POST /auth/login
     */
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<Any> {
        val user = userRepository.findByUsername(request.username)

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse("INVALID_CREDENTIALS", "Invalid username or password"))
        }

        if (!passwordEncoder.matches(request.password, user.password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse("INVALID_CREDENTIALS", "Invalid username or password"))
        }

        val token = jwtService.generateToken(user.username)

        return ResponseEntity.ok(mapOf(
            "token" to token,
            "user" to mapOf(
                "id" to user.id,
                "username" to user.username,
                "email" to user.email,
                "coins" to user.coins
            ),
            "message" to "Login successful!"
        ))
    }

    /**
     * Проверка валидности токена
     * POST /auth/validate
     */
    @PostMapping("/validate")
    fun validateToken(@RequestHeader("Authorization") authHeader: String): ResponseEntity<Any> {
        if (!authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse("INVALID_TOKEN", "Invalid token format"))
        }

        val token = authHeader.substring(7)

        return try {
            val username = jwtService.extractUsername(token)
            val user = userRepository.findByUsername(username)

            if (user != null && jwtService.isTokenValid(token, username)) {
                ResponseEntity.ok(mapOf(
                    "valid" to true,
                    "username" to username
                ))
            } else {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse("INVALID_TOKEN", "Token is invalid or expired"))
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse("INVALID_TOKEN", "Token validation failed"))
        }
    }

    /**
     * Запрос на сброс пароля (Forgot Password)
     * POST /auth/forgot-password
     */
    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestBody request: ForgotPasswordRequest): ResponseEntity<ForgotPasswordResponse> {
        val response = passwordResetService.requestPasswordReset(request)
        return ResponseEntity.ok(response)
    }

    /**
     * Сброс пароля по токену
     * POST /auth/reset-password
     */
    @PostMapping("/reset-password")
    fun resetPassword(@RequestBody request: ResetPasswordRequest): ResponseEntity<ForgotPasswordResponse> {
        val response = passwordResetService.resetPassword(request)
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
        }
    }

    /**
     * Получить токен для тестирования (только для разработки!)
     * GET /auth/test-token?email=user@example.com
     */
    @GetMapping("/test-token")
    fun getTestToken(@RequestParam email: String): ResponseEntity<Any> {
        val token = passwordResetService.getTokenForTesting(email)
        return if (token != null) {
            ResponseEntity.ok(mapOf("token" to token))
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse("NOT_FOUND", "No reset token found for this email"))
        }
    }
}