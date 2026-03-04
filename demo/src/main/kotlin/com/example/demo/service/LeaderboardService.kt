package com.example.demo.service

import com.example.demo.dto.*
import com.example.demo.repository.UserCatRepository
import com.example.demo.repository.UserRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Service
class LeaderboardService(
    private val userRepository: UserRepository,
    private val userCatRepository: UserCatRepository
) {

    companion object {
        const val LEADERBOARD_LIMIT = 10
    }

    /**
     * Получить недельный рейтинг
     */
    fun getWeeklyLeaderboard(currentUserId: Long): LeaderboardResponse {
        val topUsers = userRepository.findTop10ByOrderByWeeklyStepsDesc().take(LEADERBOARD_LIMIT)
        val currentuserPosition = userRepository.findWeeklyRankByUserId(currentUserId)

        val entries = topUsers.mapIndexed { index, user ->
            LeaderboardEntry(
                rank = index + 1,
                userId = user.id,
                username = user.username,
                avatarUrl = user.avatarUrl,  // ДОБАВЛЕНО
                steps = user.weeklySteps,
                cats = userCatRepository.countByUserId(user.id),
                isCurrentUser = user.id == currentUserId
            )
        }

        val totalParticipants = userRepository.count().toInt()
        val period = formatWeekPeriod()  // ДОБАВЛЕНО

        return LeaderboardResponse(
            type = "weekly",
            period = period,  // ДОБАВЛЕНО
            entries = entries,
            currentUserPosition = currentuserPosition,
            totalParticipants = totalParticipants
        )
    }

    /**
     * Получить общий рейтинг (за всё время)
     */
    fun getAllTimeLeaderboard(currentUserId: Long): LeaderboardResponse {
        val topUsers = userRepository.findTop10ByOrderByTotalStepsDesc().take(LEADERBOARD_LIMIT)
        val currentUserPosition = userRepository.findTotalRankByUserId(currentUserId)

        val entries = topUsers.mapIndexed { index, user ->
            LeaderboardEntry(
                rank = index + 1,
                userId = user.id,
                username = user.username,
                avatarUrl = user.avatarUrl,  // ДОБАВЛЕНО
                steps = user.totalSteps,
                cats = userCatRepository.countByUserId(user.id),
                isCurrentUser = user.id == currentUserId
            )
        }

        val totalParticipants = userRepository.count().toInt()

        return LeaderboardResponse(
            type = "all_time",
            period = null,  // Для all-time период не нужен
            entries = entries,
            currentUserPosition = currentUserPosition,
            totalParticipants = totalParticipants
        )
    }

    /**
     * Сравнить себя с другим пользователем (другом)
     */
    fun compareWithFriend(currentUserId: Long, friendId: Long): FriendComparisonResponse {
        val currentUser = userRepository.findById(currentUserId)
            .orElseThrow { RuntimeException("User not found") }

        val friend = userRepository.findById(friendId)
            .orElseThrow { RuntimeException("Friend not found") }

        val currentUserCats = userCatRepository.countByUserId(currentUserId)
        val friendCats = userCatRepository.countByUserId(friendId)

        val currentUserRank = userRepository.findWeeklyRankByUserId(currentUserId)
        val friendRank = userRepository.findWeeklyRankByUserId(friendId)

        val difference = currentUser.weeklySteps - friend.weeklySteps

        val message = when {
            difference > 0 -> "Вы опережаете ${friend.username} на $difference шагов!"
            difference < 0 -> "${friend.username} опережает вас на ${-difference} шагов"
            else -> "Вы с ${friend.username} идёте ноздря в ноздрю!"
        }

        return FriendComparisonResponse(
            currentUser = LeaderboardEntry(
                rank = currentUserRank,
                userId = currentUser.id,
                username = currentUser.username,
                avatarUrl = currentUser.avatarUrl,  // ДОБАВЛЕНО
                steps = currentUser.weeklySteps,
                cats = currentUserCats,
                isCurrentUser = true
            ),
            friend = LeaderboardEntry(
                rank = friendRank,
                userId = friend.id,
                username = friend.username,
                avatarUrl = friend.avatarUrl,  // ДОБАВЛЕНО
                steps = friend.weeklySteps,
                cats = friendCats,
                isCurrentUser = false
            ),
            difference = difference,
            message = message
        )
    }

    /**
     * Поиск пользователей по имени
     */
    fun searchUsers(query: String, currentUserId: Long): List<LeaderboardEntry> {
        val allUsers = userRepository.findAll()

        return allUsers
            .filter { it.id != currentUserId && it.username.contains(query, ignoreCase = true) }
            .take(10)
            .map { user ->
                LeaderboardEntry(
                    rank = userRepository.findWeeklyRankByUserId(user.id),
                    userId = user.id,
                    username = user.username,
                    avatarUrl = user.avatarUrl,  // ДОБАВЛЕНО
                    steps = user.weeklySteps,
                    cats = userCatRepository.countByUserId(user.id),
                    isCurrentUser = false
                )
            }
    }

    /**
     * Получить позицию пользователя в рейтинге
     */
    fun getUserRank(userId: Long, type: String): Int {
        return when (type) {
            "weekly" -> userRepository.findWeeklyRankByUserId(userId)
            "all_time" -> userRepository.findTotalRankByUserId(userId)
            else -> throw RuntimeException("Invalid leaderboard type: $type")
        }
    }

    /**
     * Получить статистику по пользователям
     */
    fun getGlobalStats(): Map<String, Any> {
        val allUsers = userRepository.findAll()

        val totalSteps = allUsers.sumOf { it.totalSteps }
        val weeklySteps = allUsers.sumOf { it.weeklySteps }
        val averageSteps = if (allUsers.isNotEmpty()) {
            BigDecimal(totalSteps.toDouble() / allUsers.size)
                .setScale(0, RoundingMode.HALF_UP)
                .toInt()
        } else 0

        return mapOf(
            "totalUsers" to allUsers.size,
            "totalSteps" to totalSteps,
            "weeklySteps" to weeklySteps,
            "averageStepsPerUser" to averageSteps
        )
    }

    // ДОБАВЛЕНО: форматирование периода недели
    private fun formatWeekPeriod(): String {
        val today = LocalDate.now()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        val weekEnd = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))
        val formatter = DateTimeFormatter.ofPattern("dd.MM")
        return "${weekStart.format(formatter)} - ${weekEnd.format(formatter)}"
    }
}