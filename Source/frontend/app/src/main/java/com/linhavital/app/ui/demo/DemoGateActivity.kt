package com.linhavital.app.ui.demo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.linhavital.app.BuildConfig
import com.linhavital.app.ui.auth.LoginActivity
import com.linhavital.app.utils.DemoAccess
import com.linhavital.app.utils.DemoValidationResult
import kotlinx.coroutines.launch

class DemoGateActivity :
    AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        if (!BuildConfig.DEMO_MODE) {

            abrirAplicativo()

            return
        }

        mostrarValidando()

        validarAcesso()
    }

    private fun validarAcesso() {

        lifecycleScope.launch {

            when (
                val resultado =
                    DemoAccess
                        .validarComServidor()
            ) {

                DemoValidationResult.Allowed -> {

                    abrirAplicativo()
                }

                DemoValidationResult.Blocked -> {

                    abrirTelaBloqueada()
                }

                is DemoValidationResult.Error -> {

                    mostrarErro(
                        resultado.message
                    )
                }
            }
        }
    }

    private fun abrirAplicativo() {

        startActivity(
            Intent(
                this,
                LoginActivity::class.java
            )
        )

        finish()
    }

    private fun abrirTelaBloqueada() {

        val intent =
            Intent(
                this,
                DemoExpiredActivity::class.java
            ).apply {

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        startActivity(
            intent
        )

        finish()
    }

    private fun mostrarValidando() {

        val density =
            resources
                .displayMetrics
                .density

        fun dp(
            value: Int
        ): Int {

            return (
                    value *
                            density
                    ).toInt()
        }

        val container =
            LinearLayout(
                this
            ).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER

                setPadding(
                    dp(32),
                    dp(32),
                    dp(32),
                    dp(32)
                )

                setBackgroundColor(
                    Color.WHITE
                )
            }

        val progress =
            ProgressBar(
                this
            )

        val title =
            TextView(
                this
            ).apply {

                text =
                    "Linha Vital - Demo"

                textSize =
                    24f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.rgb(
                        220,
                        0,
                        40
                    )
                )

                setPadding(
                    0,
                    dp(24),
                    0,
                    dp(12)
                )
            }

        val message =
            TextView(
                this
            ).apply {

                text =
                    "Validando acesso ao evento..."

                textSize =
                    16f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.DKGRAY
                )
            }

        container.addView(
            progress
        )

        container.addView(
            title,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        container.addView(
            message,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(
            container
        )
    }

    private fun mostrarErro(
        mensagem: String
    ) {

        val density =
            resources
                .displayMetrics
                .density

        fun dp(
            value: Int
        ): Int {

            return (
                    value *
                            density
                    ).toInt()
        }

        val container =
            LinearLayout(
                this
            ).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER

                setPadding(
                    dp(32),
                    dp(32),
                    dp(32),
                    dp(32)
                )

                setBackgroundColor(
                    Color.WHITE
                )
            }

        val title =
            TextView(
                this
            ).apply {

                text =
                    "Não foi possível validar o acesso"

                textSize =
                    22f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.rgb(
                        220,
                        0,
                        40
                    )
                )
            }

        val message =
            TextView(
                this
            ).apply {

                text =
                    mensagem

                textSize =
                    16f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    Color.DKGRAY
                )

                setPadding(
                    0,
                    dp(20),
                    0,
                    dp(24)
                )
            }

        val retryButton =
            Button(
                this
            ).apply {

                text =
                    "Tentar novamente"

                setOnClickListener {

                    mostrarValidando()

                    validarAcesso()
                }
            }

        val closeButton =
            Button(
                this
            ).apply {

                text =
                    "Fechar aplicativo"

                setOnClickListener {

                    finishAffinity()
                }
            }

        container.addView(
            title
        )

        container.addView(
            message
        )

        container.addView(
            retryButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        container.addView(
            closeButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(
            container
        )
    }
}