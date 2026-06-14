package com.linhavital.app.data.api

import com.linhavital.app.data.model.ContatoEmergencia
import com.linhavital.app.data.model.Usuario
import retrofit2.http.*

interface ApiService {

    @GET("usuarios")
    suspend fun getUsuarios(): List<Usuario>

    @POST("usuarios")
    suspend fun criarUsuario(@Body usuario: Usuario): Usuario

    @GET("usuarios/{id}")
    suspend fun getUsuario(@Path("id") id: Long): Usuario

    @GET("contatos")
    suspend fun getContatos(): List<ContatoEmergencia>

    @POST("contatos")
    suspend fun criarContato(@Body contato: ContatoEmergencia): ContatoEmergencia

    @DELETE("contatos/{id}")
    suspend fun deletarContato(@Path("id") id: Long)

    @GET("contatos/usuario/{usuarioId}")
    suspend fun getContatosDoUsuario(
        @Path("usuarioId") usuarioId: Long
    ): List<ContatoEmergencia>

    @POST("contatos/usuario/{usuarioId}")
    suspend fun criarContatoDoUsuario(
        @Path("usuarioId") usuarioId: Long,
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
}