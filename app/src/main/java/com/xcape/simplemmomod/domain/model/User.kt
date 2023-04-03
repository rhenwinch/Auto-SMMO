package com.xcape.simplemmomod.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.xcape.simplemmomod.common.UserTypeConverter

@Entity(tableName = "users")
@TypeConverters(UserTypeConverter::class)
data class User(
    @PrimaryKey(autoGenerate = false) val id: Int = 0,
    @ColumnInfo val avatar: String = "",
    @ColumnInfo val username: String = "",
    @ColumnInfo val notifications: Int = 0,
    @ColumnInfo val messages: Int = 0,
    @ColumnInfo val characterUpgrades: Int = 0,
    @ColumnInfo val toEquipItems: List<String> = emptyList(),
    @ColumnInfo val gold: Long = 0,
    @ColumnInfo val level: Int = 0,
    @ColumnInfo val questEnergy: Int = 0,
    @ColumnInfo val maxQuestEnergy: Int = 0,
    @ColumnInfo val battleEnergy: Int = 0,
    @ColumnInfo val maxBattleEnergy: Int = 0,
    @ColumnInfo val needsVerification: Boolean = false,
    @ColumnInfo val currentHealthPercentage: Int = 100,

    // Total Stats
    @ColumnInfo val totalSteps: Int = 0,
    @ColumnInfo val totalQuests: Int = 0,
    @ColumnInfo val totalBattles: Int = 0,
    @ColumnInfo val totalItemsFound: Int = 0,
    @ColumnInfo val totalMaterialsFound: Int = 0,

    // Daily Stats
    @ColumnInfo val dailySteps: Int = 0,
    @ColumnInfo val dailyQuests: Int = 0,
    @ColumnInfo val dailyBattles: Int = 0,
    @ColumnInfo val dailyItemsFound: Int = 0,
    @ColumnInfo val dailyMaterialsFound: Int = 0,
    @ColumnInfo val dailyResetTime: Long = 0L,
    @ColumnInfo val travelLog: String = "",

    @ColumnInfo val loggedIn: Boolean = false,
    @ColumnInfo val userAgent: String = "",
    @ColumnInfo val apiToken: String = "",
    @ColumnInfo val csrfToken: String = "",
    @ColumnInfo val cookie: String = ""
)