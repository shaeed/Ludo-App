package com.shaeed.ludo.model

import kotlinx.serialization.Serializable

@Serializable
data class Token(
    val id: Int,
    val color: PlayerColor,
    val cell: Cell
)
