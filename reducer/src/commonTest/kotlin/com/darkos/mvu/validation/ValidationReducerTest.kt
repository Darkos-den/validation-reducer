package com.darkos.mvu.validation

import com.darkos.mvu.model.MVUState
import com.darkos.mvu.model.None
import com.darkos.mvu.validation.model.Field
import com.darkos.mvu.validation.model.FieldValidationStatus
import com.darkos.mvu.validation.model.ValidationFieldType
import com.darkos.mvu.validation.model.ValidationState
import com.darkos.mvu.validation.model.mvu.ValidationEffect
import com.darkos.mvu.validation.model.mvu.ValidationMessage
import kotlin.test.Test
import kotlin.test.assertEquals

//black-box testing
//todo: add tests
class ValidationReducerTest {

    data class TestState(
        val email: String,
        val emailValid: Boolean
    ): MVUState()

    @Test
    fun checkTrigger(){
        val emailId: Long = 1
        val reducer = ValidationReducer<TestState>(
            mapperFrom = {
                ValidationState(emptyMap())
            },
            mapperTo = { _, _ ->
                TestState("", false)
            },
            errorEffect = null
        )
        val validationState = ValidationState(
            fields = mapOf(
                emailId to Field(
                    id = emailId,
                    type = ValidationFieldType.Email,
                    value = "email"
                )
            )
        )

        val result = reducer.update(validationState, ValidationMessage.Triggered)

        assertEquals(ValidationEffect.Validate::class, result.effect::class)
        assertEquals(validationState, result.state)
        assertEquals(
            validationState.fields.values.toList(),
            (result.effect as ValidationEffect.Validate).fields
        )
    }
}