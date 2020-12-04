package com.darkos.mvu.validation

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
import kotlin.test.assertTrue

//black-box testing
open class ValidationReducerTest {

    data class TestState(
        val email: String,
        val emailValid: Boolean
    ): MVUState()

    class CustomEffect: Effect()

    protected val emailId: Long = 1

    protected fun createDefaultReducer() = ValidationReducer<TestState> {
        mapState {
            fromValidationState { testState, validationState ->
                testState.copy(emailValid = validationState.fields[emailId]?.status == FieldValidationStatus.VALID)
            }
            toValidationState {
                ValidationState(
                    mapOf(
                        emailId to Field(
                            id = emailId,
                            value = it.email,
                            type = ValidationFieldType.Email
                        )
                    )
                )
            }
        }
    }

    protected fun createErrorReducer() = ValidationReducer<TestState> {
        mapState {
            fromValidationState { testState, validationState ->
                testState.copy(emailValid = validationState.fields[emailId]?.status == FieldValidationStatus.VALID)
            }
            toValidationState {
                ValidationState(
                    mapOf(
                        emailId to Field(
                            id = emailId,
                            value = it.email,
                            type = ValidationFieldType.Email
                        )
                    )
                )
            }
        }
        whenError {
            state {
                it.copy(email = "")
            }
            effect { CustomEffect() }
        }
    }

    protected fun createSuccessReducer() = ValidationReducer<TestState> {
        mapState {
            fromValidationState { testState, validationState ->
                testState.copy(emailValid = validationState.fields[emailId]?.status == FieldValidationStatus.VALID)
            }
            toValidationState {
                ValidationState(
                    mapOf(
                        emailId to Field(
                            id = emailId,
                            value = it.email,
                            type = ValidationFieldType.Email
                        )
                    )
                )
            }
        }
        whenSuccess {
            state {
                it.copy(email = "")
            }
            effect { CustomEffect() }
        }
    }

    protected fun createDefaultState() = TestState(
        email = "email",
        emailValid = true
    )

    protected fun createDefaultValidationState() = ValidationState(
        fields = mapOf(
            emailId to Field(
                id = emailId,
                value = "email",
                status = FieldValidationStatus.VALID,
                type = ValidationFieldType.Email
            )
        )
    )

    class CalculateState: ValidationReducerTest(){//todo: move to jvm and make nested

        @Test
        fun checkCalculateErrorState(){
            val reducer = createDefaultReducer()
            val state = createDefaultState()

            val result = reducer.calculateNewState(state, ValidationMessage.Error(listOf(emailId)))

            assertEquals(false, result.emailValid)
        }

        @Test
        fun checkCalculateNotErrorState(){
            val reducer = createDefaultReducer()
            val state = createDefaultState()

            val result = reducer.calculateNewState(state, ValidationMessage.Error(emptyList()))

            assertEquals(true, result.emailValid)
        }
    }

    class BuildErrorCmd: ValidationReducerTest(){

        @Test
        fun checkEmptyErrorBuilder(){
            val reducer = createDefaultReducer()
            val state = createDefaultState()

            val result = reducer.buildErrorCmdData(state)

            assertEquals(state, result.state)
            assertEquals(None::class, result.effect::class)
        }

        @Test
        fun checkNotEmptyErrorBuilder(){
            val reducer = createErrorReducer()
            val state = createDefaultState()

            val result = reducer.buildErrorCmdData(state)

            assertEquals("", result.state.email)
            assertEquals(state.emailValid, result.state.emailValid)
            assertEquals(CustomEffect::class, result.effect::class)
        }
    }

    class Success: ValidationReducerTest(){

        @Test
        fun checkEmptyBuilder(){
            val reducer = createDefaultReducer()
            val state = createDefaultState()

            val result = reducer.update(state, ValidationMessage.Success)

            assertEquals(state, result.state)
            assertEquals(None::class, result.effect::class)
        }

        @Test
        fun checkNotEmptyBuilder(){
            val reducer = createSuccessReducer()
            val state = createDefaultState()

            val result = reducer.update(state, ValidationMessage.Success)

            assertEquals("", result.state.email)
            assertEquals(state.emailValid, result.state.emailValid)
            assertEquals(CustomEffect::class, result.effect::class)
        }
    }

    class Error: ValidationReducerTest(){

        @Test
        fun checkEmptyErrorBuilder(){
            val reducer = createDefaultReducer()
            val state = createDefaultState()

            val result = reducer.update(state, ValidationMessage.Error(listOf(emailId)))

            assertEquals(state.email, result.state.email)
            assertEquals(false, result.state.emailValid)
            assertEquals(None::class, result.effect::class)
        }

        @Test
        fun checkNotEmptyErrorBuilder(){
            val reducer = createErrorReducer()
            val state = createDefaultState()

            val result = reducer.update(state, ValidationMessage.Error(listOf(emailId)))

            assertEquals("", result.state.email)
            assertEquals(false, result.state.emailValid)
            assertEquals(CustomEffect::class, result.effect::class)
        }
    }

    class Other: ValidationReducerTest(){
        @Test
        fun checkTrigger(){
            val reducer = createDefaultReducer()
            val state = createDefaultState()

            val result = reducer.update(state, ValidationMessage.Triggered)

            assertEquals(state, result.state)
            assertEquals(ValidationEffect.Validate::class, result.effect::class)
            assertTrue {
                (result.effect as ValidationEffect.Validate).fields.firstOrNull {
                    it.id == emailId
                }?.value == state.email
            }
        }

        @Test
        fun checkUpdateFields(){
            val reducer = createDefaultReducer()
            val state = createDefaultValidationState()

            val result = reducer.updateWrongFields(state, listOf(emailId))

            assertEquals(FieldValidationStatus.INVALID, result.fields[emailId]?.status)
        }
    }
}