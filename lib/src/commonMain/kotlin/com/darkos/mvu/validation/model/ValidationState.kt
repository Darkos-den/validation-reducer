package com.darkos.mvu.validation.model

import com.darkos.mvu.model.MVUState

data class ValidationState(
    val fields: List<Field>
) : MVUState()