package com.example.kotlinactivities.network

import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import java.util.Properties

fun sendEmail(toEmail: String, subject: String, messageBody: String) {
    val username = "camotesisland1@gmail.com" // Replace with your Gmail
    val password = "fmbw sbhq oqgg dvpg" // Replace with your Gmail password or app password

    val properties = Properties().apply {
        put("mail.smtp.auth", "true")
        put("mail.smtp.starttls.enable", "true")
        put("mail.smtp.host", "smtp.gmail.com")
        put("mail.smtp.port", "587")
    }

    val session = Session.getInstance(properties, object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(username, password)
        }
    })

    try {
        val message = MimeMessage(session)
        message.setFrom(InternetAddress(username))
        message.setRecipients(
            Message.RecipientType.TO,
            InternetAddress.parse(toEmail)
        )
        message.subject = subject
        message.setText(messageBody)

        Transport.send(message)
        println("Email sent successfully!")
    } catch (e: MessagingException) {
        e.printStackTrace()
        println("Failed to send email: ${e.message}")
    }
}
