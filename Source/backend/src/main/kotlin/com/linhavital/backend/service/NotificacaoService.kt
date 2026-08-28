package com.linhavital.backend.service

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Service

@Service
class NotificacaoService(
    private val firebaseAppProvider: ObjectProvider<FirebaseApp>
) {
    fun enviarNotificacao(token: String?, titulo: String, corpo: String): Boolean {
        if (token.isNullOrBlank()) return false
        val firebaseApp = firebaseAppProvider.getIfAvailable() ?: return false

        return runCatching {
            val message = Message.builder()
                .setToken(token)
                .setNotification(
                    Notification.builder()
                        .setTitle(titulo)
                        .setBody(corpo)
                        .build()
                )
                .build()
            FirebaseMessaging.getInstance(firebaseApp).send(message)
            true
        }.getOrDefault(false)
    }
}
