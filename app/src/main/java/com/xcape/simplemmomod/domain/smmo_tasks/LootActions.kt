package com.xcape.simplemmomod.domain.smmo_tasks

interface LootActions {
    suspend fun obtainMaterials(materialId: String): Long
    suspend fun shouldEquipItem(itemId: String): Boolean
    suspend fun equipItem(itemId: String)
}