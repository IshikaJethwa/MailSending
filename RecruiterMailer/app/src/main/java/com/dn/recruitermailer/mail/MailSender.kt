package com.dn.recruitermailer.mail

import com.dn.recruitermailer.data.Profile
import java.io.File
import java.net.UnknownHostException
import java.util.Properties
import javax.mail.AuthenticationFailedException
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

/** Sends mail over SMTP. Call from a background thread. */
object MailSender {

    fun send(p: Profile, to: String) {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.host", p.smtpHost)
            put("mail.smtp.port", p.smtpPort.toString())
            put("mail.smtp.connectiontimeout", "15000")
            put("mail.smtp.timeout", "30000")
            if (p.smtpPort == 465) put("mail.smtp.ssl.enable", "true")
            else put("mail.smtp.starttls.enable", "true")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication() = PasswordAuthentication(p.from, p.password)
        })

        val multipart = MimeMultipart().apply {
            addBodyPart(MimeBodyPart().apply { setText(p.body, "utf-8") })
            p.attachments.forEach { a ->
                addBodyPart(MimeBodyPart().apply {
                    attachFile(File(a.path))
                    setFileName(a.name)
                })
            }
        }

        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(p.from))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            setSubject(p.subject, "utf-8")
            setContent(multipart)
        }

        Transport.send(message)
    }

    fun friendlyError(t: Throwable): String {
        var cause: Throwable? = t
        while (cause != null) {
            if (cause is AuthenticationFailedException)
                return "Login failed. Check your email and app password."
            if (cause is UnknownHostException)
                return "No internet connection, or the SMTP server name is wrong."
            cause = cause.cause
        }
        return t.message ?: "Email not sent."
    }
}
