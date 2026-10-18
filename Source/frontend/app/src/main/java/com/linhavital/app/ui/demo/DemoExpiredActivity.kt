package com.linhavital.app.ui.demo

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.linhavital.app.utils.DemoAccess

class DemoExpiredActivity : AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        val density =
            resources.displayMetrics.density

        fun dp(value: Int): Int =
            (value * density).toInt()

        val container =
            LinearLayout(this).apply {

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
            TextView(this).apply {

                text =
                    "Linha Vital - Demo"

                textSize =
                    26f

                setTextColor(
                    Color.rgb(
                        255,
                        0,
                        59
                    )
                )

                gravity =
                    Gravity.CENTER
            }

        val message =
            TextView(this).apply {

                text =
                    DemoAccess.BLOCK_MESSAGE

                textSize =
                    18f

                setTextColor(
                    Color.DKGRAY
                )

                gravity =
                    Gravity.CENTER

                setPadding(
                    0,
                    dp(24),
                    0,
                    dp(32)
                )
            }

        val closeButton =
            Button(this).apply {

                text =
                    "Fechar aplicativo"

                setOnClickListener {

                    finishAffinity()
                }
            }

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
