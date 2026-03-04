package com.example.demo.repository

import com.example.demo.model.UserCat
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserCatRepository : JpaRepository<UserCat, Long> {

    fun findByUserId(userId: Long): List<UserCat>

    fun findByUserIdAndCatId(userId: Long, catId: Long): UserCat?

    fun countByUserId(userId: Long): Int

    fun existsByUserIdAndCatId(userId: Long, catId: Long): Boolean

    @Query(value = "SELECT SUM(uc.level * c.power) FROM user_cats uc JOIN cats c ON uc.cat_id = c.id WHERE uc.user_id = :userId", nativeQuery = true)
    fun getTotalPowerByUserId(@Param("userId") userId: Long): Int?

    @Query(value = "SELECT uc.* FROM user_cats uc JOIN cats c ON uc.cat_id = c.id WHERE uc.user_id = :userId AND c.rarity = :rarity", nativeQuery = true)
    fun findByUserIdAndRarity(@Param("userId") userId: Long, @Param("rarity") rarity: String): List<UserCat>
    @Query(value = "SELECT uc.* FROM user_cats uc JOIN cats c ON uc.cat_id = c.id WHERE uc.user_id = :userId ORDER BY (uc.level * c.power) DESC LIMIT 1", nativeQuery = true)
    fun findStrongestCatByUserId(@Param("userId") userId: Long): UserCat?
}