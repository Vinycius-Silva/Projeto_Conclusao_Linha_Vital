package com.linhavital.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linhavital.app.data.model.ContatoEmergencia
import com.linhavital.app.databinding.ItemContatoBinding

class ContatoAdapter(
    private var contatos: List<ContatoEmergencia>,
    private val onLigar: (String) -> Unit,
    private val onEditar: (ContatoEmergencia) -> Unit,
    private val onDeletar: (Long) -> Unit
) : RecyclerView.Adapter<ContatoAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemContatoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContatoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contato = contatos[position]

        holder.binding.tvAvatarContato.text =
            contato.nome.trim().firstOrNull()?.uppercase() ?: "?"

        holder.binding.tvNomeContato.text = contato.nome
        holder.binding.tvTelefoneContato.text = contato.telefone
        holder.binding.tvTipoContato.text = contato.tipoContato

        holder.binding.btnLigar.setOnClickListener {
            onLigar(contato.telefone)
        }

        holder.binding.btnEditar.setOnClickListener {
            onEditar(contato)
        }

        holder.binding.btnDeletar.setOnClickListener {
            contato.id?.let(onDeletar)
        }
    }

    override fun getItemCount() = contatos.size

    fun atualizar(novosContatos: List<ContatoEmergencia>) {
        contatos = novosContatos
        notifyDataSetChanged()
    }
}
