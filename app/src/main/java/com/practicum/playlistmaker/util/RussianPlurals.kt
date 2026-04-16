package com.practicum.playlistmaker.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object RussianPlurals {

    /** Русские правила plural (few: 2–4 трека / минуты), не английские one/other. */
    fun pluralRussian(context: Context, pluralsId: Int, quantity: Int): String {
        val q = quantity.coerceAtLeast(0)
        val ruCfg = Configuration(context.resources.configuration)
        ruCfg.setLocale(Locale.forLanguageTag("ru"))
        val ruCtx = context.createConfigurationContext(ruCfg)
        return ruCtx.resources.getQuantityString(pluralsId, q, q)
    }
}
