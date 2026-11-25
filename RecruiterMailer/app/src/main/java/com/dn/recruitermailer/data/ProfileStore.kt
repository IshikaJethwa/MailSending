package com.dn.recruitermailer.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.provider.OpenableColumns
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class Attachment(val name: String, val path: String, val size: Long)

data class Profile(
    val from: String,
    val password: String,       // Gmail App Password (not your normal password)
    val subject: String,
    val body: String,
    val attachments: List<Attachment>,
    val smtpHost: String = "smtp.gmail.com",
    val smtpPort: Int = 587
)

data class SentEntry(val to: String, val at: Long)

/** Saves profile + sent log in encrypted prefs, PDFs in app-private storage. */
class ProfileStore(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        val key = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context, "mailer_secure", key,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val dir: File
        get() = File(context.filesDir, "attachments").apply { mkdirs() }

    // ---------- Profile ----------
    fun load(): Profile? = prefs.getString(KEY_PROFILE, null)?.let {
        runCatching { profileFromJson(JSONObject(it)) }.getOrNull()
    }

    fun save(p: Profile) {
        prefs.edit().putString(KEY_PROFILE, profileToJson(p).toString()).apply()
        // Delete PDFs no longer used by the profile
        val keep = p.attachments.map { File(it.path).name }.toSet()
        dir.listFiles()?.filter { it.name !in keep }?.forEach { it.delete() }
    }

    /** Copies a picked PDF into app storage so it survives restarts. */
    fun importPdf(uri: Uri): Attachment? = runCatching {
        val name = displayName(uri) ?: "attachment_${System.currentTimeMillis()}.pdf"
        val target = File(dir, name)
        context.contentResolver.openInputStream(uri)!!.use { input ->
            target.outputStream().use { input.copyTo(it) }
        }
        Attachment(name, target.absolutePath, target.length())
    }.getOrNull()

    private fun displayName(uri: Uri): String? =
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { c -> if (c.moveToFirst()) c.getString(0) else null }

    // ---------- Sent log (newest first) ----------
    fun loadSent(): List<SentEntry> {
        val raw = prefs.getString(KEY_SENT, null) ?: return emptyList()
        val arr = JSONArray(raw)
        return (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            SentEntry(o.getString("to"), o.getLong("at"))
        }
    }

    fun saveSent(list: List<SentEntry>) {
        val arr = JSONArray()
        list.forEach { arr.put(JSONObject().put("to", it.to).put("at", it.at)) }
        prefs.edit().putString(KEY_SENT, arr.toString()).apply()
    }

    // ---------- JSON ----------
    private fun profileToJson(p: Profile) = JSONObject()
        .put("from", p.from).put("password", p.password)
        .put("subject", p.subject).put("body", p.body)
        .put("smtpHost", p.smtpHost).put("smtpPort", p.smtpPort)
        .put("attachments", JSONArray().apply {
            p.attachments.forEach {
                put(JSONObject().put("name", it.name).put("path", it.path).put("size", it.size))
            }
        })

    private fun profileFromJson(o: JSONObject): Profile {
        val arr = o.optJSONArray("attachments") ?: JSONArray()
        val atts = (0 until arr.length()).map {
            val a = arr.getJSONObject(it)
            Attachment(a.getString("name"), a.getString("path"), a.optLong("size"))
        }.filter { File(it.path).exists() }
        return Profile(
            from = o.getString("from"),
            password = o.optString("password"),
            subject = o.optString("subject"),
            body = o.optString("body"),
            attachments = atts,
            smtpHost = o.optString("smtpHost", "smtp.gmail.com"),
            smtpPort = o.optInt("smtpPort", 587)
        )
    }

    private companion object {
        const val KEY_PROFILE = "profile"
        const val KEY_SENT = "sent_log"
    }
}
