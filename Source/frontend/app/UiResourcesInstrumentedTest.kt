package com.linhavital.app.ui

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.linhavital.app.R
import com.linhavital.app.data.model.ContatoEmergencia
import com.linhavital.app.databinding.ActivityHomeBinding
import com.linhavital.app.databinding.ViewBottomNavigationBinding
import com.linhavital.app.ui.common.NavigationTab
import com.linhavital.app.ui.common.selectTab
import com.linhavital.app.ui.home.ContatoAdapter
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Inflate views only. No Activity, account, phone call, scheduler or network is started. */
@RunWith(AndroidJUnit4::class)
class UiResourcesInstrumentedTest {
    private fun onUiThread(block: (ContextThemeWrapper) -> Unit) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            block(ContextThemeWrapper(instrumentation.targetContext, R.style.Theme_LinhaVital))
        }
    }

    @Test fun allScreensInflateInPortraitAndLandscapeWithLargeText() = onUiThread { base ->
        val screens = listOf(R.layout.activity_login, R.layout.activity_register,
            R.layout.activity_onboarding, R.layout.activity_home, R.layout.activity_contatos,
            R.layout.activity_criterios, R.layout.activity_contato_form, R.layout.activity_criterio_form)
        for (landscape in listOf(false, true)) {
            val configuration = Configuration(base.resources.configuration).apply {
                fontScale = 1.6f
                orientation = if (landscape) Configuration.ORIENTATION_LANDSCAPE
                    else Configuration.ORIENTATION_PORTRAIT
                screenWidthDp = if (landscape) 640 else 360
                screenHeightDp = if (landscape) 360 else 640
            }
            val context = ContextThemeWrapper(base.createConfigurationContext(configuration), R.style.Theme_LinhaVital)
            val inflater = LayoutInflater.from(context)
            for (screen in screens) {
                val view = inflater.inflate(screen, FrameLayout(context), false)
                val density = context.resources.displayMetrics.density
                view.measure(View.MeasureSpec.makeMeasureSpec((configuration.screenWidthDp * density).toInt(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec((configuration.screenHeightDp * density).toInt(), View.MeasureSpec.EXACTLY))
                view.layout(0, 0, view.measuredWidth, view.measuredHeight)
                assertTrue("Screen must have a measurable height: $screen", view.measuredHeight > 0)
            }
        }
    }

    @Test fun navigationHasExactlyOneSelectedDestination() = onUiThread { context ->
        val binding = ViewBottomNavigationBinding.inflate(LayoutInflater.from(context))
        val items = listOf(binding.btnNavCriterios, binding.btnNavHome, binding.btnNavContatos)
        for (tab in NavigationTab.values()) {
            binding.selectTab(tab)
            assertEquals(1, items.count { it.isSelected })
            assertTrue(items[tab.index].isSelected)
            assertEquals(tab.index, binding.navigationWave.selectedIndex)
        }
        assertEquals(0, binding.bottomNavigation.indexOfChild(binding.btnNavCriterios))
        assertEquals(1, binding.bottomNavigation.indexOfChild(binding.btnNavHome))
        assertEquals(2, binding.bottomNavigation.indexOfChild(binding.btnNavContatos))
    }

    @Test fun navigationWrapsItsContentsInsteadOfFillingTheScreen() = onUiThread { context ->
        val binding = ViewBottomNavigationBinding.inflate(LayoutInflater.from(context))
        val density = context.resources.displayMetrics.density
        binding.root.measure(View.MeasureSpec.makeMeasureSpec((360 * density).toInt(), View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec((640 * density).toInt(), View.MeasureSpec.AT_MOST))
        assertTrue(binding.root.measuredHeight < 240 * density)
        assertTrue(binding.root.measuredHeight >= 104 * density)
    }

    @Test fun emergencyActionAndFormFieldsRemainPresent() = onUiThread { context ->
        val binding = ActivityHomeBinding.inflate(LayoutInflater.from(context))
        assertNotNull(binding.btnSOS)
        assertNotNull(binding.btnCheckIn)
        assertNotNull(binding.btnVerContatos)
        assertNotNull(binding.btnConfigurarMonitoramento)
        if (context.resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            assertNull(binding.dashboardScroll.findViewById<View>(R.id.btnSOS))
        }
        val register = LayoutInflater.from(context).inflate(R.layout.activity_register, null)
        for (id in listOf(R.id.etName, R.id.etEmail, R.id.etPhone, R.id.etBirthDate, R.id.etPassword, R.id.etConfirmPassword)) {
            assertNotNull(register.findViewById<View>(id))
        }
    }

    @Test fun contactButtonsKeepTheirOriginalCallbacks() = onUiThread { context ->
        val contact = ContatoEmergencia(id = 4L, nome = "Contato de teste", telefone = "11999999999")
        var called: String? = null
        var edited: ContatoEmergencia? = null
        var deleted: Long? = null
        val adapter = ContatoAdapter(listOf(contact), { called = it }, { edited = it }, { deleted = it })
        val holder = adapter.onCreateViewHolder(FrameLayout(context), 0)
        adapter.onBindViewHolder(holder, 0)
        holder.binding.btnLigar.performClick()
        holder.binding.btnEditar.performClick()
        holder.binding.btnDeletar.performClick()
        assertEquals(contact.telefone, called)
        assertEquals(contact, edited)
        assertEquals(contact.id, deleted)
    }
}
