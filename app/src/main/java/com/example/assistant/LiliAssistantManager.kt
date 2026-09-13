package com.example.assistant

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.provider.ContactsContract
import android.provider.Settings
import android.speech.tts.TextToSpeech
import androidx.core.content.ContextCompat
import com.example.BuildConfig
import com.example.data.model.AppItem
import com.example.data.model.ClockType
import com.example.data.model.FontType
import com.example.data.model.ThemeType
import com.example.data.model.WeatherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed class LiliAction {
    data class LaunchApp(val app: AppItem) : LiliAction()
    data class MakeCall(val phoneNumber: String, val contactName: String) : LiliAction()
    data class SendMessage(
        val recipient: String,
        val phoneNumber: String?,
        val message: String,
        val isWhatsApp: Boolean
    ) : LiliAction()
    data class ChangeTheme(val theme: ThemeType) : LiliAction()
    data class ChangeClock(val clock: ClockType) : LiliAction()
    data class ChangeFont(val font: FontType) : LiliAction()
    data class ToggleFlashlight(val turnOn: Boolean) : LiliAction()
    data class OpenSettings(val action: String) : LiliAction()
    data class SearchWeb(val query: String) : LiliAction()
    data class HideApp(val app: AppItem) : LiliAction()
    object ToggleEditMode : LiliAction()
    object OpenAppDrawer : LiliAction()
    object None : LiliAction()
}

data class LiliResponse(
    val message: String,
    val action: LiliAction = LiliAction.None
)

class LiliAssistantManager(private val context: Context) {

