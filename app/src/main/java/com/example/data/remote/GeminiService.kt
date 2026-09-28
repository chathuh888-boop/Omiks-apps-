package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val activeCall = AtomicReference<okhttp3.Call?>(null)

    companion object {
        const val MODEL = "gemini-3.5-flash"
        const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

        val SYSTEM_INSTRUCTION = """
            You are LearnMate AI, a general-purpose AI assistant and personal tutor designed for students.

            You can answer questions about almost any legitimate topic. You are not limited to school subjects.

            Answer naturally and accurately like a modern AI assistant.

            Your special strength is explaining things clearly so students can understand them.

            For simple factual questions, answer directly.

            For complex concepts, explain them clearly.

            For problems requiring reasoning, show the important reasoning and steps.

            When appropriate, help students understand how to reach an answer rather than encouraging blind copying.

            If the student asks for a hint, give a hint.

            If the student asks for the full answer, provide it.

            If the student asks for a short answer, be concise.

            If the student asks for a detailed explanation, provide detail.

            If the student does not understand, explain the concept differently.

            Never intentionally make simple questions complicated.

            Never intentionally hide an answer.

            Do not force students through unnecessary questions.

            Adapt explanations to the student's apparent knowledge level.

            Support English, Sinhala, Tamil, and mixed-language conversations. Respond in the student's language naturally.

            Be patient, natural, respectful, and helpful.

            Never invent facts.

            Clearly communicate uncertainty when necessary.

            Carefully check calculations and reasoning.

            Your goal is:
            Answer the student's question while helping them understand the answer.
        """.trimIndent()
    }

    fun stopActiveGeneration() {
        try {
            activeCall.getAndSet(null)?.cancel()
        } catch (_: Exception) {}
    }

    suspend fun streamGenerate(
        conversationHistory: List<Pair<String, String>>, // role to text
        latestPrompt: String,
        imageBitmap: Bitmap? = null,
        studentContext: String? = null,
        responseStyle: String? = null,
        onChunkReceived: (String) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Please configure your Gemini API Key in the AI Studio Secrets panel.")
            )
        }

        try {
            val rootJson = JSONObject()

            // System instruction
            var fullSystemInstruction = SYSTEM_INSTRUCTION
            if (!studentContext.isNullOrBlank()) {
                fullSystemInstruction += "\n\nStudent Memory / Profile:\n$studentContext"
            }
            if (!responseStyle.isNullOrBlank()) {
                fullSystemInstruction += "\n\nResponse Preference: $responseStyle"
            }

            val systemInstructionObj = JSONObject()
            val sysParts = JSONArray().put(JSONObject().put("text", fullSystemInstruction))
            systemInstructionObj.put("parts", sysParts)
            rootJson.put("systemInstruction", systemInstructionObj)

            // Contents array
            val contentsArray = JSONArray()

            // Add previous conversation turns (limit to last 16 turns for optimal speed and context)
            val turnsToInclude = conversationHistory.takeLast(16)
            for (turn in turnsToInclude) {
                val (role, text) = turn
                val mappedRole = if (role == "user") "user" else "model"
                val contentObj = JSONObject()
                contentObj.put("role", mappedRole)
                val parts = JSONArray().put(JSONObject().put("text", text))
                contentObj.put("parts", parts)
                contentsArray.put(contentObj)
            }

            // Current prompt turn
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()

            if (imageBitmap != null) {
                val base64Image = bitmapToBase64(imageBitmap)
                val inlineData = JSONObject()
                inlineData.put("mimeType", "image/jpeg")
                inlineData.put("data", base64Image)
                currentParts.put(JSONObject().put("inlineData", inlineData))
            }

            currentParts.put(JSONObject().put("text", latestPrompt))
            currentTurn.put("parts", currentParts)
            contentsArray.put(currentTurn)

            rootJson.put("contents", contentsArray)

            // Generation config
            val generationConfig = JSONObject()
            generationConfig.put("temperature", 0.7)
            generationConfig.put("topP", 0.95)
            rootJson.put("generationConfig", generationConfig)

            val endpoint = "$BASE_URL/$MODEL:streamGenerateContent?alt=sse&key=$apiKey"
            val requestBody = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val call = client.newCall(request)
            activeCall.set(call)

            val fullAccumulatedText = StringBuilder()

            val response = call.execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string().orEmpty()
                val errorMsg = try {
                    val errJson = JSONObject(errorBody)
                    errJson.optJSONObject("error")?.optString("message") ?: "Error ${response.code}: ${response.message}"
                } catch (_: Exception) {
                    "API Error (${response.code}): ${response.message}"
                }
                return@withContext Result.failure(IOException(errorMsg))
            }

            val body = response.body ?: return@withContext Result.failure(IOException("Empty response from server"))
            body.byteStream().bufferedReader().use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val rawLine = line?.trim().orEmpty()
                    if (!rawLine.startsWith("data:")) continue
                    val jsonPayload = rawLine.removePrefix("data:").trim()
                    if (jsonPayload.isEmpty() || jsonPayload == "[DONE]") continue

                    try {
                        val chunkObj = JSONObject(jsonPayload)
                        val candidates = chunkObj.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val candidate = candidates.getJSONObject(0)
                            val content = candidate.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                for (i in 0 until parts.length()) {
                                    val part = parts.getJSONObject(i)
                                    val text = part.optString("text")
                                    if (text.isNotEmpty()) {
                                        fullAccumulatedText.append(text)
                                        withContext(Dispatchers.Main) {
                                            onChunkReceived(text)
                                        }
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {
                        // Ignore malformed chunk
                    }
                }
            }

            activeCall.set(null)
            Result.success(fullAccumulatedText.toString())
        } catch (e: CancellationException) {
            activeCall.set(null)
            Result.failure(e)
        } catch (e: Exception) {
            activeCall.set(null)
            val message = if (e is IOException && e.message?.contains("Canceled") == true) {
                "Generation stopped by user"
            } else {
                e.localizedMessage ?: "Network connection error"
            }
            Result.failure(IOException(message, e))
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        // Resize if too large to save bandwidth and stay well within Gemini limits
        val maxDim = 1280
        val scaled = if (bitmap.width > maxDim || bitmap.height > maxDim) {
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val newWidth: Int
            val newHeight: Int
            if (ratio > 1) {
                newWidth = maxDim
                newHeight = (maxDim / ratio).toInt()
            } else {
                newHeight = maxDim
                newWidth = (maxDim * ratio).toInt()
            }
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }

        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
