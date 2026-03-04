package com.example.demo.dto

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    val token: String
)

data class UserResponse(
    val id: Long,
    val username: String,
    val coins: Int,
    val weeklySteps: Int,
    val goal: Int,
    val recordWeek: Int
)