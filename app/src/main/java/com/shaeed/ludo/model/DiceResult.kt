package com.shaeed.ludo.model

import java.util.UUID

data class DiceResult(
    val value: Int,
    val rollId: String = UUID.randomUUID().toString()
)
