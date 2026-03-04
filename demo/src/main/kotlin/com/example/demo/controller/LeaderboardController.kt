package com.example.demo.controller

import com.example.demo.dto.*
import com.example.demo.repository.UserRepository
import com.example.demo.service.LeaderboardService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/leaderboard")
class LeaderboardController(
    private val leaderboardService: LeaderboardService,
    private val userRepository: UserRepository
) {

    /**
     * Получить недельный рейтинг
     * GET /api/leaderboard/weekly
     */
    @GetMapping("/weekly")
    fun getWeeklyLeaderboard(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<LeaderboardResponse> {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("User not found")

        val response = leaderboardService.getWeeklyLeaderboard(user.id)
        return ResponseEntity.ok(response)
    }

    /**
     * Получить общий рейтинг (за всё время)
     * GET /api/leaderboard/all-time
     */
    @GetMapping("/all-time")
    fun getAllTimeLeaderboard(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<LeaderboardResponse> {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("User not found")

        val response = leaderboardService.getAllTimeLeaderboard(user.id)
        return ResponseEntity.ok(response)
    }

    /**
     * Сравнить себя с другом
     * GET /api/leaderboard/compare/{friendId}
     */
    @GetMapping("/compare/{friendId}")
    fun compareWithFriend(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable friendId: Long
    ): ResponseEntity<FriendComparisonResponse> {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("User not found")

        val response = leaderboardService.compareWithFriend(user.id, friendId)
        return ResponseEntity.ok(response)
    }

    /**
     * Поиск пользователей
     * GET /api/leaderboard/search?query=name
     */
    @GetMapping("/search")
    fun searchUsers(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam query: String
    ): ResponseEntity<List<LeaderboardEntry>> {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("User not found")

        val response = leaderboardService.searchUsers(query, user.id)
        return ResponseEntity.ok(response)
    }

    /**
     * Получить свою позицию в рейтинге
     * GET /api/leaderboard/my-rank?type=weekly
     */
    @GetMapping("/my-rank")
    fun getMyRank(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(defaultValue = "weekly") type: String
    ): ResponseEntity<Map<String, Int>> {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("User not found")

        val rank = leaderboardService.getUserRank(user.id, type)
        return ResponseEntity.ok(mapOf("rank" to rank))
    }

    /**
     * Получить глобальную статистику
     * GET /api/leaderboard/stats
     */
    @GetMapping("/stats")
    fun getGlobalStats(): ResponseEntity<Map<String, Any>> {
        val response = leaderboardService.getGlobalStats()
        return ResponseEntity.ok(response)
    }
}