package com.example.demo.service

import com.example.demo.dto.*
import com.example.demo.model.DailyStep
import com.example.demo.repository.CatRepository
import com.example.demo.repository.DailyStepRepository
import com.example.demo.repository.UserCatRepository
import com.example.demo.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Service
class StepService(
    private val dailyStepRepository: DailyStepRepository,
    private val userRepository: UserRepository,
    private val userCatRepository: UserCatRepository,
    private val catRepository: CatRepository
) {

    companion object {
        const val SNAIL_THRESHOLD = 100
        const val COINS_PER_1000_STEPS = 10
        const val COINS_GOAL_BONUS = 50
        const val MAX_SNAILS_BEFORE_CAT_LOSS = 3
    }

    @Transactional
    fun syncSteps(userId: Long, request: SyncStepsRequest): SyncStepsResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("User not found") }

        val date = LocalDate.parse(request.date)

        val existingStep = dailyStepRepository.findByUserIdAndDate(userId, date)
        val previousSteps = existingStep?.steps ?: 0

        val newSteps = maxOf(previousSteps, request.steps)
        val addedSteps = newSteps - previousSteps

        if (existingStep != null) {
            existingStep.steps = newSteps
            dailyStepRepository.save(existingStep)
        } else {
            dailyStepRepository.save(DailyStep(
                userId = userId,
                date = date,
                steps = newSteps
            ))
        }

        val today = LocalDate.now()
        if (date == today) {
            val coinsEarned = calculateCoinsForSteps(addedSteps)
            val goalReached = newSteps >= user.goal && previousSteps < user.goal
            val goalBonus = if (goalReached) COINS_GOAL_BONUS else 0

            user.coins += coinsEarned + goalBonus
            user.totalSteps += addedSteps
            user.weeklySteps += addedSteps

            if (user.weeklySteps > user.recordWeek) {
                user.recordWeek = user.weeklySteps
            }
            userRepository.save(user)

            return SyncStepsResponse(
                success = true,
                stepsAdded = addedSteps,
                coinsEarned = coinsEarned + goalBonus,
                goalReached = goalReached,
                message = "Синхронизировано $addedSteps шагов"
            )
        }

        return SyncStepsResponse(
            success = true,
            stepsAdded = addedSteps,
            coinsEarned = 0,
            goalReached = false,
            message = "Синхронизировано $addedSteps шагов за прошлый день"
        )
    }

    @Transactional
    fun addSteps(userId: Long, steps: Int): StepResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("User not found") }

        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        var dailyStep = dailyStepRepository.findByUserIdAndDate(userId, today)
        val previousSteps = dailyStep?.steps ?: 0
        val newSteps = previousSteps + steps

        val newCoinsFromSteps = calculateCoinsForSteps(steps)
        val goalReached = newSteps >= user.goal && previousSteps < user.goal
        val goalBonus = if (goalReached) COINS_GOAL_BONUS else 0
        val totalCoinsEarned = newCoinsFromSteps + goalBonus

        if (dailyStep != null) {
            dailyStep = dailyStep.copy(steps = newSteps)
        } else {
            dailyStep = DailyStep(userId = userId, date = today, steps = newSteps)
        }
        dailyStepRepository.save(dailyStep)

        user.coins += totalCoinsEarned
        user.totalSteps += steps
        user.weeklySteps += steps

        if (user.weeklySteps > user.recordWeek) {
            user.recordWeek = user.weeklySteps
        }
        userRepository.save(user)

        val progress = calculateProgress(newSteps, user.goal)
        val remaining = maxOf(0, user.goal - newSteps)

        return StepResponse(
            date = today.format(dateFormatter),
            steps = newSteps,
            coinsEarned = totalCoinsEarned,
            goalReached = goalReached,
            progress = progress,
            remainingToGoal = remaining
        )
    }

    @Transactional
    fun checkSnailStatus(userId: Long): SnailNotification {
        val yesterday = LocalDate.now().minusDays(1)
        val dailyStep = dailyStepRepository.findByUserIdAndDate(userId, yesterday)

        if (dailyStep == null || dailyStep.steps >= SNAIL_THRESHOLD) {
            return SnailNotification(
                hasSnail = false,
                snailCount = 0,
                message = "Отлично! Вы прошли достаточно шагов вчера!"
            )
        }

        val weekAgo = LocalDate.now().minusDays(7)
        val snailCount = dailyStepRepository.countSnailDays(userId, weekAgo, yesterday)

        var catLost = false
        var lostCatName: String? = null
        var message = "О нет! Вчера вы набрали меньше $SNAIL_THRESHOLD шагов. " +
                "У вас $snailCount улиток за последние 7 дней."

        if (snailCount >= MAX_SNAILS_BEFORE_CAT_LOSS) {
            val lostCat = removeRandomCat(userId)
            if (lostCat != null) {
                catLost = true
                lostCatName = lostCat.name
                message = "У вас $snailCount улиток! Вы потеряли котика: ${lostCat.name} 😢"
            }
        }

        return SnailNotification(
            hasSnail = true,
            snailCount = snailCount,
            message = message,
            catLost = catLost,
            lostCatName = lostCatName
        )
    }

    fun getTodayStats(userId: Long): StepResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("User not found") }

        val today = LocalDate.now()
        val dailyStep = dailyStepRepository.findByUserIdAndDate(userId, today)
        val steps = dailyStep?.steps ?: 0
        val remaining = maxOf(0, user.goal - steps)

        return StepResponse(
            date = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            steps = steps,
            coinsEarned = 0,
            goalReached = steps >= user.goal,
            progress = calculateProgress(steps, user.goal),
            remainingToGoal = remaining
        )
    }

    fun getWeeklyStats(userId: Long): WeeklyStatsResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("User not found") }

        val today = LocalDate.now()
        val weekStart = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        val weekEnd = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))

        val dailySteps = dailyStepRepository.findByUserIdAndDateBetween(userId, weekStart, weekEnd)
        val stepsMap = dailySteps.associate { it.date to it.steps }

        val dayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        val dailyStats = mutableListOf<DayStats>()
        var totalWeekSteps = 0

        var dayIndex = 0
        var currentDay = weekStart
        while (!currentDay.isAfter(weekEnd)) {
            val steps = stepsMap[currentDay] ?: 0
            totalWeekSteps += steps
            dailyStats.add(DayStats(
                date = currentDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                dayName = dayNames[dayIndex],
                steps = steps,
                goalReached = steps >= user.goal
            ))
            currentDay = currentDay.plusDays(1)
            dayIndex++
        }

        val daysActive = dailySteps.count { it.steps > 0 }
        val dailyAverage = if (daysActive > 0) totalWeekSteps.toDouble() / 7 else 0.0

        // Находим максимальное количество шагов за неделю
        val recordSteps = dailyStats.maxOfOrNull { it.steps } ?: 0

        return WeeklyStatsResponse(
            totalSteps = totalWeekSteps,
            dailyAverage = BigDecimal(dailyAverage).setScale(1, RoundingMode.HALF_UP).toDouble(),
            daysActive = daysActive,
            coinsEarned = calculateCoinsForSteps(totalWeekSteps),
            recordSteps = recordSteps,  // ДОБАВЛЕНО
            dailyStats = dailyStats
        )
    }

    fun getStatsByPeriod(userId: Long, startDate: LocalDate, endDate: LocalDate): Int {
        return dailyStepRepository.sumStepsByUserIdAndDateBetween(userId, startDate, endDate) ?: 0
    }

    @Transactional
    fun setGoal(userId: Long, goal: Int): GoalResponse {
        if (goal < 1000 || goal > 50000) {
            throw RuntimeException("Цель должна быть от 1000 до 50000 шагов")
        }

        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("User not found") }

        user.goal = goal
        userRepository.save(user)

        return GoalResponse(
            goal = goal,
            message = "Цель установлена: $goal шагов в день"
        )
    }

    private fun calculateCoinsForSteps(steps: Int): Int {
        return (steps / 1000) * COINS_PER_1000_STEPS
    }

    private fun calculateProgress(steps: Int, goal: Int): Double {
        if (goal == 0) return 0.0
        val progress = (steps.toDouble() / goal) * 100
        return BigDecimal(minOf(progress, 100.0))
            .setScale(1, RoundingMode.HALF_UP)
            .toDouble()
    }

    private fun removeRandomCat(userId: Long): com.example.demo.model.Cat? {
        val userCats = userCatRepository.findByUserId(userId)
        if (userCats.isEmpty()) return null

        val randomCat = userCats.random()
        val cat = catRepository.findById(randomCat.catId).orElse(null)

        userCatRepository.delete(randomCat)

        return cat
    }
}