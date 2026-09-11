package com.linhavital.app.data.api

import com.linhavital.app.data.model.*
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): UsuarioSessao

    @POST("usuarios")
    suspend fun criarUsuario(
        @Body usuario: Usuario
    ): UsuarioSessao

    @GET("usuarios/{id}")
    suspend fun getUsuario(
        @Path("id") id: Long
    ): UsuarioSessao

    @GET("contatos/usuario/{usuarioId}")
    suspend fun getContatosDoUsuario(
        @Path("usuarioId") usuarioId: Long
    ): List<ContatoEmergencia>

    @POST("contatos/usuario/{usuarioId}")
    suspend fun criarContatoDoUsuario(
        @Path("usuarioId") usuarioId: Long,
        @Body contato: ContatoEmergencia
    ): ContatoEmergencia

    @PUT("contatos/usuario/{usuarioId}/{contatoId}")
    suspend fun atualizarContatoDoUsuario(
        @Path("usuarioId") usuarioId: Long,
        @Path("contatoId") contatoId: Long,
        @Body contato: ContatoEmergencia
    ): ContatoEmergencia

    @DELETE("contatos/usuario/{usuarioId}/{contatoId}")
    suspend fun deletarContatoDoUsuario(
        @Path("usuarioId") usuarioId: Long,
        @Path("contatoId") contatoId: Long
    )

    @POST("alerta/panico/{usuarioId}")
    suspend fun criarAlertaPanico(
        @Path("usuarioId") usuarioId: Long
    ): Map<String, Any>

    /*
     * Registra os eventos da cascata de emergência.
     *
     * Status possíveis no backend:
     * TENTATIVA
     * NAO_ATENDIDO
     * ATENDIDO
     */
    @POST("notificacoes/alerta/{alertaId}/tentativa")
    suspend fun registrarTentativaContato(
        @Path("alertaId") alertaId: Long,
        @Body request: TentativaContatoRequest
    ): Map<String, Any>

    @GET("monitoramento/status/{usuarioId}")
    suspend fun getMonitoramentoStatus(
        @Path("usuarioId") usuarioId: Long
    ): MonitoramentoStatus

    @PUT("monitoramento/configuracao/{usuarioId}")
    suspend fun configurarMonitoramento(
        @Path("usuarioId") usuarioId: Long,
        @Body request: ConfiguracaoMonitoramentoRequest
    ): MonitoramentoStatus

    @POST("monitoramento/check-in/{usuarioId}")
    suspend fun registrarCheckIn(
        @Path("usuarioId") usuarioId: Long
    ): MonitoramentoStatus
}