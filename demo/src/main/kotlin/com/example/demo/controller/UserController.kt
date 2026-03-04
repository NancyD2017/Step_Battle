package com.example.demo.controller

import com.example.demo.dto.*
import com.example.demo.repository.DailyStepRepository
import com.example.demo.repository.UserCatRepository
import com.example.demo.repository.UserRepository
import com.example.demo.service.CatService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userRepository: UserRepository,
    private val userCatRepository: UserCatRepository,
    private val dailyStepRepository: DailyStepRepository,
    private val catService: CatService
) {

    /**
     * Получить профиль текущего пользователя
     * GET /api/users/me
     */
    @GetMapping("/me")
    fun getCurrentUser(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<UserProfileResponse> {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("User not found")

        // Получаем шаги за сегодня
        val todaySteps = dailyStepRepository.findByUserIdAndDate(user.id, LocalDate.now())?.steps ?: 0

        // Получаем котиков пользователя
        val cats = catService.getUserCats(user.id)

        // Подсчитываем улиток за последние 7 дней
        val weekAgo = LocalDate.now().minusDays(7)
        val yesterday = LocalDate.now().minusDays(1)
        val snailCount = dailyStepRepository.countSnailDays(user.id, weekAgo, yesterday)

        // Рассчитываем прогресс
        val progress = if (user.goal > 0) {
            (todaySteps.toDouble() / user.goal) * 100
        } else 0.0

        val response = UserProfileResponse(
            id = user.id,
            username = user.username,
            coins = user.coins,
            weeklySteps = user.weeklySteps,
            totalSteps = user.totalSteps,
            goal = user.goal,
            recordWeek = user.recordWeek,
            snailCount = snailCount,
            cats = cats,
            todaySteps = todaySteps,
            todayProgress = kotlin.math.min(progress, 100.0)
        )

        return ResponseEntity.ok(response)
    }

    /**
     * Получить краткую информацию о пользователе
     * GET /api/users/me/brief
     */
    @GetMapping("/me/brief")
    fun getCurrentUserBrief(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<UserResponse> {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("User not found")

        val response = UserResponse(
            id = user.id,
            username = user.username,
            email = user.email,
            coins = user.coins,
            weeklySteps = user.weeklySteps,
            totalSteps = user.totalSteps,
            goal = user.goal,
            recordWeek = user.recordWeek
        )

        return ResponseEntity.ok(response)
    }

    /**
     * Получить информацию о другом пользователе (публичный профиль)
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<Map<String, Any>> {
        val user = userRepository.findById(id)
            .orElseThrow { RuntimeException("User not found") }

        val cats = catService.getUserCats(id)

        val response = mapOf(
            "id" to user.id,
            "username" to user.username,
            "weeklySteps" to user.weeklySteps,
            "totalSteps" to user.totalSteps,
            "cats" to cats.size,
            "recordWeek" to user.recordWeek
        )

        return ResponseEntity.ok(response)
    }
}