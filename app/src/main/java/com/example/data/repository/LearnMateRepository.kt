package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.data.local.ConversationEntity
import com.example.data.local.LearnMateDao
import com.example.data.local.MessageEntity
import com.example.data.local.StudentMemoryEntity
import com.example.data.remote.GeminiService
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class LearnMateRepository(
    private val context: Context,
    private val dao: LearnMateDao,
    private val geminiService: GeminiService = GeminiService()
) {
    private val prefs = context.getSharedPreferences("learnmate_settings", Context.MODE_PRIVATE)

    // Conversations
    fun getAllConversations(): Flow<List<ConversationEntity>> = dao.getAllConversations()

    suspend fun getConversationById(id: String): ConversationEntity? = dao.getConversationById(id)

    suspend fun createNewConversation(initialTitle: String = "New Conversation"): ConversationEntity {
        val conv = ConversationEntity(
            id = UUID.randomUUID().toString(),
            title = initialTitle,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        dao.insertConversation(conv)
        return conv
    }

    suspend fun updateConversationTitle(id: String, newTitle: String) {
        val existing = dao.getConversationById(id)
        if (existing != null) {
            dao.updateConversation(existing.copy(title = newTitle, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun deleteConversation(id: String) {
        dao.deleteMessagesForConversation(id)
        dao.deleteConversation(id)
    }

    suspend fun clearConversationMessages(conversationId: String) {
        dao.deleteMessagesForConversation(conversationId)
    }

    suspend fun clearAllConversations() {
        dao.clearAllMessages()
        dao.clearAllConversations()
    }

    // Messages
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>> =
        dao.getMessagesForConversation(conversationId)

    suspend fun insertMessage(message: MessageEntity) {
        dao.insertMessage(message)
        // Also update conversation timestamp
        val conv = dao.getConversationById(message.conversationId)
        if (conv != null) {
            val title = if (conv.title == "New Conversation" && message.role == "user") {
                val clean = message.content.take(32).trim()
                if (clean.isNotEmpty()) clean else conv.title
            } else {
                conv.title
            }
            dao.updateConversation(conv.copy(title = title, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun updateMessage(message: MessageEntity) {
        dao.updateMessage(message)
    }

    suspend fun deleteMessage(id: String) {
        dao.deleteMessageById(id)
    }

    // Memory
    fun getAllMemory(): Flow<List<StudentMemoryEntity>> = dao.getAllMemory()

    suspend fun saveMemory(key: String, value: String) {
        dao.insertMemory(StudentMemoryEntity(key = key, value = value, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteMemory(key: String) {
        dao.deleteMemory(key)
    }

    suspend fun clearMemory() {
        dao.clearAllMemory()
    }

    // AI Generation
    fun stopGeneration() {
        geminiService.stopActiveGeneration()
    }

    suspend fun generateStream(
        conversationId: String,
        latestPrompt: String,
        imageBitmap: Bitmap? = null,
        onChunkReceived: (String) -> Unit
    ): Result<String> {
        val historyEntities = dao.getMessagesListForConversation(conversationId)
        val historyTurns = historyEntities.map { it.role to it.content }

        val isMemoryEnabled = isMemoryEnabled()
        val studentContext = if (isMemoryEnabled) {
            val memories = dao.getAllMemoryList()
            if (memories.isNotEmpty()) {
                memories.joinToString("\n") { "- ${it.key}: ${it.value}" }
            } else null
        } else null

        val responseStyle = getResponseStyle()

        return geminiService.streamGenerate(
            conversationHistory = historyTurns,
            latestPrompt = latestPrompt,
            imageBitmap = imageBitmap,
            studentContext = studentContext,
            responseStyle = responseStyle,
            onChunkReceived = onChunkReceived
        )
    }

    // Preferences
    fun getThemeMode(): String = prefs.getString("theme_mode", "system") ?: "system"
    fun setThemeMode(mode: String) = prefs.edit().putString("theme_mode", mode).apply()

    fun isMemoryEnabled(): Boolean = prefs.getBoolean("memory_enabled", true)
    fun setMemoryEnabled(enabled: Boolean) = prefs.edit().putBoolean("memory_enabled", enabled).apply()

    fun getResponseStyle(): String = prefs.getString("response_style", "Clear & Detailed Tutor") ?: "Clear & Detailed Tutor"
    fun setResponseStyle(style: String) = prefs.edit().putString("response_style", style).apply()

    fun getSpeechRate(): Float = prefs.getFloat("speech_rate", 1.0f)
    fun setSpeechRate(rate: Float) = prefs.edit().putFloat("speech_rate", rate).apply()
}
