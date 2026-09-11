package com.linhavital.app.ui.home
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.linhavital.app.R

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linhavital.app.data.model.ContatoEmergencia
import com.linhavital.app.databinding.ItemContatoBinding

class ContatoAdapter(
    contatos: List<ContatoEmergencia>,
    private val onLigar: (String) -> Unit,
    private val onEditar: (ContatoEmergencia) -> Unit,
    private val onDeletar: (Long) -> Unit
) : ListAdapter<ContatoEmergencia, ContatoAdapter.ViewHolder>(DIFF) {

    init {
        stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        submitList(contatos.toList())
    }

    inner class ViewHolder(val binding: ItemContatoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContatoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contato = getItem(position)

        holder.binding.tvAvatarContato.text =
            contato.nome.trim().firstOrNull()?.uppercase() ?: "?"

        holder.binding.tvNomeContato.text = contato.nome
        holder.binding.tvTelefoneContato.text = contato.telefone
        holder.binding.tvTipoContato.text = contato.tipoContato

        val context = holder.itemView.context
        holder.binding.btnLigar.contentDescription = context.getString(R.string.lv_call_contact, contato.nome)
        holder.binding.btnEditar.contentDescription = context.getString(R.string.lv_edit_contact, contato.nome)
        holder.binding.btnDeletar.contentDescription = context.getString(R.string.lv_delete_contact, contato.nome)

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

    fun atualizar(novosContatos: List<ContatoEmergencia>) {
        submitList(novosContatos.toList())
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ContatoEmergencia>() {
            override fun areItemsTheSame(oldItem: ContatoEmergencia, newItem: ContatoEmergencia): Boolean =
                if (oldItem.id != null && newItem.id != null) oldItem.id == newItem.id
                else oldItem == newItem

            override fun areContentsTheSame(oldItem: ContatoEmergencia, newItem: ContatoEmergencia): Boolean =
                oldItem == newItem
        }
    }
}
