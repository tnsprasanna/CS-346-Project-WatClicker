package com.backend.data.responses

import kotlinx.serialization.Serializable

@Serializable
class GetQuizQuestionIdsResponse (
    val questionIds: Array<String>
)