    private var isTorchOn = false
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    init {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.forLanguageTag("pt-BR"))
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.setLanguage(Locale.getDefault())
                    }
                    isTtsReady = true
                }
            }
        } catch (_: Exception) {}
    }

    fun speak(text: String) {
        if (isTtsReady) {
            // Strip emojis for cleaner speech synthesis
            val cleanText = text.replace(Regex("[\\p{So}\\p{Cn}]"), "").trim()
            if (cleanText.isNotBlank()) {
                tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "LiliSpeech")
            }
        }
    }

    fun stopSpeaking() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isTtsReady = false
        } catch (_: Exception) {}
    }

    fun isFlashlightOn(): Boolean = isTorchOn

    fun toggleFlashlight(forceState: Boolean? = null): Boolean {
        val targetState = forceState ?: !isTorchOn
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            if (cameraManager != null && cameraId != null) {
                cameraManager.setTorchMode(cameraId, targetState)
                isTorchOn = targetState
                true
            } else {
                false
            }
        } catch (_: CameraAccessException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    suspend fun processCommand(
        userPrompt: String,
        allApps: List<AppItem>,
        currentWeather: WeatherInfo
    ): LiliResponse = withContext(Dispatchers.IO) {
        val trimmed = userPrompt.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        // Wake word detection: If user just said "Lili" or "Ei Lili", answer immediately!
        if (lower == "lili" || lower == "ei lili" || lower == "hey lili" || lower == "olá lili" || lower == "ola lili" || lower == "ok lili") {
            return@withContext LiliResponse("Olá! Eu sou a Lili, estou te ouvindo. Como posso te ajudar agora? ✨")
        }

        // Clean wake word "lili", "ei lili", "olá lili", "hey lili"
        var cleanCommand = lower
        val wakeWords = listOf("ei lili", "hey lili", "olá lili", "ola lili", "ok lili", "lili, ", "lili ")
        for (w in wakeWords) {
            if (cleanCommand.startsWith(w)) {
                cleanCommand = cleanCommand.removePrefix(w).trim()
                break
            }
        }
        if (cleanCommand.startsWith("lili")) {
            cleanCommand = cleanCommand.removePrefix("lili").trim()
        }
        if (cleanCommand.isBlank()) {
            return@withContext LiliResponse("Olá! Estou pronta para te ajudar. O que você gostaria de fazer? ✨")
        }

        // 1. FAZER LIGAÇÃO / PHONE CALLS
        if (cleanCommand.startsWith("ligar para ") || cleanCommand.startsWith("ligar pro ") || cleanCommand.startsWith("ligar pra ") ||
            cleanCommand.startsWith("ligue para ") || cleanCommand.startsWith("ligue pro ") || cleanCommand.startsWith("ligue pra ") ||
            cleanCommand.startsWith("fazer ligação para ") || cleanCommand.startsWith("fazer ligacao para ") ||
            cleanCommand.startsWith("fazer chamada para ") || cleanCommand.startsWith("telefonar para ") ||
            cleanCommand.startsWith("chamar ") || cleanCommand.startsWith("discar para ") || cleanCommand.startsWith("discar ")
        ) {
            val target = cleanCommand
                .removePrefix("fazer ligação para ")
                .removePrefix("fazer ligacao para ")
                .removePrefix("fazer chamada para ")
                .removePrefix("ligar para ")
                .removePrefix("ligar pro ")
                .removePrefix("ligar pra ")
                .removePrefix("ligue para ")
                .removePrefix("ligue pro ")
                .removePrefix("ligue pra ")
                .removePrefix("telefonar para ")
                .removePrefix("chamar ")
                .removePrefix("discar para ")
                .removePrefix("discar ")
                .trim()

            if (target.isNotBlank()) {
                val isNumeric = target.replace(Regex("[^0-9+]"), "").length >= 3
                if (isNumeric) {
                    val cleanedPhone = target.replace(Regex("[^0-9+]"), "")
                    return@withContext LiliResponse(
                        "📞 Ligando para $cleanedPhone...",
                        LiliAction.MakeCall(phoneNumber = cleanedPhone, contactName = target)
                    )
                } else {
                    val contactMatch = findContactPhoneNumber(target)
                    if (contactMatch != null) {
                        return@withContext LiliResponse(
                            "📞 Ligando para ${contactMatch.first} (${contactMatch.second})...",
                            LiliAction.MakeCall(phoneNumber = contactMatch.second, contactName = contactMatch.first)
                        )
                    } else {
                        return@withContext LiliResponse(
                            "📞 Abrindo o discador para ligar para '$target'...",
                            LiliAction.MakeCall(phoneNumber = target, contactName = target)
                        )
                    }
                }
            }
        }

        // 2. MANDAR MENSAGENS / SMS & WHATSAPP
        if (cleanCommand.startsWith("mandar mensagem") || cleanCommand.startsWith("enviar mensagem") ||
            cleanCommand.startsWith("mandar sms") || cleanCommand.startsWith("enviar sms") ||
            cleanCommand.startsWith("mandar zap") || cleanCommand.startsWith("enviar zap") ||
            cleanCommand.startsWith("mandar whatsapp") || cleanCommand.startsWith("enviar whatsapp")
        ) {
            val isWhatsApp = cleanCommand.contains("zap") || cleanCommand.contains("whatsapp") || cleanCommand.contains("whats")
            
            // Extract recipient and message text
            var rawText = cleanCommand
                .removePrefix("mandar mensagem no whatsapp para ")
                .removePrefix("enviar mensagem no whatsapp para ")
                .removePrefix("mandar mensagem no zap para ")
                .removePrefix("enviar mensagem no zap para ")
                .removePrefix("mandar mensagem para ")
                .removePrefix("enviar mensagem para ")
                .removePrefix("mandar mensagem pro ")
                .removePrefix("enviar mensagem pro ")
                .removePrefix("mandar mensagem pra ")
                .removePrefix("enviar mensagem pra ")
                .removePrefix("mandar sms para ")
                .removePrefix("enviar sms para ")
                .removePrefix("mandar zap para ")
                .removePrefix("enviar zap para ")
                .removePrefix("mandar whatsapp para ")
                .removePrefix("enviar whatsapp para ")
                .trim()

            var recipient = rawText
            var messageContent = ""

            val separators = listOf(" dizendo ", " falando ", " com a mensagem ", " com o texto ", " que ", ": ")
            for (sep in separators) {
                if (rawText.contains(sep)) {
                    val parts = rawText.split(sep, limit = 2)
                    recipient = parts[0].trim()
                    messageContent = parts[1].trim()
                    break
                }
            }

            if (recipient.isNotBlank()) {
                val contactMatch = findContactPhoneNumber(recipient)
                val phone = contactMatch?.second ?: if (recipient.replace(Regex("[^0-9+]"), "").length >= 3) recipient else null
                val displayName = contactMatch?.first ?: recipient

                val msgText = if (messageContent.isNotBlank()) messageContent else "Olá!"
                val channel = if (isWhatsApp) "WhatsApp" else "SMS"

                return@withContext LiliResponse(
                    "💬 Enviando $channel para $displayName: \"$msgText\"",
                    LiliAction.SendMessage(
                        recipient = displayName,
                        phoneNumber = phone,
                        message = msgText,
                        isWhatsApp = isWhatsApp
                    )
                )
            }
        }

        // 3. LANTERNA / FLASHLIGHT
        if (cleanCommand.contains("lanterna") || cleanCommand.contains("flash") || cleanCommand.contains("luz")) {
            val shouldTurnOff = cleanCommand.contains("deslig") || cleanCommand.contains("apag")
            val shouldTurnOn = cleanCommand.contains("lig") || cleanCommand.contains("acend") || cleanCommand.contains("ativ") || !shouldTurnOff
            val success = toggleFlashlight(shouldTurnOn)
            val msg = if (success) {
                if (shouldTurnOn) "🔦 Lanterna ativada com sucesso!" else "🔦 Lanterna desligada!"
            } else {
                "Tentei controlar a lanterna, mas o dispositivo não permitiu o acesso à câmera."
            }
            return@withContext LiliResponse(msg, LiliAction.ToggleFlashlight(shouldTurnOn))
        }

        // 4. MUDAR TEMA
        if (cleanCommand.contains("tema") || cleanCommand.contains("mudar tema") || cleanCommand.contains("trocar tema")) {
            when {
                cleanCommand.contains("cyber") || cleanCommand.contains("matrix") || cleanCommand.contains("neon") -> {
                    return@withContext LiliResponse("✨ Aplicando o tema Cyber Neon Matrix!", LiliAction.ChangeTheme(ThemeType.CYBER_MATRIX))
                }
                cleanCommand.contains("cosmo") || cleanCommand.contains("estrela") || cleanCommand.contains("espaço") || cleanCommand.contains("nebula") -> {
                    return@withContext LiliResponse("🌌 Aplicando o tema Cosmos & Estrelas!", LiliAction.ChangeTheme(ThemeType.COSMIC_NEBULA))
                }
                cleanCommand.contains("aurora") || cleanCommand.contains("zen") || cleanCommand.contains("líquid") -> {
                    return@withContext LiliResponse("🌊 Aplicando o tema Aurora Zen Líquida!", LiliAction.ChangeTheme(ThemeType.ZEN_AURORA))
                }
                cleanCommand.contains("retro") || cleanCommand.contains("arcade") || cleanCommand.contains("80") || cleanCommand.contains("synth") -> {
                    return@withContext LiliResponse("🕹️ Aplicando o tema Retro 80s Arcade!", LiliAction.ChangeTheme(ThemeType.RETRO_ARCADE))
                }
                cleanCommand.contains("floresta") || cleanCommand.contains("bio") || cleanCommand.contains("vaga-lume") || cleanCommand.contains("natureza") -> {
                    return@withContext LiliResponse("🌿 Aplicando o tema Floresta Bio-Luminosa!", LiliAction.ChangeTheme(ThemeType.BIO_FOREST))
                }
                else -> {
                    return@withContext LiliResponse("Você pode escolher entre: Cyber Matrix, Cosmos, Aurora Zen, Retro Arcade ou Floresta Bio-Luminosa. Qual prefere?")
                }
            }
        }

        // 5. MUDAR RELÓGIO
        if (cleanCommand.contains("relógio") || cleanCommand.contains("relogio") || cleanCommand.contains("horário")) {
            when {
                cleanCommand.contains("analógico") || cleanCommand.contains("analogico") || cleanCommand.contains("ponteiro") -> {
                    return@withContext LiliResponse("🕒 Alterei o estilo do relógio para Analógico Elegante!", LiliAction.ChangeClock(ClockType.ANALOG_DIAL))
                }
                cleanCommand.contains("digital") || cleanCommand.contains("minimal") -> {
                    return@withContext LiliResponse("⏰ Alterei o relógio para Digital Minimalista!", LiliAction.ChangeClock(ClockType.MINIMAL_DIGITAL))
                }
                cleanCommand.contains("hud") || cleanCommand.contains("cyber") -> {
                    return@withContext LiliResponse("⚡ Alterei o relógio para HUD Cyberpunk!", LiliAction.ChangeClock(ClockType.CYBER_HUD))
                }
                cleanCommand.contains("flip") || cleanCommand.contains("retrô") || cleanCommand.contains("retro") -> {
                    return@withContext LiliResponse("📟 Alterei o relógio para Flip Clock Retrô!", LiliAction.ChangeClock(ClockType.RETRO_FLIP))
                }
                cleanCommand.contains("texto") || cleanCommand.contains("tipograf") -> {
                    return@withContext LiliResponse("📜 Alterei o relógio para Texto Tipográfico!", LiliAction.ChangeClock(ClockType.TYPOGRAPHY))
                }
                cleanCommand.contains("vidro") || cleanCommand.contains("capsula") || cleanCommand.contains("glass") -> {
                    return@withContext LiliResponse("💎 Alterei o relógio para Cápsula Glassmorphic!", LiliAction.ChangeClock(ClockType.GLASS_PILL))
                }
            }
        }

        // 6. ORGANIZAR APPS / MODO EDIÇÃO / GAVETA
        if (cleanCommand.contains("organizar") || cleanCommand.contains("editar apps") || cleanCommand.contains("modo edição") || cleanCommand.contains("reordenar")) {
            return@withContext LiliResponse("🛠️ Ativando o Modo de Organização de Apps!", LiliAction.ToggleEditMode)
        }
        if (cleanCommand.contains("gaveta") || cleanCommand.contains("todos os apps") || cleanCommand.contains("menu de apps") || cleanCommand.contains("lista de apps")) {
            return@withContext LiliResponse("📱 Abrindo a lista completa de aplicativos!", LiliAction.OpenAppDrawer)
        }

        // 7. ABRIR APLICATIVO
        if (cleanCommand.startsWith("abrir ") || cleanCommand.startsWith("iniciar ") || cleanCommand.startsWith("executar ") ||
            cleanCommand.startsWith("abra ") || cleanCommand.startsWith("inicie ")
        ) {
            val appTarget = cleanCommand
                .removePrefix("abrir a ")
                .removePrefix("abrir o ")
                .removePrefix("abrir ")
                .removePrefix("iniciar a ")
                .removePrefix("iniciar o ")
                .removePrefix("iniciar ")
                .removePrefix("executar ")
                .removePrefix("abra a ")
                .removePrefix("abra o ")
                .removePrefix("abra ")
                .removePrefix("inicie ")
                .trim()

            val matchedApp = findBestAppMatch(appTarget, allApps)
            if (matchedApp != null) {
                return@withContext LiliResponse(
                    "🚀 Abrindo ${matchedApp.displayLabel} agora...",
                    LiliAction.LaunchApp(matchedApp)
                )
            } else {
                // Check system settings actions
                val settingAction = matchSettingsAction(appTarget)
                if (settingAction != null) {
                    return@withContext LiliResponse("⚙️ Abrindo configurações de $appTarget...", LiliAction.OpenSettings(settingAction))
                }
                return@withContext LiliResponse("Não encontrei nenhum aplicativo com o nome '$appTarget' instalado no celular.")
            }
        }

        // 7.5 OCULTAR APLICATIVO
        if (cleanCommand.startsWith("esconder ") || cleanCommand.startsWith("ocultar ") || cleanCommand.startsWith("esconda ") || cleanCommand.startsWith("oculte ")) {
            val appTarget = cleanCommand
                .removePrefix("esconder o ")
                .removePrefix("esconder a ")
                .removePrefix("esconder aplicativo ")
                .removePrefix("esconder app ")
                .removePrefix("esconder ")
                .removePrefix("ocultar o ")
                .removePrefix("ocultar a ")
                .removePrefix("ocultar aplicativo ")
                .removePrefix("ocultar app ")
                .removePrefix("ocultar ")
                .removePrefix("esconda o ")
                .removePrefix("esconda a ")
                .removePrefix("esconda ")
                .removePrefix("oculte o ")
                .removePrefix("oculte a ")
                .removePrefix("oculte ")
                .trim()

            val matchedApp = findBestAppMatch(appTarget, allApps)
            if (matchedApp != null) {
                return@withContext LiliResponse(
                    "👁️‍🗨️ O aplicativo ${matchedApp.displayLabel} foi ocultado do launcher. Você pode restaurá-lo nas Configurações > Apps Ocultos.",
                    LiliAction.HideApp(matchedApp)
                )
            } else {
                return@withContext LiliResponse("Não encontrei o aplicativo '$appTarget' para ocultar.")
            }
        }

        // 8. STATUS DO SISTEMA / BATERIA / MEMÓRIA
        if (cleanCommand.contains("bateria") || cleanCommand.contains("carga")) {
            val batteryLevel = getBatteryLevel()
            return@withContext LiliResponse("🔋 Seu celular está com $batteryLevel% de bateria.")
        }
        if (cleanCommand.contains("memória") || cleanCommand.contains("ram") || cleanCommand.contains("armazenamento") || cleanCommand.contains("status")) {
            val (freeRamMb, totalRamMb) = getRamInfo()
            val (freeStorageGb, totalStorageGb) = getStorageInfo()
            val bat = getBatteryLevel()
            return@withContext LiliResponse(
                "📊 Status do Dispositivo:\n• Bateria: $bat%\n• RAM: ${totalRamMb - freeRamMb}MB usados de ${totalRamMb}MB\n• Armazenamento: ${freeStorageGb}GB livres de ${totalStorageGb}GB"
            )
        }

        // 9. CLIMA
        if (cleanCommand.contains("clima") || cleanCommand.contains("tempo") || cleanCommand.contains("temperatura") || cleanCommand.contains("chover")) {
            return@withContext LiliResponse(
                "${currentWeather.iconEmoji} Em ${currentWeather.city} está fazendo ${currentWeather.temperature}°C (${currentWeather.condition}). Mínima de ${currentWeather.lowTemp}°C e máxima de ${currentWeather.highTemp}°C, umidade em ${currentWeather.humidity}%."
            )
        }

        // 10. CONFIGURAÇÕES
        val settingAction = matchSettingsAction(cleanCommand)
        if (settingAction != null) {
            return@withContext LiliResponse("⚙️ Abrindo configurações solicitadas...", LiliAction.OpenSettings(settingAction))
        }

        // 11. PESQUISA
        if (cleanCommand.startsWith("pesquisar ") || cleanCommand.startsWith("buscar ") || cleanCommand.startsWith("pesquise ")) {
            val query = cleanCommand.substringAfter(" ").trim()
            return@withContext LiliResponse("🔍 Pesquisando por '$query'...", LiliAction.SearchWeb(query))
        }

        // 12. IA GEMINI (se chave configurada)
        val geminiKey = BuildConfig.GEMINI_API_KEY
        if (geminiKey.isNotBlank() && !geminiKey.contains("MY_GEMINI_API_KEY")) {
            val aiResponse = callGeminiAssistant(cleanCommand, geminiKey)
            if (aiResponse != null) {
                return@withContext LiliResponse(aiResponse)
            }
        }

        // Fallback amigável da Lili
        return@withContext LiliResponse(generateLocalFallback(cleanCommand, currentWeather))
    }

    private fun findContactPhoneNumber(contactNameQuery: String): Pair<String, String>? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return null
        }
        return try {
            val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )
            val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
            val selectionArgs = arrayOf("%$contactNameQuery%")
            val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val name = if (nameIndex >= 0) it.getString(nameIndex) else contactNameQuery
                    val number = if (numberIndex >= 0) it.getString(numberIndex) else ""
                    if (number.isNotBlank()) Pair(name, number) else null
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun findBestAppMatch(query: String, allApps: List<AppItem>): AppItem? {
        val q = query.lowercase().trim()
        // Exact matches
        allApps.firstOrNull { it.displayLabel.lowercase() == q }?.let { return it }
        allApps.firstOrNull { it.packageName.lowercase().contains(q) }?.let { return it }

        // Startswith
        allApps.firstOrNull { it.displayLabel.lowercase().startsWith(q) }?.let { return it }

        // Contains
        allApps.firstOrNull { it.displayLabel.lowercase().contains(q) }?.let { return it }

        // Common synonyms
        val synonymMap = mapOf(
            "camera" to listOf("câmera", "camera", "foto", "cam"),
            "whatsapp" to listOf("zap", "whats", "mensagens"),
            "chrome" to listOf("navegador", "internet", "google"),
            "telefone" to listOf("chamada", "ligar", "discador", "phone"),
            "contatos" to listOf("agenda", "contato", "contacts"),
            "spotify" to listOf("musica", "música", "som", "spotify"),
            "youtube" to listOf("videos", "vídeos", "yt"),
            "configurações" to listOf("config", "ajustes", "settings")
        )
        for ((target, aliases) in synonymMap) {
            if (aliases.any { q.contains(it) }) {
                allApps.firstOrNull { it.displayLabel.lowercase().contains(target) }?.let { return it }
            }
        }

        return null
    }

    private fun matchSettingsAction(text: String): String? {
        return when {
            text.contains("wifi") || text.contains("wi-fi") || text.contains("internet") -> Settings.ACTION_WIFI_SETTINGS
            text.contains("bluetooth") -> Settings.ACTION_BLUETOOTH_SETTINGS
            text.contains("som") || text.contains("volume") || text.contains("audio") -> Settings.ACTION_SOUND_SETTINGS
            text.contains("tela") || text.contains("brilho") || text.contains("display") -> Settings.ACTION_DISPLAY_SETTINGS
            text.contains("aplicativo") || text.contains("apps") -> Settings.ACTION_APPLICATION_SETTINGS
            text.contains("data") || text.contains("hora") -> Settings.ACTION_DATE_SETTINGS
            text.contains("localização") || text.contains("gps") -> Settings.ACTION_LOCATION_SOURCE_SETTINGS
            text.contains("config") -> Settings.ACTION_SETTINGS
            else -> null
        }
    }

    private fun getBatteryLevel(): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 85
    }

    private fun getRamInfo(): Pair<Long, Long> {
        return try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager?.getMemoryInfo(memInfo)
            val freeMb = memInfo.availMem / (1024 * 1024)
            val totalMb = memInfo.totalMem / (1024 * 1024)
            Pair(freeMb, totalMb)
        } catch (_: Exception) {
            Pair(2048L, 4096L)
        }
    }

    private fun getStorageInfo(): Pair<Long, Long> {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
            val totalBlocks = stat.blockCountLong
            val freeGb = (availableBlocks * blockSize) / (1024 * 1024 * 1024)
            val totalGb = (totalBlocks * blockSize) / (1024 * 1024 * 1024)
            Pair(freeGb, totalGb)
        } catch (_: Exception) {
            Pair(32L, 128L)
        }
    }

    private fun callGeminiAssistant(prompt: String, apiKey: String): String? {
        val modelsToTry = listOf("gemini-3.5-flash", "gemini-3.1-pro-preview")
        val systemInstruction = "Você é Lili, a assistente de IA oficial e ultra inteligente do Lili Launcher no Android. Você tem acesso e controle por voz sobre o dispositivo. Responda em português brasileiro com simpatia, brevidade, clareza, inteligência avançada e emojis adequados."

        for (model in modelsToTry) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

                val jsonBody = JSONObject().apply {
                    put("contents", JSONArray().put(JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().apply {
                            put("text", prompt)
                        }))
                    }))
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().apply {
                            put("text", systemInstruction)
                        }))
                    })
                }

                val request = Request.Builder()
                    .url(url)
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val respText = response.body?.string() ?: continue
                    val root = JSONObject(respText)
                    val candidates = root.optJSONArray("candidates")
                    val first = candidates?.optJSONObject(0)
                    val content = first?.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val text = parts?.optJSONObject(0)?.optString("text")
                    if (!text.isNullOrBlank()) {
                        return text.trim()
                    }
                }
            } catch (_: Exception) {}
        }
        return null
    }

    private fun generateLocalFallback(query: String, weather: WeatherInfo): String {
        return when {
            query.contains("quem é você") || query.contains("seu nome") || query.contains("quem e voce") ->
                "Olá! Eu sou a Lili, sua assistente pessoal integrada ao Lili Launcher! Posso fazer chamadas, enviar mensagens no WhatsApp ou SMS, abrir aplicativos, ligar a lanterna, trocar temas 3D e muito mais. Como posso te ajudar agora? ✨"
            query.contains("olá") || query.contains("ola") || query.contains("bom dia") || query.contains("boa tarde") || query.contains("boa noite") ->
                "Olá! Estou pronta para te ajudar. Pode me pedir para ligar para alguém, mandar mensagens, abrir qualquer app, trocar temas animados ou ver a previsão do tempo! 🌸"
            query.contains("ajuda") || query.contains("o que você faz") || query.contains("comandos") ->
                "Comandos por voz da Lili:\n• 'Ligar para [Contato ou Número]'\n• 'Mandar mensagem para [Contato] dizendo [Texto]'\n• 'Mandar WhatsApp para [Contato] dizendo [Texto]'\n• 'Abrir [Nome do App]'\n• 'Ligar / Desligar lanterna'\n• 'Mudar tema para Cyber, Cosmos, Aurora, Retro ou Floresta'\n• 'Mudar relógio para Analógico, Digital ou HUD'\n• 'Organizar aplicativos'\n• 'Como está a bateria?'\n• 'Previsão do tempo'"
            query.contains("piada") ->
                "Por que o desenvolvedor do Android atravessou a rua? Para testar se o ciclo de vida da Activity mudava de estado! 😄"
            else ->
                "Entendido! Estou aqui no Lili Launcher para você. Posso fazer chamadas, mandar mensagens, abrir seus apps, ligar a lanterna ou aplicar novos temas. O que deseja fazer? ✨"
        }
    }
}
