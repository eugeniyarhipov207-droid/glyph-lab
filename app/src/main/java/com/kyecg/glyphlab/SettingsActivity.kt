
package com.kyecg.glyphlab

import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.button.MaterialButton
import rikka.shizuku.Shizuku

class SettingsActivity : AppCompatActivity() {

    private val prefs by lazy {
        getSharedPreferences("glyph_settings", MODE_PRIVATE)
    }

    private lateinit var root: LinearLayout
    private lateinit var styleContainer: LinearLayout
    private lateinit var themeContainer: LinearLayout
    private lateinit var colorContainer: LinearLayout
    private lateinit var palette: AppPalette

    private var glyphStateSwitch: SwitchCompat? = null

    private var selectedStyle = "expressive"
    private var selectedColor = Color.rgb(229, 57, 53)

    private val REQUEST_GLYPH_PERMISSION = 2001

    private val permissionListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->

            if (requestCode != REQUEST_GLYPH_PERMISSION) {
                return@OnRequestPermissionResultListener
            }

            val granted =
                grantResult == PackageManager.PERMISSION_GRANTED

            prefs.edit()
                .putBoolean("show_glyph_state", granted)
                .apply()

            glyphStateSwitch?.isChecked = granted

            Toast.makeText(
                this,
                if (granted) {
                    "Разрешение Shizuku получено"
                } else {
                    "Разрешение Shizuku не получено"
                },
                Toast.LENGTH_SHORT
            ).show()
        }

    private val presets = listOf(
        "Красный" to Color.rgb(229, 57, 53),
        "Оранжевый" to Color.rgb(255, 152, 0),
        "Жёлтый" to Color.rgb(255, 213, 79),
        "Зелёный" to Color.rgb(76, 175, 80),
        "Бирюзовый" to Color.rgb(0, 188, 212),
        "Синий" to Color.rgb(33, 150, 243),
        "Фиолетовый" to Color.rgb(103, 58, 183),
        "Розовый" to Color.rgb(233, 30, 99)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Shizuku.addRequestPermissionResultListener(permissionListener)

        selectedStyle = AppColors.currentStyle(this)
        selectedColor = prefs.getInt(
            "accent_color",
            Color.rgb(229, 57, 53)
        )

        renderScreen()
    }

    override fun onResume() {
        super.onResume()

        if (::root.isInitialized) {
            val newStyle = AppColors.currentStyle(this)

            if (newStyle != selectedStyle) {
                selectedStyle = newStyle
                renderScreen()
            }
        }
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        super.onDestroy()
    }

    private fun renderScreen() {
        selectedStyle = AppColors.currentStyle(this)

        selectedColor = prefs.getInt(
            "accent_color",
            Color.rgb(229, 57, 53)
        )

        palette = AppColors.getPalette(this)

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            setBackgroundColor(palette.background)
        }

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(32))
            setBackgroundColor(palette.background)
        }

        scroll.addView(
            root,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(scroll)

        window.statusBarColor = palette.background
        window.navigationBarColor = palette.background

        val dark = AppColors.isDark(this)

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }

        addText(root, "GLYPH LAB", 13f, palette.accent, true)

        addText(
            root,
            "Настройки",
            32f,
            palette.text,
            true
        ).setPadding(0, dp(8), 0, dp(4))

        addText(
            root,
            "Персонализируй приложение",
            14f,
            palette.secondaryText
        )

        addSection(root, "Стиль интерфейса")

        addText(
            root,
            "Выбери внешний вид приложения",
            13f,
            palette.secondaryText
        )

        val styleScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            clipToPadding = false
            setPadding(0, dp(10), 0, dp(8))
        }

        styleContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        styleScroll.addView(styleContainer)
        root.addView(styleScroll)

        addSection(root, "Тема оформления")

        addText(
            root,
            "Выбери светлое или тёмное оформление",
            13f,
            palette.secondaryText
        )

        val themeScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            clipToPadding = false
            setPadding(0, dp(10), 0, dp(8))
        }

        themeContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        themeScroll.addView(themeContainer)
        root.addView(themeScroll)

        addSection(root, "Цветовая схема")

        colorContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        root.addView(colorContainer)

        addSection(root, "Состояние глифов")
        buildGlyphStateOption()

        val backButton = createButton("Назад") {
            finish()
        }

        val backParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(54)
        ).apply {
            topMargin = dp(28)
        }

        root.addView(backButton, backParams)

        buildStyleCards()
        buildThemeOptions()
        buildColorOptions()

        animateEntrance()
    }

    // Переключатель состояния глифов

    private fun buildGlyphStateOption() {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            setPadding(
                dp(16),
                dp(14),
                dp(12),
                dp(14)
            )

            background = createBorder(
                palette.surface,
                palette.divider,
                1,
                cardRadius()
            )
        }

        val textContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val title = TextView(this).apply {
            text = "Отображение текущего состояния глифов"
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(palette.text)
        }

        val subtitle = TextView(this).apply {
            text = "отображает актуальную подсветку через Shizuku на главном экране"
            textSize = 11f
            setTextColor(palette.secondaryText)
            setPadding(0, dp(5), 0, 0)
        }

        textContainer.addView(title)
        textContainer.addView(subtitle)

        val switch = SwitchCompat(this).apply {
            isChecked = prefs.getBoolean("show_glyph_state", false)

            thumbTintList = ColorStateList.valueOf(palette.accent)

            trackTintList = ColorStateList.valueOf(
                if (AppColors.isDark(this@SettingsActivity)) {
                    Color.rgb(90, 90, 90)
                } else {
                    Color.rgb(190, 190, 190)
                }
            )
        }

        glyphStateSwitch = switch

        card.addView(
            textContainer,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        card.addView(
            switch,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = dp(8)
            }
        )

        switch.setOnCheckedChangeListener { _, checked ->

            if (!checked) {
                prefs.edit()
                    .putBoolean("show_glyph_state", false)
                    .apply()

                return@setOnCheckedChangeListener
            }

            if (!Shizuku.pingBinder()) {
                switch.isChecked = false

                Toast.makeText(
                    this,
                    "Сначала запусти Shizuku",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnCheckedChangeListener
            }

            try {
                if (
                    Shizuku.checkSelfPermission() ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    prefs.edit()
                        .putBoolean("show_glyph_state", true)
                        .apply()

                    Toast.makeText(
                        this,
                        "Разрешение Shizuku уже получено",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Shizuku.requestPermission(
                        REQUEST_GLYPH_PERMISSION
                    )
                }
            } catch (e: Exception) {
                switch.isChecked = false

                Toast.makeText(
                    this,
                    "Не удалось запросить разрешение Shizuku",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(10)
        }

        root.addView(card, params)
    }

    // Анимации

    private fun animateEntrance() {
        root.post {
            animateChildren(root)
        }
    }

    private fun animateChildren(parent: ViewGroup) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)

            child.animate().cancel()
            child.alpha = 0f
            child.translationY = dp(18).toFloat()

            child.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(i * 45L)
                .setDuration(380L)
                .setInterpolator(
                    DecelerateInterpolator(1.5f)
                )
                .start()

            if (
                child is ViewGroup &&
                child !is ScrollView &&
                child !is HorizontalScrollView
            ) {
                child.post {
                    animateChildren(child)
                }
            }
        }
    }

    private fun animatePress(view: View) {
        view.animate().cancel()

        view.scaleX = 1f
        view.scaleY = 1f

        view.animate()
            .scaleX(0.96f)
            .scaleY(0.96f)
            .setDuration(75L)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150L)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
            .start()
    }

    private fun animateSelection(view: View) {
        view.animate().cancel()

        view.scaleX = 0.96f
        view.scaleY = 0.96f

        view.animate()
            .scaleX(1.025f)
            .scaleY(1.025f)
            .setDuration(130L)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(160L)
                    .start()
            }
            .start()
    }

    // Стили интерфейса

    private fun buildStyleCards() {
        styleContainer.removeAllViews()

        val styles = listOf(
            Triple("Nothing", "nothing", "Чёрный, белый и красный"),
            Triple("M3 Expressive", "expressive", "Динамические цвета и формы"),
            Triple("MIUIX", "miuix", "Стиль HyperOS")
        )

        for ((name, value, description) in styles) {
            val selected = selectedStyle == value
            val card = createCard(selected)

            card.addView(
                createMiniPreview(value),
                LinearLayout.LayoutParams(dp(150), dp(100))
            )

            val title = TextView(this).apply {
                text = name
                textSize = 15f
                setTypeface(null, Typeface.BOLD)
                setTextColor(palette.text)
                setPadding(0, dp(10), 0, dp(3))
            }

            card.addView(title)

            val desc = TextView(this).apply {
                text = description
                textSize = 10f
                setTextColor(palette.secondaryText)
            }

            card.addView(desc)

            card.setOnClickListener {
                if (selectedStyle == value) {
                    return@setOnClickListener
                }

                animateSelection(card)

                prefs.edit()
                    .putString("interface_style", value)
                    .apply()

                selectedStyle = value
                renderScreen()
            }

            val params = LinearLayout.LayoutParams(
                dp(174),
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = dp(12)
            }

            styleContainer.addView(card, params)
        }
    }

    // Темы оформления

    private fun buildThemeOptions() {
        themeContainer.removeAllViews()

        if (selectedStyle == "nothing") {
            val info = TextView(this).apply {
                text = "В стиле Nothing используется только тёмная тема."
                textSize = 13f
                setTextColor(palette.secondaryText)
                setPadding(0, dp(8), 0, dp(8))
            }

            themeContainer.addView(info)
            return
        }

        val key = if (selectedStyle == "miuix") {
            "theme_miuix"
        } else {
            "theme_m3"
        }

        val currentTheme = prefs.getString(key, "dark") ?: "dark"

        val themes = listOf(
            "Тёмная" to "dark",
            "Светлая" to "light"
        )

        for ((name, value) in themes) {
            val dark = value == "dark"
            val selected = currentTheme == value
            val card = createCard(selected)

            val previewBackground = when {
                dark && selectedStyle == "miuix" ->
                    Color.rgb(17, 22, 32)

                dark ->
                    Color.rgb(31, 29, 36)

                selectedStyle == "miuix" ->
                    Color.rgb(242, 246, 253)

                else ->
                    Color.rgb(250, 248, 255)
            }

            val previewText = if (dark) {
                Color.WHITE
            } else {
                Color.rgb(35, 40, 48)
            }

            val preview = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(10), dp(10), dp(10), dp(10))

                background = rounded(
                    previewBackground,
                    if (selectedStyle == "miuix") 9 else 17
                )
            }

            val sampleTitle = TextView(this).apply {
                text = "Aa"
                textSize = 25f
                setTypeface(null, Typeface.BOLD)
                setTextColor(previewText)
                gravity = Gravity.CENTER
            }

            preview.addView(sampleTitle)

            val sampleLine = View(this).apply {
                background = rounded(palette.accent, 5)
            }

            preview.addView(
                sampleLine,
                LinearLayout.LayoutParams(dp(65), dp(5)).apply {
                    topMargin = dp(7)
                }
            )

            card.addView(
                preview,
                LinearLayout.LayoutParams(dp(125), dp(75))
            )

            val label = TextView(this).apply {
                text = name
                textSize = 14f
                gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
                setTextColor(palette.text)
                setPadding(0, dp(10), 0, dp(2))
            }

            card.addView(label)

            val statusText = TextView(this).apply {
                text = if (selected) "Выбрана" else "Нажми для выбора"
                textSize = 10f
                gravity = Gravity.CENTER

                setTextColor(
                    if (selected) palette.accent
                    else palette.secondaryText
                )
            }

            card.addView(statusText)

            card.setOnClickListener {
                if (currentTheme == value) {
                    return@setOnClickListener
                }

                animateSelection(card)

                prefs.edit()
                    .putString(key, value)
                    .apply()

                renderScreen()
            }

            val params = LinearLayout.LayoutParams(
                dp(149),
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = dp(12)
            }

            themeContainer.addView(card, params)
        }
    }

    // Цветовая схема

    private fun buildColorOptions() {
        colorContainer.removeAllViews()

        if (selectedStyle != "expressive") {
            val info = TextView(this).apply {
                text = when (selectedStyle) {
                    "miuix" ->
                        "В MIUIX используется фирменный синий цвет."

                    else ->
                        "В стиле Nothing используется фирменный красный цвет."
                }

                textSize = 13f
                setTextColor(palette.secondaryText)
                setPadding(0, dp(4), 0, dp(8))
            }

            colorContainer.addView(info)
            return
        }

        addText(
            colorContainer,
            "Готовые цвета",
            15f,
            palette.text,
            true
        )

        val horizontal = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(12), 0, dp(8))
        }

        for ((name, color) in presets) {
            val selected = selectedColor == color

            val button = TextView(this).apply {
                text = if (selected) "✓ $name" else name
                textSize = 12f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                setPadding(dp(15), dp(10), dp(15), dp(10))

                background = GradientDrawable().apply {
                    setColor(color)
                    cornerRadius = dp(20).toFloat()

                    if (selected) {
                        setStroke(dp(2), palette.text)
                    }
                }

                setOnClickListener {
                    if (selectedColor == color) {
                        return@setOnClickListener
                    }

                    animatePress(this)

                    prefs.edit()
                        .putInt("accent_color", color)
                        .apply()

                    selectedColor = color
                    renderScreen()
                }
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = dp(8)
            }

            row.addView(button, params)
        }

        horizontal.addView(row)
        colorContainer.addView(horizontal)
    }

    // Миниатюрный предпросмотр стиля

    private fun createMiniPreview(style: String): View {
        val colors = when (style) {
            "nothing" -> Triple(
                Color.BLACK,
                Color.WHITE,
                Color.rgb(229, 57, 53)
            )

            "miuix" -> Triple(
                Color.rgb(17, 22, 32),
                Color.WHITE,
                Color.rgb(65, 140, 255)
            )

            else -> Triple(
                Color.rgb(31, 29, 36),
                Color.WHITE,
                palette.accent
            )
        }

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))

            background = rounded(
                colors.first,
                if (style == "nothing") 5 else 15
            )

            val heading = TextView(this@SettingsActivity).apply {
                text = when (style) {
                    "nothing" -> "NOTHING"
                    "miuix" -> "HyperOS"
                    else -> "Glyph Lab"
                }

                textSize = 12f
                setTypeface(null, Typeface.BOLD)
                setTextColor(colors.second)
            }

            addView(heading)

            val line = View(this@SettingsActivity).apply {
                background = rounded(colors.third, 5)
            }

            addView(
                line,
                LinearLayout.LayoutParams(dp(75), dp(5)).apply {
                    topMargin = dp(9)
                }
            )

            val smallText = TextView(this@SettingsActivity).apply {
                text = "Настройки интерфейса"
                textSize = 8f
                setTextColor(colors.second)
                alpha = 0.7f
            }

            addView(smallText)
        }
    }

    // Карточки

    private fun createCard(selected: Boolean): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL

            setPadding(
                dp(12),
                dp(12),
                dp(12),
                dp(12)
            )

            background = createBorder(
                palette.surface,
                if (selected) palette.accent else palette.divider,
                if (selected) 2 else 1,
                cardRadius()
            )

            isClickable = true
            isFocusable = true
        }
    }

    // Заголовки разделов

    private fun addSection(
        parent: LinearLayout,
        text: String
    ) {
        val view = TextView(this).apply {
            this.text = text
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(palette.text)

            setPadding(
                0,
                dp(24),
                0,
                dp(8)
            )
        }

        parent.addView(
            view,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    // Текстовые элементы

    private fun addText(
        parent: LinearLayout,
        text: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        val view = TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)

            if (bold) {
                setTypeface(null, Typeface.BOLD)
            }
        }

        parent.addView(
            view,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        return view
    }

    // Кнопки

    private fun createButton(
        text: String,
        action: () -> Unit
    ): MaterialButton {
        val button = MaterialButton(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)

            cornerRadius = dp(buttonRadius())

            backgroundTintList =
                ColorStateList.valueOf(palette.accent)

            rippleColor =
                ColorStateList.valueOf(
                    Color.argb(45, 255, 255, 255)
                )

            insetTop = 0
            insetBottom = 0

            setOnClickListener {
                animatePress(this)
                action()
            }
        }

        return button
    }

    // Радиусы

    private fun cardRadius(): Int {
        return when (selectedStyle) {
            "miuix" -> 12
            "nothing" -> 5
            else -> 22
        }
    }

    private fun buttonRadius(): Int {
        return when (selectedStyle) {
            "miuix" -> 10
            "nothing" -> 5
            else -> 25
        }
    }

    // Фоны

    private fun rounded(
        color: Int,
        radius: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun createBorder(
        backgroundColor: Int,
        strokeColor: Int,
        strokeWidth: Int,
        radius: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(backgroundColor)
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(strokeWidth), strokeColor)
        }
    }

    // Перевод dp

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
