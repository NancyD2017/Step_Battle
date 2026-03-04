package com.example.demo.controller

import com.example.demo.dto.UserResponse
import com.example.demo.model.User
import com.example.demo.repository.UserRepository
import org.apache.tomcat.util.net.openssl.ciphers.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userRepository: UserRepository
) {

    @GetMapping("/me")
    fun getCurrentUser(authentication: Authentication): UserResponse {

        val username = authentication.name

        val user = userRepository.findByUsername(username)
            ?: throw RuntimeException("User not found")

        return UserResponse(
            id = user.id,
            username = user.username,
            coins = user.coins,
            weeklySteps = user.weeklySteps,
            goal = user.goal,
            recordWeek = user.recordWeek
        )
    }
}