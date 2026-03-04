package com.example.demo.controller

import com.example.demo.dto.*
import com.example.demo.repository.UserRepository
import com.example.demo.service.StepService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/steps")
class StepController(
    private val stepService: StepService,
    private val userRepository: UserRepository
) {

    /**
     * Добавить шаги за сегодня
     * POST /api/steps/add
     */
    @PostMapping("/add")
    fun addSteps(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: AddStepsRequest
    ): ResponseEntity<StepResponse> {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("User not found")

        if (request.steps < 0) {
            throw RuntimeException("Steps cannot be negative")
        }

        if (request.steps > 100000) {
            throw RuntimeException("Too many steps at once! Maximum 100,000")
        }

        val response = stepService.addSteps(user.id, request.steps)
        return ResponseEntity.ok(response)
    }

    /**
     * Получить статистику за сегодня
     * GET /api/steps/today
     */
    @GetMapping("/today")
    fun getTodayStats(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<StepResponse> {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("User not found")

        val response = stepService.getTodayStats(user.id)
        return ResponseEntity.ok(response)
    }

    /**
     * Получить статистику за неделю
     * GET /api/steps/week
     */
    @GetMapping("/week")
    fun getWeeklyStats(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<WeeklyStatsResponse> {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("User not found")

        val response = stepService.getWeeklyStats(user.id)
        return ResponseEntity.ok(response)
    }

    /**
     * Проверить статус улитки
     * GET /api/steps/snail-check
     */
    @GetMapping("/snail-check")
    fun checkSnailStatus(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<SnailNotification> {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("User not found")

        val response = stepService.checkSnailStatus(user.id)
        return ResponseEntity.ok(response)
    }

    /**
     * Установить цель на день
     * PUT /api/steps/goal
     */
    @PutMapping("/goal")
    fun setGoal(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: SetGoalRequest
    ): ResponseEntity<GoalResponse> {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("User not found")

        val response = stepService.setGoal(user.id, request.goal)
        return ResponseEntity.ok(response)
    }
}