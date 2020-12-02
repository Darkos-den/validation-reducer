package com.darkos.mvu.validation

import com.darkos.mvu.model.MVUState
import com.darkos.mvu.validation.model.ValidationState
import kotlin.test.Test

class ValidationReducerTest {

    data class TestState(
        val email: String,
        val emailValid: Boolean
    ): MVUState()

    @Test
    fun checkCallUpdate(){
        val reducer = ValidationReducer<TestState>(
            mapperFrom = {
                ValidationState(emptyMap())
            },
            mapperTo = { _, _ ->
                TestState("", false)
            },
            errorEffect = null
        )
        TODO()
    }
}