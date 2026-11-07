package com.linhavital.app.ui.home

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linhavital.app.R
import com.linhavital.app.data.model.ContatoEmergencia
import com.linhavital.app.databinding.ItemContatoBinding

class ContatoAdapter(
    contatosIniciais:
    List<ContatoEmergencia>,

    private val onLigar:
        (String) -> Unit,

    private val onEditar:
        (ContatoEmergencia) -> Unit,

    private val onDeletar:
        (Long) -> Unit,

    private val onIniciarArraste:
        (RecyclerView.ViewHolder) -> Unit

) : RecyclerView.Adapter<ContatoAdapter.ViewHolder>() {

    private val contatos =
        contatosIniciais.toMutableList()

    inner class ViewHolder(
        val binding:
        ItemContatoBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    )

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding =
            ItemContatoBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        return ViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val contato =
            contatos[position]

        val context =
            holder.itemView.context

        holder.binding
            .tvAvatarContato
            .text =
            contato
                .nome
                .trim()
                .firstOrNull()
                ?.uppercase()
                ?: "?"

        holder.binding
            .tvNomeContato
            .text =
            contato.nome

        holder.binding
            .tvTelefoneContato
            .text =
            contato.telefone

        holder.binding
            .tvTipoContato
            .text =
            contato.tipoContato

        /*
         * O primeiro contato da lista
         * é sempre o prioritário.
         */
        holder.binding
            .tvPrioritario
            .visibility =
            if (
                position == 0
            ) {

                View.VISIBLE

            } else {

                View.GONE
            }

        holder.binding
            .btnArrastar
            .visibility =
            if (
                contatos.size > 1
            ) {

                View.VISIBLE

            } else {

                View.INVISIBLE
            }

        holder.binding
            .btnLigar
            .contentDescription =
            context.getString(
                R.string.lv_call_contact,
                contato.nome
            )

        holder.binding
            .btnEditar
            .contentDescription =
            context.getString(
                R.string.lv_edit_contact,
                contato.nome
            )

        holder.binding
            .btnDeletar
            .contentDescription =
            context.getString(
                R.string.lv_delete_contact,
                contato.nome
            )

        holder.binding
            .btnArrastar
            .contentDescription =
            context.getString(
                R.string.lv_reorder_contact,
                contato.nome
            )

        holder.binding
            .btnLigar
            .setOnClickListener {

                onLigar(
                    contato.telefone
                )
            }

        holder.binding
            .btnEditar
            .setOnClickListener {

                onEditar(
                    contato
                )
            }

        holder.binding
            .btnDeletar
            .setOnClickListener {

                contato
                    .id
                    ?.let(
                        onDeletar
                    )
            }

        holder.binding
            .btnArrastar
            .setOnTouchListener {
                    _,
                    event ->

                if (
                    event.actionMasked ==
                    MotionEvent.ACTION_DOWN
                ) {

                    onIniciarArraste(
                        holder
                    )

                    true

                } else {

                    false
                }
            }
    }

    override fun getItemCount():
            Int =
        contatos.size

    fun atualizar(
        novosContatos:
        List<ContatoEmergencia>
    ) {

        contatos.clear()

        contatos.addAll(
            novosContatos
        )

        notifyDataSetChanged()
    }

    fun moverItem(
        origem: Int,
        destino: Int
    ): Boolean {

        if (
            origem !in contatos.indices ||
            destino !in contatos.indices ||
            origem == destino
        ) {

            return false
        }

        val contato =
            contatos.removeAt(
                origem
            )

        contatos.add(
            destino,
            contato
        )

        notifyItemMoved(
            origem,
            destino
        )

        return true
    }

    fun finalizarReordenacao() {

        /*
         * Atualiza principalmente o selo
         * "Prioritário", que depende da posição 0.
         */
        notifyDataSetChanged()
    }

    fun contatosAtuais():
            List<ContatoEmergencia> =
        contatos.toList()
}