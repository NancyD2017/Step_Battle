package com.example.demo.repository

import com.example.demo.model.UserCat
import org.springframework.data.jpa.repository.JpaRepository

interface UserCatRepository : JpaRepository<UserCat, Long>