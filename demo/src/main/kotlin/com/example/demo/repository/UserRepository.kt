package com.example.demo.repository

import com.example.demo.model.User
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface UserRepository : JpaRepository<User, Long> {

    fun findByUsername(username: String): User?

    fun findByEmail(email: String): User?

    fun existsByUsername(username: String): Boolean

    fun existsByEmail(email: String): Boolean

    fun findTop10ByOrderByWeeklyStepsDesc(): List<User>

    fun findTop10ByOrderByTotalStepsDesc(): List<User>
    @Query("SELECT COUNT(u) + 1 FROM User u WHERE u.weeklySteps > (SELECT us.weeklySteps FROM User us WHERE us.id = :userId)")
    fun findWeeklyRankByUserId(@Param("userId") userId: Long): Int

    @Query("SELECT COUNT(u) + 1 FROM User u WHERE u.totalSteps > (SELECT us.totalSteps FROM User us WHERE us.id = :userId)")
    fun findTotalRankByUserId(@Param("userId") userId: Long): Int
}