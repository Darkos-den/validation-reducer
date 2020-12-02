package com.darkos.mvu.validation

import com.darkos.mvu.model.ComponentInitialized
import com.darkos.mvu.model.Effect
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
import kotlin.test.assertFailsWith

//black-box testing
//todo: add tests
class ValidationReducerTest {

    data class TestState(
        val email: String,
        val emailValid: Boolean
    ): MVUState()

    class CustomErrorEffect: Effect()

    private val emailId: Long = 1

    private fun createEmptyReducer(): ValidationReducer<TestState> {
        return ValidationReducer(
            mapperFrom = {
                ValidationState(emptyMap())
            },
            mapperTo = { _, _ ->
                TestState("", false)
            },
            errorEffect = null
        )
    }

    private fun createReducerWithCustomErrorEffect(effect: Effect): ValidationReducer<TestState> {
        return ValidationReducer(
            mapperFrom = {
                ValidationState(emptyMap())
            },
            mapperTo = { _, _ ->
                TestState("", false)
            },
            errorEffect = effect
        )
    }

    private fun createDefaultValidationState(): ValidationState {
        return ValidationState(
            fields = mapOf(
                emailId to Field(
                    id = emailId,
                    type = ValidationFieldType.Email,
                    value = "email"
                )
            )
        )
    }

    @Test
    fun checkTrigger(){
        val reducer = createEmptyReducer()
        val state = createDefaultValidationState()

        val result = reducer.update(state, ValidationMessage.Triggered)

        assertEquals(ValidationEffect.Validate::class, result.effect::class)
        assertEquals(state, result.state)
        assertEquals(
            state.fields.values.toList(),
            (result.effect as ValidationEffect.Validate).fields
        )
    }

    @Test
    fun checkOtherMessage(){
        val reducer = createEmptyReducer()
        val state = createDefaultValidationState()

        assertFailsWith(IllegalArgumentException::class) {
            reducer.update(state, ComponentInitialized)
        }
    }

    @Test
    fun checkErrorMessage(){
        val reducer = createEmptyReducer()
        val state = createDefaultValidationState()

        val wrongFields = listOf(emailId)

        val result = reducer.update(state, ValidationMessage.Error(wrongFields))

        assertEquals(None::class, result.effect::class)
        assertEquals(
            FieldValidationStatus.INVALID,
            (result.state.fields[emailId] ?: error("field is null")).status
        )
    }

    @Test
    fun checkEmptyError(){
        val reducer = createEmptyReducer()
        val state = createDefaultValidationState()

        val result = reducer.update(state, ValidationMessage.Error(emptyList()))

        assertEquals(None::class, result.effect::class)
        assertEquals(
            FieldValidationStatus.VALID,
            (result.state.fields[emailId] ?: error("field is null")).status
        )
    }

    @Test
    fun checkCustomErrorEffect(){
        val reducer = createReducerWithCustomErrorEffect(CustomErrorEffect())
        val state = createDefaultValidationState()

        val result = reducer.update(state, ValidationMessage.Error(emptyList()))

        assertEquals(CustomErrorEffect::class, result.effect::class)
    }
}