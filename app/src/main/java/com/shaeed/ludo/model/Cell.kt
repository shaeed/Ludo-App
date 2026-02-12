package com.shaeed.ludo.model

import kotlinx.serialization.Serializable

@Serializable
sealed class Cell {
    @Serializable
    data class Base(val color: PlayerColor) : Cell()
    @Serializable
    data class Normal(val index: Int, val isSafe: Boolean = false) : Cell()
    @Serializable
    data class HomeStretch(val color: PlayerColor, val index: Int) : Cell()
    @Serializable
    data class Home(val color: PlayerColor) : Cell()
}
