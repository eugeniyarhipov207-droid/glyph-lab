package com.kyecg.glyphlab

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat

data class AppPalette(
    val background: Int,
    val surface: Int,
    val surfaceSecondary: Int,
    val text: Int,
    val secondaryText: Int,
    val accent: Int,
    val onAccent: Int,
    val divider: Int
)

object AppColors {

    private const val PREFS_NAME = "glyph_settings"

    fun currentStyle(context: Context): String {
        return context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).getString("interface_style", "expressive") ?: "expressive"
    }

    private fun isDarkSystem(context: Context): Boolean {
        val nightMode = context.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK

        return nightMode ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    fun isDark(context: Context): Boolean {
        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        return when (currentStyle(context)) {
            "nothing" -> true

            "miuix" ->
                prefs.getString("theme_miuix", "dark") == "dark"

            else -> when (prefs.getString("theme_m3", "dark")) {
                "dark" -> true
                "light" -> false
                "system" -> isDarkSystem(context)
                else -> isDarkSystem(context)
            }
        }
    }

    fun getPalette(context: Context): AppPalette {
        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        return when (currentStyle(context)) {

            "nothing" -> AppPalette(
                background = Color.BLACK,
                surface = Color.rgb(18, 18, 18),
                surfaceSecondary = Color.rgb(32, 32, 32),
                text = Color.WHITE,
                secondaryText = Color.LTGRAY,
                accent = Color.rgb(230, 35, 45),
                onAccent = Color.WHITE,
                divider = Color.rgb(65, 65, 65)
            )

            "miuix" -> {
                val dark = isDark(context)

                if (dark) {
                    AppPalette(
                        background = Color.rgb(17, 22, 32),
                        surface = Color.rgb(25, 33, 48),
                        surfaceSecondary = Color.rgb(35, 47, 67),
                        text = Color.rgb(243, 247, 255),
                        secondaryText = Color.rgb(166, 183, 211),
                        accent = Color.rgb(0, 70, 255),
                        onAccent = Color.WHITE,
                        divider = Color.rgb(51, 68, 94)
                    )
                } else {
                    AppPalette(
                        background = Color.rgb(242, 246, 253),
                        surface = Color.WHITE,
                        surfaceSecondary = Color.rgb(225, 235, 250),
                        text = Color.rgb(25, 39, 61),
                        secondaryText = Color.rgb(91, 111, 143),
                        accent = Color.rgb(0, 70, 255),
                        onAccent = Color.WHITE,
                        divider = Color.rgb(202, 217, 240)
                    )
                }
            }

            else -> {
                val dark = isDark(context)
                val useMonet = prefs.getBoolean("material_you", false)

                val accent = if (useMonet) {
                    getSystemColor(
                        context,
                        if (dark) {
                            "system_accent1_200"
                        } else {
                            "system_accent1_500"
                        },
                        Color.rgb(140, 170, 255)
                    )
                } else {
                    when (prefs.getInt(
                        "accent_color",
                        Color.rgb(229, 57, 53)
                    )) {
                        Color.rgb(229, 57, 53) ->
                            Color.rgb(229, 57, 53)

                        Color.rgb(255, 152, 0) ->
                            Color.rgb(255, 152, 0)

                        Color.rgb(255, 213, 79) ->
                            Color.rgb(255, 213, 79)

                        Color.rgb(76, 175, 80) ->
                            Color.rgb(76, 175, 80)

                        Color.rgb(0, 188, 212) ->
                            Color.rgb(0, 188, 212)

                        Color.rgb(33, 150, 243) ->
                            Color.rgb(33, 150, 243)

                        Color.rgb(103, 58, 183) ->
                            Color.rgb(103, 58, 183)

                        Color.rgb(233, 30, 99) ->
                            Color.rgb(233, 30, 99)

                        else -> Color.rgb(170, 120, 255)
                    }
                }

                AppPalette(
                    background = if (dark) {
                        Color.rgb(18, 18, 22)
                    } else {
                        Color.rgb(248, 247, 255)
                    },
                    surface = if (dark) {
                        Color.rgb(29, 29, 35)
                    } else {
                        Color.WHITE
                    },
                    surfaceSecondary = if (dark) {
                        Color.rgb(42, 42, 50)
                    } else {
                        Color.rgb(238, 236, 248)
                    },
                    text = if (dark) {
                        Color.WHITE
                    } else {
                        Color.rgb(30, 28, 38)
                    },
                    secondaryText = if (dark) {
                        Color.LTGRAY
                    } else {
                        Color.rgb(100, 96, 112)
                    },
                    accent = accent,
                    onAccent = Color.WHITE,
                    divider = if (dark) {
                        Color.rgb(65, 65, 75)
                    } else {
                        Color.rgb(220, 217, 232)
                    }
                )
            }
        }
    }

    private fun getSystemColor(
        context: Context,
        name: String,
        fallback: Int
    ): Int {
        return try {
            val id = context.resources.getIdentifier(
                name,
                "color",
                "android"
            )

            if (id != 0) {
                ContextCompat.getColor(context, id)
            } else {
                fallback
            }
        } catch (_: Exception) {
            fallback
        }
    }

    fun animateColor(
        view: View,
        from: Int,
        to: Int,
        duration: Long = 300,
        update: (Int) -> Unit
    ) {
        ValueAnimator.ofObject(
            ArgbEvaluator(),
            from,
            to
        ).apply {
            this.duration = duration

            addUpdateListener {
                update(it.animatedValue as Int)
            }

            start()
        }
    }

    fun animateBackground(
        view: View,
        color: Int,
        duration: Long = 300
    ) {
        val from = (view.background as? ColorDrawable)?.color ?: color

        animateColor(view, from, color, duration) {
            view.setBackgroundColor(it)
        }
    }

    fun animateText(
        textView: TextView,
        color: Int,
        duration: Long = 300
    ) {
        val from = textView.currentTextColor

        animateColor(view = textView, from = from, to = color, duration = duration) {
            textView.setTextColor(it)
        }
    }
}
