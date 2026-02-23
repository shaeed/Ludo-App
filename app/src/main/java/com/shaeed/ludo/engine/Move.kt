package com.shaeed.ludo.engine

import com.shaeed.ludo.model.Cell
import com.shaeed.ludo.model.Token
import kotlinx.serialization.Serializable

@Serializable
data class Move(
    val token: Token,
    val destination: Cell,
    val captures: List<Token> = emptyList()
)
