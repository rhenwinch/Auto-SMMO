package com.xcape.simplemmomod.data.local

import androidx.room.*
import com.xcape.simplemmomod.domain.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    suspend fun getAll(): List<User>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun findByUserId(id: Int): User?

    @Query("SELECT * FROM users WHERE loggedIn = 1")
    suspend fun findByLoggedIn(): User?

    @Query("SELECT * FROM users WHERE loggedIn = 1")
    fun findByLoggedInReturnsFlow(): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUser(user: User)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUser(user: User)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteByUserId(id: Int)
}