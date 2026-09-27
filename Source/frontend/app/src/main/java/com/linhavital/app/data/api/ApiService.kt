package com.linhavital.app.data.api

import com.linhavital.app.data.model.*
import retrofit2.http.*

interface ApiService {

    /*
     * =====================================================
     * AUTENTICAÇÃO
     * =====================================================
     */

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): AuthResponse

    @POST("auth/google")
    suspend fun loginGoogle(
        @Body request: GoogleLoginRequest
    ): GoogleLoginResponse

    @POST("auth/google/cadastro")
    suspend fun cadastrarGoogle(
        @Body request: GoogleCadastroRequest
    ): AuthResponse

    @GET("auth/me")
    suspend fun usuarioAtual(): UsuarioSessao

    @POST("auth/logout")
    suspend fun logout(): Map<String, String>

    /*
     * =====================================================
     * USUÁRIOS
     * =====================================================
     */

    @POST("usuarios")
    suspend fun criarUsuario(
        @Body usuario: Usuario
    ): UsuarioSessao

    @GET("usuarios/{id}")
    suspend fun getUsuario(
        @Path("id") id: Long
    ): UsuarioSessao

    /*
     * =====================================================
     * CONTATOS
     * =====================================================
     */

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

    /*
     * =====================================================
     * ALERTAS
     * =====================================================
     */

    @POST("alerta/panico/{usuarioId}")
    suspend fun criarAlertaPanico(
        @Path("usuarioId") usuarioId: Long
    ): Map<String, Any>

    /*
     * =====================================================
     * HISTÓRICO DE NOTIFICAÇÃO
     * =====================================================
     */

    @POST("notificacoes/alerta/{alertaId}/tentativa")
    suspend fun registrarTentativaContato(
        @Path("alertaId") alertaId: Long,
        @Body request: TentativaContatoRequest
    ): Map<String, Any>

    /*
     * =====================================================
     * MONITORAMENTO
     * =====================================================
     */

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

    /*
     * =====================================================
     * LOCALIZAÇÃO
     * =====================================================
     */

    @POST("localizacoes")
    suspend fun registrarLocalizacao(
        @Body request: LocalizacaoRequest
    ): LocalizacaoResponse

    @GET("localizacoes/ultima")
    suspend fun obterUltimaLocalizacao():
            LocalizacaoResponse

    @GET("localizacoes")
    suspend fun listarLocalizacoes():
            List<LocalizacaoResponse>
}