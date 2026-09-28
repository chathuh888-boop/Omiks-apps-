package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ConversationEntity
import com.example.data.local.LearnMateDatabase
import com.example.data.local.MessageEntity
import com.example.data.local.StudentMemoryEntity
import com.example.data.repository.LearnMateRepository
import com.example.util.TtsManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class LearnMateViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LearnMateDatabase.getDatabase(application)
    private val repository = LearnMateRepository(application, db.dao())
    val ttsManager = TtsManager(application)

    // Current Active Conversation
    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    // All Conversations List
    val conversations: StateFlow<List<ConversationEntity>> = repository.getAllConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Messages for current conversation
    val messages: StateFlow<List<MessageEntity>> = _currentConversationId
        .flatMapLatest { convId ->
            if (convId != null) repository.getMessagesForConversation(convId)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Memory items
    val studentMemories: StateFlow<List<StudentMemoryEntity>> = repository.getAllMemory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Input state
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    // Attached Image
    private val _attachedBitmap = MutableStateFlow<Bitmap?>(null)
    val attachedBitmap: StateFlow<Bitmap?> = _attachedBitmap.asStateFlow()

    private val _attachedImageUri = MutableStateFlow<String?>(null)
    val attachedImageUri: StateFlow<String?> = _attachedImageUri.asStateFlow()

    // Generation state
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _streamingContent = MutableStateFlow("")
    val streamingContent: StateFlow<String> = _streamingContent.asStateFlow()

    // Error state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Settings
    private val _themeMode = MutableStateFlow(repository.getThemeMode())
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _isMemoryEnabled = MutableStateFlow(repository.isMemoryEnabled())
    val isMemoryEnabled: StateFlow<Boolean> = _isMemoryEnabled.asStateFlow()

    private val _responseStyle = MutableStateFlow(repository.getResponseStyle())
    val responseStyle: StateFlow<String> = _responseStyle.asStateFlow()

    private val _speechRate = MutableStateFlow(repository.getSpeechRate())
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    // Explain Differently Target Message
    private val _explainDifferentlyMessage = MutableStateFlow<MessageEntity?>(null)
    val explainDifferentlyMessage: StateFlow<MessageEntity?> = _explainDifferentlyMessage.asStateFlow()

    private var generationJob: Job? = null

    init {
        // Initialize with latest conversation or ready state
        viewModelScope.launch {
            conversations.collect { list ->
                if (_currentConversationId.value == null && list.isNotEmpty()) {
                    _currentConversationId.value = list.first().id
                }
            }
        }
    }

    fun selectConversation(id: String) {
        ttsManager.stop()
        stopGeneration()
        _currentConversationId.value = id
        _inputText.value = ""
        clearAttachedImage()
    }

    fun startNewConversation() {
        ttsManager.stop()
        stopGeneration()
        clearAttachedImage()
        _inputText.value = ""
        viewModelScope.launch {
            val newConv = repository.createNewConversation("New Conversation")
            _currentConversationId.value = newConv.id
        }
    }

    fun deleteConversation(id: String) {
        ttsManager.stop()
        stopGeneration()
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_currentConversationId.value == id) {
                val remaining = conversations.value.filter { it.id != id }
                _currentConversationId.value = remaining.firstOrNull()?.id
            }
        }
    }

    fun clearAllConversations() {
        ttsManager.stop()
        stopGeneration()
        clearAttachedImage()
        _inputText.value = ""
        viewModelScope.launch {
            repository.clearAllConversations()
            _currentConversationId.value = null
        }
    }

    fun clearCurrentConversationMessages() {
        val currentId = _currentConversationId.value ?: return
        ttsManager.stop()
        stopGeneration()
        clearAttachedImage()
        _inputText.value = ""
        viewModelScope.launch {
            repository.clearConversationMessages(currentId)
        }
    }

    fun deleteMessage(messageId: String) {
        if (ttsManager.currentPlayingMessageId.value == messageId) {
            ttsManager.stop()
        }
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun renameConversation(id: String, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            repository.updateConversationTitle(id, newTitle.trim())
        }
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun setAttachedImage(bitmap: Bitmap, uriString: String? = null) {
        _attachedBitmap.value = bitmap
        _attachedImageUri.value = uriString
    }

    fun clearAttachedImage() {
        _attachedBitmap.value = null
        _attachedImageUri.value = null
    }

    fun setExplainDifferentlyTarget(message: MessageEntity?) {
        _explainDifferentlyMessage.value = message
    }

    fun triggerExplainDifferently(targetMessage: MessageEntity, stylePrompt: String, styleLabel: String) {
        _explainDifferentlyMessage.value = null
        val prompt = "Please explain this differently using a $styleLabel approach:\n\n${targetMessage.content}"
        sendMessage(promptOverride = prompt, explanationStyle = styleLabel)
    }

    fun sendMessage(promptOverride: String? = null, explanationStyle: String? = null) {
        val textToSend = (promptOverride ?: _inputText.value).trim()
        val currentBitmap = _attachedBitmap.value

        if (textToSend.isEmpty() && currentBitmap == null) {
            return
        }

        viewModelScope.launch {
            var activeConvId = _currentConversationId.value
            if (activeConvId == null) {
                val newConv = repository.createNewConversation(
                    if (textToSend.isNotBlank()) textToSend.take(30) else "Photo Question"
                )
                activeConvId = newConv.id
                _currentConversationId.value = activeConvId
            }

            // Save image locally to display inside chat bubble
            var savedImageLocalUri: String? = null
            if (currentBitmap != null) {
                savedImageLocalUri = saveBitmapToLocalCache(currentBitmap)
            }

            // Insert User Message
            val userMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = activeConvId,
                role = "user",
                content = textToSend,
                imageUri = savedImageLocalUri,
                timestamp = System.currentTimeMillis(),
                explanationStyle = explanationStyle
            )
            repository.insertMessage(userMsg)

            // Clear input & image preview immediately
            if (promptOverride == null) {
                _inputText.value = ""
                clearAttachedImage()
            }

            // Detect memory cues automatically (e.g. "I am in Grade 10", "I struggle with physics")
            detectAndSaveStudentPreferences(textToSend)

            // Start AI Generation
            _isGenerating.value = true
            _streamingContent.value = ""
            _errorMessage.value = null

            generationJob = launch {
                val fullResponse = StringBuilder()

                val result = repository.generateStream(
                    conversationId = activeConvId,
                    latestPrompt = textToSend,
                    imageBitmap = currentBitmap
                ) { chunk ->
                    fullResponse.append(chunk)
                    _streamingContent.value = fullResponse.toString()
                }

                _isGenerating.value = false

                result.onSuccess { finalText ->
                    val cleanFinal = if (finalText.isNotBlank()) finalText else fullResponse.toString()
                    if (cleanFinal.isNotBlank()) {
                        val assistantMsg = MessageEntity(
                            id = UUID.randomUUID().toString(),
                            conversationId = activeConvId,
                            role = "assistant",
                            content = cleanFinal,
                            timestamp = System.currentTimeMillis(),
                            explanationStyle = explanationStyle
                        )
                        repository.insertMessage(assistantMsg)
                    }
                    _streamingContent.value = ""
                }.onFailure { err ->
                    _streamingContent.value = ""
                    val msg = err.localizedMessage ?: "Failed to get response"
                    if (!msg.contains("stopped by user", ignoreCase = true) &&
                        !msg.contains("Canceled", ignoreCase = true)
                    ) {
                        _errorMessage.value = "Couldn't connect to LearnMate AI. Check your internet connection and try again."
                    }
                }
            }
        }
    }

    fun stopGeneration() {
        repository.stopGeneration()
        generationJob?.cancel()
        generationJob = null
        val streamed = _streamingContent.value.trim()
        val convId = _currentConversationId.value
        if (streamed.isNotBlank() && convId != null) {
            viewModelScope.launch {
                val assistantMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    conversationId = convId,
                    role = "assistant",
                    content = "$streamed\n\n*(Generation stopped)*",
                    timestamp = System.currentTimeMillis()
                )
                repository.insertMessage(assistantMsg)
                _streamingContent.value = ""
            }
        }
        _isGenerating.value = false
    }

    fun speakMessage(message: MessageEntity) {
        ttsManager.speak(message.id, message.content, _speechRate.value)
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        repository.setThemeMode(mode)
    }

    fun setMemoryEnabled(enabled: Boolean) {
        _isMemoryEnabled.value = enabled
        repository.setMemoryEnabled(enabled)
    }

    fun setResponseStyle(style: String) {
        _responseStyle.value = style
        repository.setResponseStyle(style)
    }

    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate
        repository.setSpeechRate(rate)
    }

    fun addMemoryItem(key: String, value: String) {
        viewModelScope.launch {
            repository.saveMemory(key, value)
        }
    }

    fun deleteMemoryItem(key: String) {
        viewModelScope.launch {
            repository.deleteMemory(key)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearMemory()
        }
    }

    private fun detectAndSaveStudentPreferences(text: String) {
        if (!_isMemoryEnabled.value) return
        val lower = text.lowercase()

        // Grade detection: "I am in grade 9", "grade 11 student"
        val gradeMatch = Regex("(grade|class|year)\\s+(\\d{1,2}|kindergarten|freshman|sophomore|junior|senior)").find(lower)
        if (gradeMatch != null) {
            val grade = gradeMatch.value
            viewModelScope.launch { repository.saveMemory("Grade / Level", grade.replaceFirstChar { it.uppercase() }) }
        }

        // Struggle topic: "I struggle with algebra", "hard time understanding calculus"
        val struggleMatch = Regex("(struggle with|difficult for me to understand|trouble with)\\s+([a-zA-Z\\s]{3,20})").find(lower)
        if (struggleMatch != null) {
            val topic = struggleMatch.groupValues[2].trim()
            viewModelScope.launch { repository.saveMemory("Topic Needing Clarity", topic) }
        }
    }

    private fun saveBitmapToLocalCache(bitmap: Bitmap): String {
        val app = getApplication<Application>()
        val imagesDir = File(app.cacheDir, "images")
        if (!imagesDir.exists()) imagesDir.mkdirs()
        val file = File(imagesDir, "img_${UUID.randomUUID()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file.absolutePath
    }

    fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val context = getApplication<Application>()
            val input = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(input)
        } catch (_: Exception) {
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
