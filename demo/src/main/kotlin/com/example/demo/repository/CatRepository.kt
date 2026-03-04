package com.example.demo.repository

import com.example.demo.model.Cat
import org.springframework.data.jpa.repository.JpaRepository

interface CatRepository : JpaRepository<Cat, Long>