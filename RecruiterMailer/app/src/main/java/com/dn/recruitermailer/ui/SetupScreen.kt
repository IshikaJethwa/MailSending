package com.dn.recruitermailer.ui

import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dn.recruitermailer.data.Attachment
import com.dn.recruitermailer.data.Profile
import com.dn.recruitermailer.data.ProfileStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val FieldShape = RoundedCornerShape(14.dp)

@Composable
fun SetupScreen(
    store: ProfileStore,
    existing: Profile?,
    onSaved: (Profile) -> Unit,
    onBack: (() -> Unit)?
) {
    val scope = rememberCoroutineScope()
    var from by rememberSaveable { mutableStateOf(existing?.from ?: "") }
    var password by rememberSaveable { mutableStateOf(existing?.password ?: "") }
    var subject by rememberSaveable { mutableStateOf(existing?.subject ?: "") }
    var body by rememberSaveable { mutableStateOf(existing?.body ?: "") }
    var host by rememberSaveable { mutableStateOf(existing?.smtpHost ?: "smtp.gmail.com") }
    var port by rememberSaveable { mutableStateOf((existing?.smtpPort ?: 587).toString()) }
    val attachments = remember { mutableStateListOf<Attachment>().apply { existing?.attachments?.let { addAll(it) } } }
    var showPassword by remember { mutableStateOf(false) }
    var showAdvanced by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    if (onBack != null) BackHandler(onBack = onBack)

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        scope.launch {
            val imported = withContext(Dispatchers.IO) { uris.mapNotNull { store.importPdf(it) } }
            imported.forEach { a ->
                attachments.removeAll { it.name == a.name }
                attachments.add(a)
            }
        }
    }

    fun save() {
        val f = from.trim()
        error = when {
            !Patterns.EMAIL_ADDRESS.matcher(f).matches() -> "Enter a valid From email."
            password.isBlank() -> "Enter your app password."
            subject.isBlank() -> "Add a subject."
            body.isBlank() -> "Write the email body."
            port.toIntOrNull() == null -> "SMTP port must be a number."
            else -> null
        }
        if (error != null) return
        val p = Profile(f, password.trim(), subject.trim(), body, attachments.toList(), host.trim(), port.toInt())
        store.save(p)
        onSaved(p)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        GradientHeader(
            title = if (existing == null) "Set up your profile" else "Edit profile",
            subtitle = "Saved once, used for every email",
            trailing = {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                }
            }
        )

        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .offset(y = (-12).dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionCard("Sender", Icons.Filled.AlternateEmail) {
                OutlinedTextField(
                    value = from, onValueChange = { from = it },
                    label = { Text("From (your email)") },
                    leadingIcon = { Icon(Icons.Filled.AlternateEmail, null) },
                    singleLine = true, shape = FieldShape,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("App password") },
                    leadingIcon = { Icon(Icons.Filled.Key, null) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                if (showPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true, shape = FieldShape,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    supportingText = { Text("Gmail: Google Account → Security → App passwords") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SectionCard("Message", Icons.Filled.EditNote) {
                OutlinedTextField(
                    value = subject, onValueChange = { subject = it },
                    label = { Text("Subject") },
                    leadingIcon = { Icon(Icons.Filled.Subject, null) },
                    singleLine = true, shape = FieldShape,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = body, onValueChange = { body = it },
                    label = { Text("Email body") },
                    placeholder = { Text("Hi,\n\nI'm a software developer with experience in…") },
                    minLines = 8, shape = FieldShape,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            SectionCard("Attachments", Icons.Filled.AttachFile) {
                if (attachments.isEmpty()) {
                    Text(
                        "No PDFs yet. Add your resume and any other documents.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                }
                attachments.forEach { a ->
                    PdfRow(a, onRemove = { attachments.remove(a) })
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedButton(
                    onClick = { picker.launch(arrayOf("application/pdf")) },
                    shape = FieldShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.UploadFile, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add PDFs")
                }
            }

            SectionCard("Mail server", Icons.Filled.Settings) {
                TextButton(onClick = { showAdvanced = !showAdvanced }) {
                    Text(if (showAdvanced) "Hide SMTP settings" else "Show SMTP settings ($host:$port)")
                    Icon(if (showAdvanced) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null)
                }
                AnimatedVisibility(showAdvanced) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = host, onValueChange = { host = it },
                            label = { Text("SMTP host") }, singleLine = true, shape = FieldShape,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = port, onValueChange = { port = it.filter(Char::isDigit) },
                            label = { Text("Port") }, singleLine = true, shape = FieldShape,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(96.dp)
                        )
                    }
                }
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
            }

            Button(
                onClick = ::save,
                shape = FieldShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Save profile", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.navigationBarsPadding().height(12.dp))
        }
    }
}

@Composable
private fun PdfRow(a: Attachment, onRemove: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(start = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE5484D).copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.PictureAsPdf, null, tint = Color(0xFFE5484D))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(a.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
            Text(
                formatSize(a.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onRemove) { Icon(Icons.Filled.Close, "Remove ${a.name}") }
    }
}
