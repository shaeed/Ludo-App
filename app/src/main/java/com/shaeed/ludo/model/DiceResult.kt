package com.shaeed.ludo.model

import kotlinx.serialization.Serializable

@Serializable
data class DiceResult(
    val value: Int,
)
