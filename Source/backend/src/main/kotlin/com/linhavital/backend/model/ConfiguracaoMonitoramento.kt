package com.linhavital.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "configuracaomonitoramento")
class ConfiguracaoMonitoramento(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_configuracao_monitoramento")
    var id: Long = 0,

    @Column(name = "ativo_monitoramento", nullable = false)
    var ativo: Boolean = true,

    @Column(name = "intervalo_minutos", nullable = false)
    var intervaloMinutos: Int = 35,

    @Column(name = "ultima_confirmacao", nullable = false)
    var ultimaConfirmacao: LocalDateTime = LocalDateTime.now(),

    @Column(name = "alerta_inatividade_aberto", nullable = false)
    var alertaInatividadeAberto: Boolean = false,

    @OneToOne
    @JoinColumn(name = "fk_usuario_id_usuario", unique = true, nullable = false)
    var usuario: Usuario
)
