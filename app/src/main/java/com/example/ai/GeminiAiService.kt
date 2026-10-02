package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiAiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val MODEL_NAME = "gemini-3.1-pro-preview"

    suspend fun solveAndExplain(problemQuery: String, domainContext: String = "Mathematics"): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isNullOrBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please add your GEMINI_API_KEY in the AI Studio Secrets panel.")
            )
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"

            // Construct JSON request body with ThinkingLevel.HIGH
            val root = JSONObject()

            // System instruction
            val sysInstruction = JSONObject()
            val sysParts = JSONArray()
            val sysPart = JSONObject()
            sysPart.put(
                "text",
                "You are Calcora AI — an expert, rigorous mathematical, scientific, engineering, and financial calculation solver. " +
                "Provide a deeply structured, step-by-step solution. Break down: 1. Problem Statement & Given Parameters, 2. Relevant Theorems/Formulas, 3. Step-by-Step Algebraic/Numerical Working, 4. Verified Final Answer in bold, 5. Key Takeaways or Graphical Insights. Keep it clean, elegant, and readable."
            )
            sysParts.put(sysPart)
            sysInstruction.put("parts", sysParts)
            root.put("systemInstruction", sysInstruction)

            // Contents
            val contents = JSONArray()
            val contentObj = JSONObject()
            val parts = JSONArray()
            val textPart = JSONObject()
            textPart.put("text", "Context: $domainContext. Solve and explain this thoroughly step-by-step:\n\n$problemQuery")
            parts.put(textPart)
            contentObj.put("parts", parts)
            contents.put(contentObj)
            root.put("contents", contents)

            // Generation config with ThinkingLevel.HIGH and no maxOutputTokens
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.2)
            val thinkingConfig = JSONObject()
            thinkingConfig.put("thinkingLevel", "high")
            genConfig.put("thinkingConfig", thinkingConfig)
            root.put("generationConfig", genConfig)

            val body = root.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val respBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(respBody)
                    errJson.optJSONObject("error")?.optString("message") ?: "API Error (${response.code})"
                } catch (_: Exception) {
                    "API Error (${response.code}): $respBody"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val respJson = JSONObject(respBody)
            val candidates = respJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("No solution generated."))
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val candidateParts = content?.optJSONArray("parts")

            val fullText = StringBuilder()
            if (candidateParts != null) {
                for (i in 0 until candidateParts.length()) {
                    val p = candidateParts.getJSONObject(i)
                    val txt = p.optString("text", "")
                    if (txt.isNotEmpty()) {
                        fullText.append(txt)
                    }
                }
            }

            if (fullText.isEmpty()) {
                Result.failure(Exception("Empty answer returned from model."))
            } else {
                Result.success(fullText.toString())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
