package com.xcape.simplemmomod.domain.repository

import com.xcape.simplemmomod.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getAll(): List<User>
    suspend fun getUserById(id: Int): User?
    suspend fun getLoggedInUser(): User?
    fun getFlowLoggedInUser(): Flow<User?>
    suspend fun addUser(user: User)
    suspend fun updateUser(user: User)
    fun delete(userId: Int)
}