package com.backend.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class ChangeStateResponse (
    val changed: Boolean
)