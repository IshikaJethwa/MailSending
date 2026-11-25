package com.dn.recruitermailer.ui

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dn.recruitermailer.data.Profile
import com.dn.recruitermailer.data.ProfileStore
import com.dn.recruitermailer.data.SentEntry
import com.dn.recruitermailer.mail.MailSender
import com.dn.recruitermailer.ui.theme.Mint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date

@Composable
fun SendScreen(store: ProfileStore, profile: Profile, onEdit: () -> Unit) {
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    var to by rememberSaveable { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var confirmDuplicate by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }
    val history = remember { mutableStateListOf<SentEntry>().apply { addAll(store.loadSent()) } }

    fun doSend() {
        val target = to.trim()
        sending = true
        result = null
        focus.clearFocus()
        scope.launch {
            val r = runCatching { withContext(Dispatchers.IO) { MailSender.send(profile, target) } }
            sending = false
            if (r.isSuccess) {
                history.add(0, SentEntry(target, System.currentTimeMillis()))
                store.saveSent(history.toList())
                result = true to "Sent to $target"
                to = ""
            } else {
                result = false to MailSender.friendlyError(r.exceptionOrNull()!!)
            }
        }
    }

    fun onSendClick() {
        val target = to.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(target).matches()) {
            result = false to "Enter a valid recruiter email."
            return
        }
        if (history.any { it.to.equals(target, ignoreCase = true) }) confirmDuplicate = true
        else doSend()
    }

    if (confirmDuplicate) {
        AlertDialog(
            onDismissRequest = { confirmDuplicate = false },
            title = { Text("Already sent") },
            text = { Text("You already emailed ${to.trim()}. Send again?") },
            confirmButton = {
                TextButton(onClick = { confirmDuplicate = false; doSend() }) { Text("Send again") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDuplicate = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        GradientHeader(
            title = "Recruiter Mailer",
            subtitle = "${history.size} sent so far",
            trailing = {
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, "Edit profile", tint = Color.White) }
            }
        )

        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .offset(y = (-12).dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ---- Compose: the main action ----
            SectionCard("Send to a recruiter", Icons.AutoMirrored.Filled.Send) {
                OutlinedTextField(
                    value = to, onValueChange = { to = it; result = null },
                    label = { Text("To") },
                    placeholder = { Text("recruiter@company.com") },
                    leadingIcon = { Icon(Icons.Filled.Person, null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    textStyle = MaterialTheme.typography.titleMedium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { if (!sending) onSendClick() }),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = ::onSendClick,
                    enabled = !sending,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (sending) {
                        CircularProgressIndicator(
                            strokeWidth = 2.5.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Sending…", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, null)
                        Spacer(Modifier.width(10.dp))
                        Text("Send email", fontWeight = FontWeight.Bold)
                    }
                }
                AnimatedVisibility(result != null) {
                    result?.let { (ok, msg) -> ResultBanner(ok, msg) }
                }
            }

            // ---- What will be sent ----
            SectionCard("Your profile", Icons.Filled.MailOutline) {
                InfoLine("From", profile.from)
                InfoLine("Subject", profile.subject)
                Spacer(Modifier.height(6.dp))
                profile.attachments.forEach { a ->
                    Row(
                        Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.PictureAsPdf, null, tint = Color(0xFFE5484D), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            a.name, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f)
                        )
                        Text(
                            formatSize(a.size), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(onClick = { showPreview = !showPreview }) {
                    Text(if (showPreview) "Hide message" else "Preview message")
                }
                AnimatedVisibility(showPreview) {
                    Text(
                        profile.body,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(14.dp)
                    )
                }
            }

            // ---- History ----
            SectionCard("Sent", Icons.Filled.History) {
                if (history.isEmpty()) {
                    Text(
                        "No emails sent yet. Enter a recruiter's email above to send your first one.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                val fmt = remember { DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT) }
                history.forEachIndexed { i, e ->
                    if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    Row(Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                e.to.first().uppercase(),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(e.to, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                            Text(
                                fmt.format(Date(e.at)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.navigationBarsPadding().height(12.dp))
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.padding(vertical = 3.dp)) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(72.dp)
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ResultBanner(ok: Boolean, msg: String) {
    val tint = if (ok) Mint else MaterialTheme.colorScheme.error
    Row(
        Modifier
            .padding(top = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(tint.copy(alpha = 0.12f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(if (ok) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline, null, tint = tint)
        Spacer(Modifier.width(10.dp))
        Text(msg, color = tint, fontWeight = FontWeight.Medium)
    }
}
