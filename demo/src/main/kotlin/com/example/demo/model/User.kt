package com.example.demo.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val username: String,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    val password: String,

    val role: String = "USER",

    var coins: Int = 0,
    var weeklySteps: Int = 0,
    var totalSteps: Int = 0,
    var recordWeek: Int = 0,
    var goal: Int = 8000,
    var snailCount: Int = 0,

    var avatarUrl: String? = null
)