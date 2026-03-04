package com.example.demo.repository

import com.example.demo.model.DailyStep
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface DailyStepRepository : JpaRepository<DailyStep, Long> {

    fun findByUserIdAndDate(userId: Long, date: LocalDate): DailyStep?

    fun findByUserIdAndDateBetween(userId: Long, startDate: LocalDate, endDate: LocalDate): List<DailyStep>

    fun findByUserId(userId: Long): List<DailyStep>

    @Query("SELECT SUM(d.steps) FROM DailyStep d WHERE d.userId = :userId AND d.date BETWEEN :startDate AND :endDate")
    fun sumStepsByUserIdAndDateBetween(
        @Param("userId") userId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): Int?

    @Query("SELECT COUNT(d) FROM DailyStep d WHERE d.userId = :userId AND d.date BETWEEN :startDate AND :endDate AND d.steps > 0")
    fun countActiveDaysByUserIdAndDateBetween(
        @Param("userId") userId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): Int

    @Query("SELECT COUNT(d) FROM DailyStep d WHERE d.userId = :userId AND d.date BETWEEN :startDate AND :endDate AND d.steps >= :goal")
    fun countGoalReachedDays(
        @Param("userId") userId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
        @Param("goal") goal: Int
    ): Int

    @Query("SELECT COUNT(d) FROM DailyStep d WHERE d.userId = :userId AND d.date BETWEEN :startDate AND :endDate AND d.steps < 100")
    fun countSnailDays(
        @Param("userId") userId: Long,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): Int

    fun findTop10ByUserIdOrderByDateDesc(userId: Long): List<DailyStep>
}