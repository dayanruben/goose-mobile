package xyz.block.gosling

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AgentException(message: String) : Exception(message)

object Agent {
    data class Message(
        val role: String,
        val content: Any?,
        val tool_call_id: String? = null,
        val name: String? = null,
        val tool_calls: List<Map<String, Any>>? = null
    )

    suspend fun processCommand(userInput: String, context: Context, onStatusUpdate: (String) -> Unit): String {
        val availableIntents = IntentScanner.getAvailableIntents(context)
        val installedApps = availableIntents.joinToString("\n") { it.formatForLLM() }

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels


        val systemMessage = """
            You are an assistant managing the users android phone. The user does not have access to the phone. 
            You will autonomously complete complex tasks on the phone and report back to the user when 
            done. Try to avoid asking extra questions. You accomplish this by starting apps on the phone 
            and interacting with them. When you call a tool, tell the user about it.
            After each tool call you will see the state of the phone by way of a screenshot and a ui hierarchy 
            produced using 'adb shell uiautomator dump'. One or both might be simplified or omitted to save space. 
            Use this to verify your work
            The phone has a screen resolution of ${width}x${height} pixels
            The phone has the following apps installed:
    
            $installedApps
            
            Before getting started, explicitly state the steps you want to take and which app(s) you want 
            use to accomplish that task. For example, open the contacts app to find out Joe's phone number. 
            Then after each step verify that the step was completed successfully by looking at the screen and 
            the ui hierarchy dump. If the step was not completed successfully, try to recover. When you start 
            an app, make sure the app is in the state you expect it to be in. If it is not, try to recover.
            
            After each tool call and before the next step, write down what you see on the screen that helped 
            you resolve this step. If you can't consider retrying.
        """.trimIndent()

        val messages = mutableListOf(
            Message(role = "system", content = systemMessage),
            Message(role = "user", content = userInput)
        )

        onStatusUpdate("Thinking...")

        withContext(Dispatchers.IO) {
            while (true) {
                var response: JSONObject?
                try {
                    response = callLlm(messages, 0.1, context)
                } catch (e: AgentException) {
                    val errorMsg = e.message ?: "Error"
                    onStatusUpdate(errorMsg)
                    return@withContext errorMsg
                }

                val assistantMessage = response.getJSONArray("choices").getJSONObject(0).getJSONObject("message")
                val assistantReply = assistantMessage.optString("content", "Ok")

                onStatusUpdate(assistantReply)

                val toolResults = parseAndExecuteTool(response, context)
                if (toolResults.isEmpty()) break

                messages.add(
                    Message(
                        role = "assistant",
                        content = assistantMessage.optString("content"),
                        tool_calls = assistantMessage.optJSONArray("tool_calls")?.let {
                            List(it.length()) { i -> jsonObjectToMap(it.getJSONObject(i)) }
                        }
                    )
                )

                for (result in toolResults) {
                    messages.add(
                        Message(
                            role = "tool",
                            tool_call_id = result["tool_call_id"].toString(),
                            content = result["output"]
                        )
                    )
                }
            }
        }
        return "Done"
    }

    private fun parseAndExecuteTool(response: JSONObject, context: Context): List<Map<String, String>> {
        val assistantMessage = response.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")

        val toolCalls = assistantMessage.optJSONArray("tool_calls") ?: return emptyList()
        val toolResults = mutableListOf<Map<String, String>>()

        for (i in 0 until toolCalls.length()) {
            val toolCall = toolCalls.getJSONObject(i)
            val toolCallId = toolCall.getString("id")

            val result = callTool(toolCall, context, GoslingAccessibilityService.getInstance())
            toolResults.add(mapOf("tool_call_id" to toolCallId, "output" to result))
        }

        return toolResults
    }

    private fun callLlm(messages: List<Message>, temperature: Double, context: Context): JSONObject {
        val url = URL("https://api.openai.com/v1/chat/completions")
        val connection = url.openConnection() as HttpURLConnection

        val sharedPrefs = context.getSharedPreferences("gosling_settings", Context.MODE_PRIVATE)
        val model = sharedPrefs.getString("selected_model", "gpt-40") ?: "gpt-40"
        val apiKey = sharedPrefs.getString("api_key", "") ?: ""

        return try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val messagesJsonArray = JSONArray()
            messages.forEach { message ->
                messagesJsonArray.put(messageToJson(message))
            }

            val requestBody = JSONObject()
            requestBody.put("model", model)
            requestBody.put("messages", messagesJsonArray)
            requestBody.put("temperature", temperature)
            requestBody.put("tools", getToolDefinitions().toJSONArray())

            connection.outputStream.write(requestBody.toString().toByteArray())

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                throw AgentException("Error calling LLM: $errorResponse")
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            return JSONObject(response)
        } catch (e: Exception) {
            throw AgentException("Error calling LLM: ${e.message}")
        } finally {
            connection.disconnect()
        }
    }

    private fun messageToJson(message: Message): JSONObject {
        val json = JSONObject()
        json.put("role", message.role)
        if (message.content != null) {
            json.put("content", message.content)
        }
        message.tool_call_id?.let { json.put("tool_call_id", it) }
        message.name?.let { json.put("name", it) }
        message.tool_calls?.let {
            val toolCallsJson = JSONArray()
            it.forEach { toolCall ->
                toolCallsJson.put((toolCall as Map<String, Any>).toJSONObject())
            }
            json.put("tool_calls", toolCallsJson)
        }
        return json
    }

    private fun Map<String, Any>.toJSONObject(): JSONObject {
        val json = JSONObject()
        this.forEach { (key, value) ->
            when (value) {
                is Map<*, *> -> json.put(key, (value as Map<String, Any>).toJSONObject())
                is List<*> -> json.put(key, (value as List<Any>).toJSONArray())
                else -> json.put(key, value)
            }
        }
        return json
    }

    private fun List<Any>.toJSONArray(): JSONArray {
        val jsonArray = JSONArray()
        this.forEach { item ->
            when (item) {
                is Map<*, *> -> jsonArray.put((item as Map<String, Any>).toJSONObject())
                is List<*> -> jsonArray.put((item as List<Any>).toJSONArray())
                else -> jsonArray.put(item)
            }
        }
        return jsonArray
    }

    private fun jsonObjectToMap(jsonObject: JSONObject): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = when (val value = jsonObject.get(key)) {
                is JSONObject -> jsonObjectToMap(value)
                is JSONArray -> jsonArrayToList(value)
                else -> value
            }
        }
        return map
    }

    private fun jsonArrayToList(jsonArray: JSONArray): List<Any> {
        return List(jsonArray.length()) { i ->
            when (val value = jsonArray.get(i)) {
                is JSONObject -> jsonObjectToMap(value)
                is JSONArray -> jsonArrayToList(value)
                else -> value
            }
        }
    }
}
