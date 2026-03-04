package com.example.demo.service

import com.example.demo.dto.*
import com.example.demo.model.UserCat
import com.example.demo.repository.CatRepository
import com.example.demo.repository.UserCatRepository
import com.example.demo.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CatService(
    private val catRepository: CatRepository,
    private val userCatRepository: UserCatRepository,
    private val userRepository: UserRepository
) {

    companion object {
        const val LEVEL_UP_BASE_COST = 50
        const val LEVEL_UP_MULTIPLIER = 2
    }

    /**
     * Получить всех котиков (для магазина)
     */
    fun getAllCats(userId: Long): List<CatResponse> {
        val allCats = catRepository.findAll()
        val ownedCatIds = userCatRepository.findByUserId(userId).map { it.catId }.toSet()

        return allCats.map { cat ->
            CatResponse(
                id = cat.id,
                name = cat.name,
                rarity = cat.rarity,
                power = cat.power,
                price = cat.price,
                description = cat.description,
                imageUrl = cat.imageUrl,
                cardColor = cat.cardColor,
                owned = cat.id in ownedCatIds
            )
        }
    }

    /**
     * Получить доступных для покупки котиков
     */
    fun getAvailableCats(userId: Long): List<CatResponse> {
        val availableCats = catRepository.findCatsNotOwnedByUser(userId)

        return availableCats.map { cat ->
            CatResponse(
                id = cat.id,
                name = cat.name,
                rarity = cat.rarity,
                power = cat.power,
                price = cat.price,
                description = cat.description,
                imageUrl = cat.imageUrl,
                cardColor = cat.cardColor,
                owned = false
            )
        }
    }

    /**
     * Получить котиков пользователя
     */
    fun getUserCats(userId: Long): List<UserCatResponse> {
        val userCats = userCatRepository.findByUserId(userId)
        val catIds = userCats.map { it.catId }
        val cats = catRepository.findAllById(catIds)
        val catMap = cats.associateBy { it.id }

        return userCats.map { userCat ->
            val cat = catMap[userCat.catId]!!
            val upgradeCost = calculateUpgradeCost(userCat.level)

            UserCatResponse(
                id = userCat.id,
                catId = cat.id,
                name = cat.name,
                rarity = cat.rarity,
                power = cat.power,
                level = userCat.level,
                totalPower = cat.power * userCat.level,
                description = cat.description,
                imageUrl = cat.imageUrl,
                cardColor = cat.cardColor,
                upgradeCost = upgradeCost
            )
        }
    }

    /**
     * Получить детальную информацию о котике (для экрана просмотра)
     */
    fun getCatDetail(userId: Long, catId: Long): CatDetailResponse {
        val cat = catRepository.findById(catId)
            .orElseThrow { RuntimeException("Котик не найден") }

        val userCat = userCatRepository.findByUserIdAndCatId(userId, catId)
        val owned = userCat != null

        return if (owned && userCat != null) {
            // Котик куплен - показываем полную информацию
            val upgradeCost = calculateUpgradeCost(userCat.level)

            CatDetailResponse(
                id = userCat.id,
                catId = cat.id,
                name = cat.name,
                rarity = cat.rarity,
                power = cat.power,
                level = userCat.level,
                totalPower = cat.power * userCat.level,
                description = cat.description,
                imageUrl = cat.imageUrl,
                cardColor = cat.cardColor,
                upgradeCost = upgradeCost,
                canUpgrade = true,
                owned = true
            )
        } else {
            // Котик не куплен - показываем базовую информацию
            CatDetailResponse(
                id = 0,
                catId = cat.id,
                name = cat.name,
                rarity = cat.rarity,
                power = cat.power,
                level = 0,
                totalPower = cat.power,
                description = cat.description,
                imageUrl = cat.imageUrl,
                cardColor = cat.cardColor,
                upgradeCost = 0,
                canUpgrade = false,
                owned = false
            )
        }
    }

    /**
     * Купить котика
     */
    @Transactional
    fun buyCat(userId: Long, catId: Long): BuyCatResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("Пользователь не найден") }

        val cat = catRepository.findById(catId)
            .orElseThrow { RuntimeException("Котик не найден") }

        if (userCatRepository.existsByUserIdAndCatId(userId, catId)) {
            return BuyCatResponse(
                success = false,
                message = "У вас уже есть этот котик!",
                cat = null,
                remainingCoins = user.coins
            )
        }

        if (user.coins < cat.price) {
            return BuyCatResponse(
                success = false,
                message = "Недостаточно монет! Нужно ${cat.price}, у вас ${user.coins}",
                cat = null,
                remainingCoins = user.coins
            )
        }

        user.coins -= cat.price
        userRepository.save(user)

        val userCat = UserCat(
            userId = userId,
            catId = catId,
            level = 1
        )
        userCatRepository.save(userCat)

        val userCatResponse = UserCatResponse(
            id = userCat.id,
            catId = cat.id,
            name = cat.name,
            rarity = cat.rarity,
            power = cat.power,
            level = 1,
            totalPower = cat.power,
            description = cat.description,
            imageUrl = cat.imageUrl,
            cardColor = cat.cardColor,
            upgradeCost = calculateUpgradeCost(1)
        )

        return BuyCatResponse(
            success = true,
            message = "Поздравляем! Вы купили ${cat.name}!",
            cat = userCatResponse,
            remainingCoins = user.coins
        )
    }

    /**
     * Повысить уровень котика
     */
    @Transactional
    fun upgradeCat(userId: Long, userCatId: Long): UpgradeCatResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { RuntimeException("Пользователь не найден") }

        val userCat = userCatRepository.findById(userCatId)
            .orElseThrow { RuntimeException("Котик не найден в вашей коллекции") }

        if (userCat.userId != userId) {
            throw RuntimeException("Этот котик вам не принадлежит!")
        }

        val cat = catRepository.findById(userCat.catId)
            .orElseThrow { RuntimeException("Данные котика не найдены") }

        val cost = calculateUpgradeCost(userCat.level)

        if (user.coins < cost) {
            return UpgradeCatResponse(
                success = false,
                message = "Недостаточно монет! Нужно $cost, у вас ${user.coins}",
                cat = null,
                cost = cost,
                remainingCoins = user.coins
            )
        }

        user.coins -= cost
        userRepository.save(user)

        val newLevel = userCat.level + 1
        val updatedUserCat = userCat.copy(level = newLevel)
        userCatRepository.save(updatedUserCat)

        val userCatResponse = UserCatResponse(
            id = userCat.id,
            catId = cat.id,
            name = cat.name,
            rarity = cat.rarity,
            power = cat.power,
            level = newLevel,
            totalPower = cat.power * newLevel,
            description = cat.description,
            imageUrl = cat.imageUrl,
            cardColor = cat.cardColor,
            upgradeCost = calculateUpgradeCost(newLevel)
        )

        return UpgradeCatResponse(
            success = true,
            message = "${cat.name} повышен до уровня $newLevel!",
            cat = userCatResponse,
            cost = cost,
            remainingCoins = user.coins
        )
    }

    /**
     * Получить общую силу всех котиков пользователя
     */
    fun getTotalPower(userId: Long): Int {
        return userCatRepository.getTotalPowerByUserId(userId) ?: 0
    }

    /**
     * Получить самого сильного котика пользователя
     */
    fun getStrongestCat(userId: Long): UserCatResponse? {
        val userCat = userCatRepository.findStrongestCatByUserId(userId) ?: return null
        val cat = catRepository.findById(userCat.catId).orElse(null) ?: return null

        return UserCatResponse(
            id = userCat.id,
            catId = cat.id,
            name = cat.name,
            rarity = cat.rarity,
            power = cat.power,
            level = userCat.level,
            totalPower = cat.power * userCat.level,
            description = cat.description,
            imageUrl = cat.imageUrl,
            cardColor = cat.cardColor,
            upgradeCost = calculateUpgradeCost(userCat.level)
        )
    }
    /**
     * Получить информацию о котике по ID (без проверки владения)
     */
    fun getCatInfo(catId: Long): CatResponse {
        val cat = catRepository.findById(catId)
            .orElseThrow { RuntimeException("Котик не найден") }

        return CatResponse(
            id = cat.id,
            name = cat.name,
            rarity = cat.rarity,
            power = cat.power,
            price = cat.price,
            description = cat.description,
            imageUrl = cat.imageUrl,
            cardColor = cat.cardColor,
            owned = false
        )
    }
    
    private fun calculateUpgradeCost(currentLevel: Int): Int {
        return LEVEL_UP_BASE_COST * (LEVEL_UP_MULTIPLIER.pow(currentLevel - 1))
    }

    private fun Int.pow(exponent: Int): Int {
        var result = 1
        repeat(exponent) { result *= this }
        return result
    }
}