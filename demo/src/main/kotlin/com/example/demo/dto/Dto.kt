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
    val token: String,
    val user: UserInfo? = null,
    val message: String? = null
)

data class UserInfo(
    val id: Long,
    val username: String,
    val email: String? = null,
    val coins: Int,
    val avatarUrl: String? = null
)

data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val coins: Int,
    val weeklySteps: Int,
    val totalSteps: Int,
    val goal: Int,
    val recordWeek: Int,
    val snailCount: Int = 0,
    val avatarUrl: String? = null
)

data class UserProfileResponse(
    val id: Long,
    val username: String,
    val coins: Int,
    val weeklySteps: Int,
    val totalSteps: Int,
    val goal: Int,
    val recordWeek: Int,
    val snailCount: Int,
    val cats: List<UserCatResponse>,
    val todaySteps: Int,
    val todayProgress: Double,
    val avatarUrl: String? = null
)

data class AddStepsRequest(
    val steps: Int
)

data class StepResponse(
    val date: String,
    val steps: Int,
    val coinsEarned: Int,
    val goalReached: Boolean,
    val progress: Double,
    val remainingToGoal: Int
)

data class WeeklyStatsResponse(
    val totalSteps: Int,
    val dailyAverage: Double,
    val daysActive: Int,
    val coinsEarned: Int,
    val recordSteps: Int,
    val dailyStats: List<DayStats>
)

data class DayStats(
    val date: String,
    val dayName: String, // "Пн", "Вт" и т.д.
    val steps: Int,
    val goalReached: Boolean
)

data class SnailNotification(
    val hasSnail: Boolean,
    val snailCount: Int,
    val message: String,
    val catLost: Boolean = false,
    val lostCatName: String? = null
)

data class CatResponse(
    val id: Long,
    val name: String,
    val rarity: String,
    val power: Int,
    val price: Int,
    val description: String,
    val imageUrl: String,
    val cardColor: String,
    val owned: Boolean = false
)

data class UserCatResponse(
    val id: Long,
    val catId: Long,
    val name: String,
    val rarity: String,
    val power: Int,
    val level: Int,
    val totalPower: Int,
    val description: String,
    val imageUrl: String,
    val cardColor: String,
    val upgradeCost: Int
)

data class CatDetailResponse(
    val id: Long,
    val catId: Long,
    val name: String,
    val rarity: String,
    val power: Int,
    val level: Int,
    val totalPower: Int,
    val description: String,
    val imageUrl: String,
    val cardColor: String,
    val upgradeCost: Int,
    val canUpgrade: Boolean,
    val owned: Boolean
)

data class BuyCatRequest(
    val catId: Long
)

data class BuyCatResponse(
    val success: Boolean,
    val message: String,
    val cat: UserCatResponse?,
    val remainingCoins: Int
)

data class UpgradeCatRequest(
    val userCatId: Long
)

data class UpgradeCatResponse(
    val success: Boolean,
    val message: String,
    val cat: UserCatResponse?,
    val cost: Int,
    val remainingCoins: Int
)

data class LeaderboardEntry(
    val rank: Int,
    val userId: Long,
    val username: String,
    val avatarUrl: String?,
    val steps: Int,
    val cats: Int,
    val isCurrentUser: Boolean = false
)

data class LeaderboardResponse(
    val type: String, // "weekly" or "all_time"
    val period: String?, // "17.08 - 23.08" для UI
    val entries: List<LeaderboardEntry>,
    val currentUserPosition: Int?,
    val totalParticipants: Int
)

data class LeaderboardByDateRequest(
    val startDate: String, // "2025-08-17"
    val endDate: String    // "2025-08-23"
)

data class FriendComparisonResponse(
    val currentUser: LeaderboardEntry,
    val friend: LeaderboardEntry?,
    val difference: Int,
    val message: String
)

data class SetGoalRequest(
    val goal: Int
)

data class GoalResponse(
    val goal: Int,
    val message: String
)

data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: String = java.time.LocalDateTime.now().toString()
)

data class FriendRequestResponse(
    val success: Boolean,
    val message: String,
    val friendshipId: Long?
)

data class FriendResponse(
    val id: Long,
    val username: String,
    val avatarUrl: String?,
    val weeklySteps: Int,
    val totalSteps: Int,
    val cats: Int,
    val friendshipSince: String?,
    val isOnline: Boolean = false
)

data class FriendRequestInfo(
    val id: Long,
    val fromUserId: Long,
    val fromUsername: String,
    val fromAvatarUrl: String?,
    val createdAt: String
)

data class UserSearchResult(
    val id: Long,
    val username: String,
    val avatarUrl: String?,
    val weeklySteps: Int,
    val isFriend: Boolean,
    val requestStatus: String // "friends", "pending", "none"
)

data class SendFriendRequest(
    val friendId: Long
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)

data class ForgotPasswordResponse(
    val success: Boolean,
    val message: String
)

data class PushNotificationRequest(
    val userId: Long,
    val title: String,
    val message: String,
    val type: String // "friend_request", "step_challenge", "snail_warning"
)

data class NotificationResponse(
    val id: Long,
    val title: String,
    val message: String,
    val type: String,
    val read: Boolean,
    val createdAt: String
)

data class SyncStepsRequest(
    val steps: Int,
    val date: String, // "2025-08-30"
    val source: String // "google_fit", "health_connect", "manual"
)

data class SyncStepsResponse(
    val success: Boolean,
    val stepsAdded: Int,
    val coinsEarned: Int,
    val goalReached: Boolean,
    val message: String
)