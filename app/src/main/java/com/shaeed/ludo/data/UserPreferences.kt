package com.shaeed.ludo.data

import android.content.Context
import android.content.SharedPreferences
import com.shaeed.ludo.model.TokenStyle

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ludo_prefs", Context.MODE_PRIVATE)

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var shakeToRollEnabled: Boolean
        get() = prefs.getBoolean(KEY_SHAKE, true)
        set(value) = prefs.edit().putBoolean(KEY_SHAKE, value).apply()

    var tokenStyle: TokenStyle
        get() {
            val name = prefs.getString(KEY_TOKEN_STYLE, TokenStyle.CLASSIC_CONE.name)
            return try {
                TokenStyle.valueOf(name!!)
            } catch (_: Exception) {
                TokenStyle.CLASSIC_CONE
            }
        }
        set(value) = prefs.edit().putString(KEY_TOKEN_STYLE, value.name).apply()

    var enterOnSixOnly: Boolean
        get() = prefs.getBoolean(KEY_ENTER_ON_SIX, true)
        set(value) = prefs.edit().putBoolean(KEY_ENTER_ON_SIX, value).apply()

    var safeZonesEnabled: Boolean
        get() = prefs.getBoolean(KEY_SAFE_ZONES, true)
        set(value) = prefs.edit().putBoolean(KEY_SAFE_ZONES, value).apply()

    var maxConsecutiveSixes: Int
        get() = prefs.getInt(KEY_MAX_SIXES, 3)
        set(value) = prefs.edit().putInt(KEY_MAX_SIXES, value).apply()

    var passDiceToNextPlayer: Boolean
        get() = prefs.getBoolean(KEY_PASS_DICE, false)
        set(value) = prefs.edit().putBoolean(KEY_PASS_DICE, value).apply()

    var activePreset: String?
        get() = prefs.getString(KEY_ACTIVE_PRESET, "classic")
        set(value) = prefs.edit().putString(KEY_ACTIVE_PRESET, value).apply()

    fun applyClassicPreset() {
        enterOnSixOnly = true
        safeZonesEnabled = true
        maxConsecutiveSixes = 3
        passDiceToNextPlayer = false
        activePreset = "classic"
    }

    fun applyCasualPreset() {
        enterOnSixOnly = false
        safeZonesEnabled = false
        maxConsecutiveSixes = 3
        passDiceToNextPlayer = false
        activePreset = "casual"
    }

    companion object {
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_SHAKE = "shake_to_roll"
        private const val KEY_TOKEN_STYLE = "token_style"
        private const val KEY_ENTER_ON_SIX = "enter_on_six_only"
        private const val KEY_SAFE_ZONES = "safe_zones_enabled"
        private const val KEY_MAX_SIXES = "max_consecutive_sixes"
        private const val KEY_PASS_DICE = "pass_dice_to_next"
        private const val KEY_ACTIVE_PRESET = "active_preset"
    }
}
