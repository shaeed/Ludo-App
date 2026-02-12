package com.shaeed.ludo.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class TokenStyle(val displayName: String) {
    CLASSIC_CONE("Classic"),
    FLAT_DISC("Flat Disc"),
    STAR("Star"),
    RING("Ring"),
    PAWN("Pawn")
}

object TokenStyleHolder {
    var current by mutableStateOf(TokenStyle.CLASSIC_CONE)
}
