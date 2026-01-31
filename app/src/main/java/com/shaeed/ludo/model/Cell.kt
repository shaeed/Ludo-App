package com.shaeed.ludo.model

sealed class Cell {
    data class Base(val color: PlayerColor) : Cell()
    data class Normal(val index: Int, val isSafe: Boolean = false) : Cell()
    data class HomeStretch(val color: PlayerColor, val index: Int) : Cell()
    data class Home(val color: PlayerColor) : Cell()
}
