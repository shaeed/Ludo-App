package com.shaeed.ludo.data

import android.content.Context
import com.shaeed.ludo.model.GameConfig
import com.shaeed.ludo.model.GameState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

class GameRepository(context: Context) {

    private val savesDir = File(context.filesDir, "saved_games").also { it.mkdirs() }
    private val json = Json { prettyPrint = false }

    fun save(gameState: GameState, gameConfig: GameConfig, name: String): SavedGame {
        val savedGame = SavedGame(
            id = UUID.randomUUID().toString(),
            name = name,
            timestamp = System.currentTimeMillis(),
            gameState = gameState,
            gameConfig = gameConfig
        )
        val file = File(savesDir, "${savedGame.id}.json")
        file.writeText(json.encodeToString(savedGame))
        return savedGame
    }

    fun listSaves(): List<SavedGame> {
        return savesDir.listFiles { f -> f.extension == "json" }
            ?.mapNotNull { file ->
                try {
                    json.decodeFromString<SavedGame>(file.readText())
                } catch (_: Exception) {
                    null
                }
            }
            ?.sortedByDescending { it.timestamp }
            ?: emptyList()
    }

    fun load(id: String): SavedGame? {
        val file = File(savesDir, "$id.json")
        if (!file.exists()) return null
        return try {
            json.decodeFromString<SavedGame>(file.readText())
        } catch (_: Exception) {
            null
        }
    }

    fun update(id: String, gameState: GameState, gameConfig: GameConfig, name: String): SavedGame {
        val savedGame = SavedGame(
            id = id,
            name = name,
            timestamp = System.currentTimeMillis(),
            gameState = gameState,
            gameConfig = gameConfig
        )
        val file = File(savesDir, "$id.json")
        file.writeText(json.encodeToString(savedGame))
        return savedGame
    }

    fun delete(id: String) {
        File(savesDir, "$id.json").delete()
    }
}
