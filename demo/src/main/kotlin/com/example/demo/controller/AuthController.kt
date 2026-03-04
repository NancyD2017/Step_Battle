package com.example.demo.controller

import com.example.demo.dto.*
import com.example.demo.model.User
import com.example.demo.repository.*
import com.example.demo.service.JwtService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): AuthResponse {

        if (userRepository.findByUsername(request.username) != null) {
            throw RuntimeException("Username already exists")
        }

        val user = User(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password)
        )

        userRepository.save(user)

        val token = jwtService.generateToken(user.username)

        return AuthResponse(token)
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): AuthResponse {

        val user = userRepository.findByUsername(request.username)
            ?: throw RuntimeException("User not found")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw RuntimeException("Wrong password")
        }

        val token = jwtService.generateToken(user.username)
        return AuthResponse(token)
    }
}