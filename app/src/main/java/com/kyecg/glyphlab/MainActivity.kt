package com.kyecg.glyphlab

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.ComponentName
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.nothing.ketchum.Glyph
import com.nothing.ketchum.GlyphException
import com.nothing.ketchum.GlyphManager

class MainActivity : AppCompatActivity() {

    private var glyphManager: GlyphManager? = null
    private var registered = false
    private var sessionOpened = false

    private lateinit var root: LinearLayout
    private lateinit var status: TextView
    private lateinit var mainCard: MaterialCardView
    private lateinit var controlCard: MaterialCardView
    private lateinit var glyphMapCard: MaterialCardView
    private lateinit var glyphMap: GlyphMapView

    private val selectedZones = mutableSetOf<String>()

    private var lastPalette: AppPalette? = null
    private var lastStyle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildInterface()
        applyPalette(false)
        animateEntrance()
    }

    override fun onResume() {
        super.onResume()

        if (::root.isInitialized) {
            val currentStyle = AppColors.currentStyle(this)

            if (lastStyle != currentStyle) {
                rebuildInterface()
            } else {
                applyPalette(true)
            }
        }
    }

    private fun rebuildInterface() {
        val currentStatus = if (::status.isInitialized) {
            status.text.toString()
        } else {
            "Glyph SDK: не подключён"
        }

        buildInterface()
        status.text = currentStatus

        lastPalette = null
        applyPalette(false)
        animateEntrance()
    }

    private fun animateEntrance() {
        root.post {
            val views = (0 until root.childCount).map {
                root.getChildAt(it)
            }

            views.forEachIndexed { index, view ->
                view.animate().cancel()

                view.alpha = 0f
                view.translationY = dp(20).toFloat()

                view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(index * 85L)
                    .setDuration(420L)
                    .setInterpolator(DecelerateInterpolator(1.6f))
                    .start()
            }
        }
    }

    private fun buildInterface() {
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
        }

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(32))
        }

        scroll.addView(
            root,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(scroll)

        val header = makeText("GLYPH LAB", 13f, true).apply {
            letterSpacing = 0.18f
        }
        root.addView(header)

        val title = makeText("Освещение.", 34f, true).apply {
            setPadding(0, dp(8), 0, dp(4))
        }
        root.addView(title)

        val subtitle = makeText(
            "Управляй светом своего Nothing",
            14f
        )
        root.addView(subtitle)

        // Карточка подключения

        mainCard = createCard()

        val mainContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
        }

        mainContent.addView(
            makeText("Подключение Glyph", 20f, true)
        )

        status = makeText("Glyph SDK: не подключён", 14f).apply {
            setPadding(0, dp(10), 0, dp(16))
        }
        mainContent.addView(status)

        mainContent.addView(
            createButton("Подключить Glyph") {
                connectGlyph()
            },
            matchWidth(dp(52))
        )

        mainCard.addView(mainContent)

        root.addView(
            mainCard,
            matchWidth(0).apply {
                topMargin = dp(26)
            }
        )

        // Карточка управления

        controlCard = createCard()

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
        }

        controls.addView(
            makeText("Управление зонами", 20f, true)
        )

        controls.addView(
            makeText("Выбери нужную световую зону", 13f).apply {
                setPadding(0, dp(5), 0, dp(16))
            }
        )

        val zoneRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        zoneRow.addView(
            createButton("1") {
                updateGlyphMap("C")
                activateZone("C")
            },
            weightedButton()
        )

        zoneRow.addView(
            createButton("2") {
                updateGlyphMap("A")
                activateZone("A")
            },
            weightedButton()
        )

        zoneRow.addView(
            createButton("3") {
                updateGlyphMap("B")
                activateZone("B")
            },
            weightedButton()
        )

        controls.addView(zoneRow)

        controls.addView(
            createButton("Выключить Glyph") {
                clearGlyphMap()
                turnOffGlyph()
            },
            matchWidth(dp(52)).apply {
                topMargin = dp(14)
            }
        )

        controlCard.addView(controls)

        root.addView(
            controlCard,
            matchWidth(0).apply {
                topMargin = dp(16)
            }
        )

        // Визуальная карта Glyph

        glyphMapCard = createCard()

        val glyphMapContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
        }

        glyphMapContent.addView(
            makeText("Карта Glyph", 20f, true)
        )

        glyphMapContent.addView(
            makeText(
                "Расположение световых сегментов на задней панели",
                13f
            ).apply {
                setPadding(0, dp(5), 0, dp(10))
            }
        )

        glyphMap = GlyphMapView(this)

        glyphMapContent.addView(
            glyphMap,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(390)
            )
        )

        glyphMapContent.addView(
            makeText(
                "1 — C  •  2 — A  •  3 — B",
                12f
            ).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(8), 0, 0)
            }
        )

        glyphMapCard.addView(glyphMapContent)

        root.addView(
            glyphMapCard,
            matchWidth(0).apply {
                topMargin = dp(16)
            }
        )

        // Восстанавливаем выделение после перестройки интерфейса

        selectedZones.forEach { zone ->
            glyphMap.setZoneSelected(zone, true)
        }

        // Настройки

        root.addView(
            createButton("⚙  Настройки") {
                startActivity(
                    Intent(this, SettingsActivity::class.java)
                )
            },
            matchWidth(dp(54)).apply {
                topMargin = dp(20)
            }
        )
    }

    private fun updateGlyphMap(zone: String) {
        if (!selectedZones.add(zone)) {
            selectedZones.remove(zone)
        }

        glyphMap.setZoneSelected(
            zone,
            selectedZones.contains(zone)
        )
    }

    private fun clearGlyphMap() {
        selectedZones.clear()
        glyphMap.clearSelection()
    }

    private fun makeText(
        value: String,
        size: Float,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size

            if (bold) {
                setTypeface(null, Typeface.BOLD)
            }
        }
    }

    private fun createCard(): MaterialCardView {
        val style = AppColors.currentStyle(this)

        return MaterialCardView(this).apply {
            radius = when (style) {
                "nothing" -> dp(12).toFloat()
                "miuix" -> dp(16).toFloat()
                else -> dp(28).toFloat()
            }

            strokeWidth = if (style == "nothing") dp(1) else 0

            cardElevation = if (style == "miuix") {
                dp(1).toFloat()
            } else {
                0f
            }
        }
    }

    private fun createButton(
        label: String,
        action: () -> Unit
    ): MaterialButton {
        val style = AppColors.currentStyle(this)

        return MaterialButton(this).apply {
            text = label
            textSize = 15f
            isAllCaps = false
            setTypeface(null, Typeface.BOLD)
            insetTop = 0
            insetBottom = 0

            cornerRadius = when (style) {
                "nothing" -> dp(10)
                "miuix" -> dp(12)
                else -> dp(22)
            }

            setOnClickListener {
                animate()
                    .scaleX(0.96f)
                    .scaleY(0.96f)
                    .setDuration(75L)
                    .withEndAction {
                        animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(130L)
                            .setInterpolator(
                                DecelerateInterpolator()
                            )
                            .start()
                    }
                    .start()

                action()
            }
        }
    }

    private fun applyPalette(animate: Boolean) {
        val palette = AppColors.getPalette(this)
        val old = lastPalette

        fun transition(
            from: Int,
            to: Int,
            update: (Int) -> Unit
        ) {
            if (!animate || old == null) {
                update(to)
                return
            }

            ValueAnimator.ofObject(
                ArgbEvaluator(),
                from,
                to
            ).apply {
                duration = 350L
                addUpdateListener {
                    update(it.animatedValue as Int)
                }
                start()
            }
        }

        transition(
            old?.background ?: palette.background,
            palette.background
        ) {
            root.setBackgroundColor(it)
            window.statusBarColor = it
            window.navigationBarColor = it
        }

        transition(
            old?.surface ?: palette.surface,
            palette.surface
        ) {
            mainCard.setCardBackgroundColor(it)
            controlCard.setCardBackgroundColor(it)
            glyphMapCard.setCardBackgroundColor(it)
        }

        val oldText = old?.text ?: palette.text
        val oldSecondary = old?.secondaryText ?: palette.secondaryText

        updateTextColors(
            root,
            palette,
            oldText,
            oldSecondary,
            animate
        )

        updateButtons(
            root,
            palette,
            old?.accent ?: palette.accent,
            animate
        )

        val dark = AppColors.isDark(this)

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }

        lastPalette = palette
        lastStyle = AppColors.currentStyle(this)
    }

    private fun updateTextColors(
        view: View,
        palette: AppPalette,
        oldText: Int,
        oldSecondary: Int,
        animate: Boolean
    ) {
        if (view is MaterialButton) {
            view.setTextColor(palette.onAccent)
            return
        }

        if (view is TextView) {
            val target = if (view == status) {
                palette.secondaryText
            } else {
                palette.text
            }

            val previous = if (view == status) {
                oldSecondary
            } else {
                oldText
            }

            if (animate) {
                ValueAnimator.ofObject(
                    ArgbEvaluator(),
                    previous,
                    target
                ).apply {
                    duration = 350L
                    addUpdateListener {
                        view.setTextColor(
                            it.animatedValue as Int
                        )
                    }
                    start()
                }
            } else {
                view.setTextColor(target)
            }
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                updateTextColors(
                    view.getChildAt(i),
                    palette,
                    oldText,
                    oldSecondary,
                    animate
                )
            }
        }
    }

    private fun updateButtons(
        view: View,
        palette: AppPalette,
        oldAccent: Int,
        animate: Boolean
    ) {
        if (view is MaterialButton) {
            if (animate) {
                ValueAnimator.ofObject(
                    ArgbEvaluator(),
                    oldAccent,
                    palette.accent
                ).apply {
                    duration = 350L
                    addUpdateListener {
                        view.backgroundTintList =
                            ColorStateList.valueOf(
                                it.animatedValue as Int
                            )
                    }
                    start()
                }
            } else {
                view.backgroundTintList =
                    ColorStateList.valueOf(palette.accent)
            }

            view.setTextColor(palette.onAccent)
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                updateButtons(
                    view.getChildAt(i),
                    palette,
                    oldAccent,
                    animate
                )
            }
        }
    }

    private fun connectGlyph() {
        if (glyphManager != null) return

        status.text = "Glyph SDK: подключение..."

        val manager = GlyphManager.getInstance(applicationContext)
        glyphManager = manager

        manager.init(object : GlyphManager.Callback {

            override fun onServiceConnected(componentName: ComponentName) {
                try {
                    registered = manager.register(Glyph.DEVICE_24111)

                    if (registered) {
                        manager.openSession()
                        sessionOpened = true

                        runOnUiThread {
                            status.text = "Glyph SDK: подключён"
                        }
                    } else {
                        runOnUiThread {
                            status.text = "Ошибка регистрации Glyph"
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GlyphLab", "Connection error", e)

                    runOnUiThread {
                        status.text = "Ошибка подключения"
                    }
                }
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
                registered = false
                sessionOpened = false

                runOnUiThread {
                    status.text = "Соединение потеряно"
                }
            }
        })
    }

    private fun activateZone(zone: String) {
        val manager = glyphManager

        if (manager == null || !registered) {
            status.text = "Сначала подключи Glyph"
            return
        }

        try {
            val builder = manager.getGlyphFrameBuilder()

            val frame = when (zone) {
                "A" -> builder.buildChannelA().build()
                "B" -> builder.buildChannelB().build()
                "C" -> builder.buildChannelC().build()
                else -> return
            }

            manager.toggle(frame)
            status.text = "Команда отправлена"

        } catch (e: Exception) {
            Log.e("GlyphLab", "Zone error", e)
            status.text = "Ошибка управления зоной"
        }
    }

    private fun turnOffGlyph() {
        val manager = glyphManager

        if (manager == null || !registered) {
            status.text = "Glyph не подключён"
            return
        }

        try {
            manager.turnOff()
            status.text = "Glyph выключен"

        } catch (e: Exception) {
            Log.e("GlyphLab", "Turn off error", e)
            status.text = "Ошибка выключения Glyph"
        }
    }

    private fun matchWidth(height: Int): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            if (height == 0) {
                LinearLayout.LayoutParams.WRAP_CONTENT
            } else {
                height
            }
        )
    }

    private fun weightedButton(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            0,
            dp(58),
            1f
        ).apply {
            marginEnd = dp(6)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        glyphManager?.let { manager ->
            if (sessionOpened) {
                try {
                    manager.closeSession()
                } catch (e: GlyphException) {
                    Log.e("GlyphLab", "Close session error", e)
                }
            }

            manager.unInit()
        }

        glyphManager = null
        super.onDestroy()
    }
}
