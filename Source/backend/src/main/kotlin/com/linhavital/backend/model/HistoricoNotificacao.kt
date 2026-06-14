package com.linhavital.backend.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "historiconotificacao")
data class HistoricoNotificacao(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacao")
    val id: Long = 0,

    @Column(name = "status_notificacao")
    val status: String,

    @Column(name = "data_hora_notificacao")
    val dataHora: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "fk_contato_id_contato")
    val contato: ContatoEmergencia,

    @ManyToOne
    @JoinColumn(name = "fk_alerta_id_alerta")
    val alerta: Alerta
)