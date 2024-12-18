package com.xcape.simplemmomod.di

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.room.Room
import com.google.gson.GsonBuilder
import com.xcape.simplemmomod.common.Endpoints.BASE_URL
import com.xcape.simplemmomod.common.UserTypeConverter
import com.xcape.simplemmomod.data.local.UserDatabase
import com.xcape.simplemmomod.data.remote.AutoSMMORequest
import com.xcape.simplemmomod.data.remote.UserApiService
import com.xcape.simplemmomod.data.repository.UserApiServiceRepositoryImpl
import com.xcape.simplemmomod.data.repository.UserRepositoryImpl
import com.xcape.simplemmomod.data.smmo_tasks.*
import com.xcape.simplemmomod.dataStore
import com.xcape.simplemmomod.domain.model.AppPreferences
import com.xcape.simplemmomod.domain.repository.UserApiServiceRepository
import com.xcape.simplemmomod.domain.repository.UserRepository
import com.xcape.simplemmomod.domain.smmo_tasks.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Singleton
    @Provides
    fun provideUserApiService(): UserApiService {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(UserApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideUserDatabase(
        application: Application,
    ): UserDatabase {
        return Room.databaseBuilder(
            application,
            UserDatabase::class.java,
            UserDatabase.USER_DATABASE
        )
            .addTypeConverter(UserTypeConverter())
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideUserRepository(
        userDatabase: UserDatabase,
        io: CoroutineDispatcher
    ): UserRepository = UserRepositoryImpl(
        userDao = userDatabase.userDao(),
        io = io
    )

    @Singleton
    @Provides
    fun provideUserApiServiceRepository(
        userApiService: UserApiService,
    ): UserApiServiceRepository = UserApiServiceRepositoryImpl(
        userApiService = userApiService
    )

    @Singleton
    @Provides
    fun provideAppPreferences(
        application: Application,
    ): DataStore<AppPreferences> {
        return application.dataStore
    }

    @Singleton
    @Provides
    fun provideAutoSMMORequester(
        userRepository: UserRepository,
        ioDispatcher: CoroutineDispatcher,
    ): AutoSMMORequest = AutoSMMORequest(
        userRepository = userRepository,
        ioDispatcher = ioDispatcher
    )

    @Singleton
    @Provides
    fun provideAutoSMMOLogger(
        userRepository: UserRepository,
    ): AutoSMMOLogger = AutoSMMOLoggerImpl(userRepository = userRepository)

    @Singleton
    @Provides
    fun provideTraveller(
        userRepository: UserRepository,
        userApiServiceRepository: UserApiServiceRepository,
        autoSMMORequest: AutoSMMORequest,
        lootActions: LootActions,
        npcActions: NpcActions,
        questActions: QuestActions,
        autoSMMOLogger: AutoSMMOLogger
    ): Traveller = TravellerImpl(
        userRepository = userRepository,
        userApiServiceRepository = userApiServiceRepository,
        autoSMMORequest = autoSMMORequest,
        autoSMMOLogger = autoSMMOLogger,
        lootActions = lootActions,
        npcActions = npcActions,
        questActions = questActions
    )

    @Singleton
    @Provides
    fun provideLootActions(
        userRepository: UserRepository,
        autoSMMORequest: AutoSMMORequest,
        autoSMMOLogger: AutoSMMOLogger
    ): LootActions = LootActionsImpl(
        userRepository = userRepository,
        autoSMMORequest = autoSMMORequest,
        autoSMMOLogger = autoSMMOLogger
    )

    @Singleton
    @Provides
    fun provideArenaActions(
        userRepository: UserRepository,
        autoSMMORequest: AutoSMMORequest,
        userApiServiceRepository: UserApiServiceRepository,
        lootActions: LootActions,
        autoSMMOLogger: AutoSMMOLogger,
    ): NpcActions = NpcActionsImpl(
        userRepository = userRepository,
        autoSMMORequest = autoSMMORequest,
        userApiServiceRepository = userApiServiceRepository,
        autoSMMOLogger = autoSMMOLogger,
        lootActions = lootActions
    )

    @Singleton
    @Provides
    fun provideQuestActions(
        userRepository: UserRepository,
        autoSMMORequest: AutoSMMORequest,
    ): QuestActions = QuestActionsImpl(
        userRepository = userRepository,
        autoSMMORequest = autoSMMORequest
    )

    @Singleton
    @Provides
    fun provideIODispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }
}