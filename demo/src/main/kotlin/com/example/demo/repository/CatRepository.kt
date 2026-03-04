package com.example.demo.repository

import com.example.demo.model.Cat
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CatRepository : JpaRepository<Cat, Long> {

    // Найти котиков по редкости
    fun findByRarity(rarity: String): List<Cat>

    // Найти котиков дешевле определённой цены
    fun findByPriceLessThanEqual(price: Int): List<Cat>

    // Найти котиков по цене в диапазоне
    fun findByPriceBetween(minPrice: Int, maxPrice: Int): List<Cat>

    // Найти котиков, которых у пользователя ещё нет
    @Query("""
        SELECT c FROM Cat c WHERE c.id NOT IN (
            SELECT uc.catId FROM UserCat uc WHERE uc.userId = :userId
        )
    """)
    fun findCatsNotOwnedByUser(@Param("userId") userId: Long): List<Cat>

    // Проверить, есть ли у пользователя конкретный котик
    @Query("""
        SELECT CASE WHEN COUNT(uc) > 0 THEN true ELSE false END 
        FROM UserCat uc WHERE uc.userId = :userId AND uc.catId = :catId
    """)
    fun isCatOwnedByUser(@Param("userId") userId: Long, @Param("catId") catId: Long): Boolean
}