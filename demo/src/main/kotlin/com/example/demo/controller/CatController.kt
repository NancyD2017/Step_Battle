package com.example.demo.controller

import com.example.demo.dto.*
import com.example.demo.repository.UserRepository
import com.example.demo.service.CatService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cats")
class CatController(
    private val catService: CatService,
    private val userRepository: UserRepository
) {

    /**
     * Получить всех котиков (магазин)
     * GET /api/cats/shop
     */
    @GetMapping("/shop")
    fun getAllCats(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<CatResponse>> {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("User not found")

        val cats = catService.getAllCats(user.id)
        return ResponseEntity.ok(cats)
    }

    /**
     * Получить доступных для покупки котиков
     * GET /api/cats/available
     */
    @GetMapping("/available")
    fun getAvailableCats(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<CatResponse>> {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("User not found")

        val cats = catService.getAvailableCats(user.id)
        return ResponseEntity.ok(cats)
    }

    /**
     * Получить котиков пользователя (коллекция)
     * GET /api/cats/my
     */
    @GetMapping("/my")
    fun getMyCats(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<UserCatResponse>> {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("User not found")

        val cats = catService.getUserCats(user.id)
        return ResponseEntity.ok(cats)
    }

    /**
     * Получить информацию о конкретном котике
     * GET /api/cats/{id}
     */
    @GetMapping("/{id}")
    fun getCatInfo(@PathVariable id: Long): ResponseEntity<CatResponse> {
        val cat = catService.getCatInfo(id)
        return ResponseEntity.ok(cat)
    }

    /**
     * Купить котика
     * POST /api/cats/buy
     */
    @PostMapping("/buy")
    fun buyCat(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: BuyCatRequest
    ): ResponseEntity<BuyCatResponse> {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("User not found")

        val response = catService.buyCat(user.id, request.catId)
        return ResponseEntity.ok(response)
    }

    /**
     * Повысить уровень котика
     * POST /api/cats/upgrade
     */
    @PostMapping("/upgrade")
    fun upgradeCat(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: UpgradeCatRequest
    ): ResponseEntity<UpgradeCatResponse> {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("User not found")

        val response = catService.upgradeCat(user.id, request.userCatId)
        return ResponseEntity.ok(response)
    }

    /**
     * Получить общую силу котиков
     * GET /api/cats/total-power
     */
    @GetMapping("/total-power")
    fun getTotalPower(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Map<String, Int>> {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("User not found")

        val power = catService.getTotalPower(user.id)
        return ResponseEntity.ok(mapOf("totalPower" to power))
    }

    /**
     * Получить самого сильного котика
     * GET /api/cats/strongest
     */
    @GetMapping("/strongest")
    fun getStrongestCat(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<UserCatResponse?> {
        val user = userRepository.findByUsername(userDetails.username)
            ?: throw RuntimeException("User not found")

        val cat = catService.getStrongestCat(user.id)
        return ResponseEntity.ok(cat)
    }
}