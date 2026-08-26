package com.linhavital.app.ui.home

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.linhavital.app.databinding.ActivityCriteriosBinding
import com.linhavital.app.utils.applySystemBarsPadding

class CriteriosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCriteriosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCriteriosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding.headerCriterios.applySystemBarsPadding(top = true)
        binding.bottomNavigationContainer.applySystemBarsPadding(bottom = true)

        window.statusBarColor = android.graphics.Color.parseColor("#FFF5F5")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        binding.btnAdicionarCriterio.setOnClickListener {
            Toast.makeText(
                this,
                "A configuração de novos critérios será disponibilizada na próxima etapa.",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.btnNavCriterios.setBackgroundResource(com.linhavital.app.R.drawable.nav_item_active)
        binding.iconNavCriterios.setColorFilter(android.graphics.Color.parseColor("#BB0013"))
        binding.labelNavCriterios.setTextColor(android.graphics.Color.parseColor("#BB0013"))

        binding.btnNavHome.setOnClickListener {
            finish()
        }
        binding.btnNavContatos.setOnClickListener {
            startActivity(android.content.Intent(this, ContatosActivity::class.java))
            finish()
        }
        binding.btnNavCriterios.setOnClickListener { }
    }
}
