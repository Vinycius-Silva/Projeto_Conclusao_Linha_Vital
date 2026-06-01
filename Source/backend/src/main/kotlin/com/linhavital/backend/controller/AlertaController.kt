package com.linhavital.backend.controller

import com.linhavital.backend.model.Alerta
import com.linhavital.backend.repository.AlertaRepository
import com.linhavital.backend.repository.UsuarioRepository
import com.linhavital.backend.service.NotificacaoService
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/alerta")
class AlertaController(
    private val alertaRepository: AlertaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val notificacaoService: NotificacaoService
) {

    @PostMapping("/panico/{id}")
    fun alertaPanico(@PathVariable id: Long): String {

        println("🚀 Endpoint /alerta/panico chamado com ID: $id")

        val usuarioOptional = usuarioRepository.findById(id)

        println("🔎 Resultado da busca no banco: $usuarioOptional")

        if (usuarioOptional.isEmpty) {
            println("❌ Usuário NÃO encontrado no banco")
            return "Usuário não encontrado"
        }

        val usuario = usuarioOptional.get()

        println("👤 Usuário encontrado: ${usuario.nome}")
        println("📱 Token do usuário: ${usuario.fcmToken}")

        // Criando alerta
        val alerta = Alerta(
            tipo = "PANICO",
            dataHora = LocalDateTime.now(),
            status = "ATIVO",
            usuario = usuario
        )

        alertaRepository.save(alerta)

        println("💾 Alerta salvo com sucesso no banco")

        // Enviando notificação
        println("📡 Chamando serviço de notificação...")

        notificacaoService.enviarNotificacao(
            usuario.fcmToken,
            "🚨 ALERTA DE PÂNICO",
            "O usuário ${usuario.nome} acionou o botão de pânico!"
        )

        println("🏁 Fim do fluxo de alerta")

        return "Alerta processado"
    }
}