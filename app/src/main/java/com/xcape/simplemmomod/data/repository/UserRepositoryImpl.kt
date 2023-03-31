package com.xcape.simplemmomod.data.repository

import com.xcape.simplemmomod.data.local.UserDao
import com.xcape.simplemmomod.domain.model.User
import com.xcape.simplemmomod.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {
    override suspend fun getAll(): List<User> {
        return userDao.getAll()
    }

    override suspend fun getUserById(id: Int): User? {
        return userDao.findByUserId(id = id)
    }

    override suspend fun getLoggedInUser(): User? {
        return userDao.findByLoggedIn()
    }

    override fun getFlowLoggedInUser(): Flow<User?> {
        return userDao.findByLoggedInReturnsFlow()
    }

    override suspend fun addUser(user: User) {
        if(user.id == 0)
            return

        userDao.addUser(user = user)
    }

    override suspend fun updateUser(user: User) {
        userDao.updateUser(user = user)
    }

    override fun delete(userId: Int) {
        userDao.deleteByUserId(userId)
    }
}