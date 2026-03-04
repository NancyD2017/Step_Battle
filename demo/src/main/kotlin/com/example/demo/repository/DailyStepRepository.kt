package com.example.demo.repository

import com.example.demo.model.DailyStep
import org.springframework.data.jpa.repository.JpaRepository

interface DailyStepRepository : JpaRepository<DailyStep, Long>