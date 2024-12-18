package com.xcape.simplemmomod.domain.smmo_tasks

interface LootActions {
    suspend fun obtainMaterials(materialUrl: String): Long
    suspend fun shouldEquipItem(itemId: String): Boolean
    suspend fun equipItem(itemId: String)
}