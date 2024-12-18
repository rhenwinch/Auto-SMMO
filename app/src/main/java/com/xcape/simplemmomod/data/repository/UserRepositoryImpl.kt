package com.xcape.simplemmomod.data.repository

import com.xcape.simplemmomod.data.local.UserDao
import com.xcape.simplemmomod.domain.model.User
import com.xcape.simplemmomod.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val io: CoroutineDispatcher
) : UserRepository {
    override suspend fun getAll(): List<User> {
        return withContext(io) {
            userDao.getAll()
        }
    }

    override suspend fun getUserById(id: Int): User? {
        return withContext(io) {
            userDao.findByUserId(id = id)
        }
    }

    override suspend fun getLoggedInUser(): User? {
        return withContext(io) {
            userDao.findByLoggedIn()
        }
    }

    override fun getFlowLoggedInUser(): Flow<User?> {
        return userDao.findByLoggedInReturnsFlow()
    }

    override suspend fun addUser(user: User) {
        if(user.id == 0)
            return

        withContext(io) {
            userDao.addUser(user = user)
        }
    }

    override suspend fun updateUser(user: User) {
        withContext(io) {
            userDao.updateUser(user = user)
        }
    }

    override suspend fun delete(userId: Int) {
        withContext(io) {
            userDao.deleteByUserId(userId)
        }
    }
}