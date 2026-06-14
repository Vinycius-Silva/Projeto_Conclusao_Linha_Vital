package com.linhavital.app.data.repository

import android.util.Log
import com.linhavital.app.data.api.ApiClient
import com.linhavital.app.data.api.ApiService
import com.linhavital.app.data.model.ContatoEmergencia

class ContatoRepository {

    private val api = ApiClient.create<ApiService>()

    suspend fun listarContatos(usuarioId: Long): Result<List<ContatoEmergencia>> {
        return try {
            Result.success(api.getContatosDoUsuario(usuarioId))
        } catch (e: Exception) {
            Log.e("ContatoRepository", "Erro ao listar: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun cadastrarContato(
        usuarioId: Long,
        contato: ContatoEmergencia
    ): Result<ContatoEmergencia> {
        return try {
            Result.success(api.criarContatoDoUsuario(usuarioId, contato))
        } catch (e: Exception) {
            Log.e("ContatoRepository", "Erro ao cadastrar: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deletarContato(usuarioId: Long, contatoId: Long): Result<Unit> {
        return try {
            Log.d("ContatoRepository", "Deletando contato ID: $contatoId do usuário ID: $usuarioId")
            api.deletarContatoDoUsuario(usuarioId, contatoId)
            Log.d("ContatoRepository", "Contato $contatoId deletado com sucesso para usuário $usuarioId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ContatoRepository", "Erro ao deletar: ${e.message}")
            Result.failure(e)
        }
    }
}