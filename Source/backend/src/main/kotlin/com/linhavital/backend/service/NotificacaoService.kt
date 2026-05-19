package com.linhavital.backend.service

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.springframework.stereotype.Service

@Service
class NotificacaoService {

    fun enviarNotificacao(token: String?, titulo: String, corpo: String) {

        if (token.isNullOrBlank()) {
            println("⚠️ Token inválido. Notificação não enviada.")
            return
        }

        try {
            println("📡 Enviando notificação...")

            val notification = Notification.builder()
                .setTitle(titulo)
                .setBody(corpo)
                .build()

            val message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .build()

            val response = FirebaseMessaging.getInstance().send(message)

            println("✅ Notificação enviada com sucesso: $response")

        } catch (e: Exception) {
            println("❌ Erro ao enviar notificação: ${e.message}")
        }
    }
}