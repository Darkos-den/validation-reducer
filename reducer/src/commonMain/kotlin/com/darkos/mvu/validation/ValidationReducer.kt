package com.darkos.mvu.validation

import com.darkos.mvu.model.*
import com.darkos.mvu.validation.model.FieldValidationStatus
import com.darkos.mvu.validation.model.ValidationState
import com.darkos.mvu.validation.model.mvu.ValidationEffect
import com.darkos.mvu.validation.model.mvu.ValidationMessage

class ValidationReducer<T : MVUState> internal constructor(
    private val mapperTo: (T, ValidationState) -> T,
    private val mapperFrom: (T) -> ValidationState,
    private val errorCmdBuilder: ValidationCmdBuilder<T>?,
    private val successCmdBuilder: ValidationCmdBuilder<T>?
) {

    fun callUpdate(
        state: T,
        message: Message
    ): StateCmdData<T> {
        return when (message) {
            is ValidationMessage.Triggered -> {
                StateCmdData(
                    state = state,
                    effect = ValidationEffect.Validate(mapperFrom(state).fields.values.toList())
                )
            }
            is ValidationMessage.Success -> {
                successCmdBuilder?.build(state) ?: StateCmdData(
                    state = state,
                    effect = None
                )
            }
            is ValidationMessage.Error -> {
                val newState = mapperFrom(state).also { vState ->//todo: need test
                    HashMap(vState.fields).also { map ->
                        message.wrongFields.forEach { wrongId ->
                            map[wrongId]?.let {
                                map[wrongId] = it.copy(status = FieldValidationStatus.INVALID)
                            }
                        }
                    }
                }.let {
                    mapperTo(state, it)
                }

                errorCmdBuilder?.build(newState) ?: StateCmdData(
                    state = newState,
                    effect = None
                )
            }
            else -> throw IllegalArgumentException()
        }
    }

    @ValidationDsl
    class Builder<T : MVUState> {
        private var mapperTo: ((T, ValidationState) -> T)? = null
        private var mapperFrom: ((T) -> ValidationState)? = null

        private var errorCmdBuilder: ValidationCmdBuilder<T>? = null
        private var successCmdBuilder: ValidationCmdBuilder<T>? = null

        fun registerMapperTo(block: (T, ValidationState) -> T) {
            mapperTo = block
        }

        fun registerMapperFrom(block: (T) -> ValidationState) {
            mapperFrom = block
        }

        fun whenError(block: ValidationCmdBuilder<T>.() -> Unit) {
            errorCmdBuilder = ValidationCmdBuilder<T>().apply(block)
        }

        fun whenSuccess(block: ValidationCmdBuilder<T>.() -> Unit) {
            successCmdBuilder = ValidationCmdBuilder<T>().apply(block)
        }

        fun build() = ValidationReducer(
            mapperTo = mapperTo!!,
            mapperFrom = mapperFrom!!,
            errorCmdBuilder = errorCmdBuilder,
            successCmdBuilder = successCmdBuilder
        )
    }
}

interface CmdBuilder<T : MVUState> {
    fun effect(block: (T) -> Effect)
    fun state(block: (T) -> T)
    fun build(state: T): StateCmdData<T>
}

class StateCmdBuilder<T : MVUState> : CmdBuilder<T> {
    private var effectBuilder: ((T) -> Effect)? = null
    private var stateBuilder: ((T) -> T)? = null

    override fun effect(block: (T) -> Effect) {
        effectBuilder = block
    }

    override fun state(block: (T) -> T) {
        stateBuilder = block
    }

    override fun build(state: T) = StateCmdData<T>(
        state = stateBuilder?.invoke(state) ?: state,
        effect = effectBuilder?.invoke(state) ?: None
    )
}

@ValidationDsl
class ValidationCmdBuilder<T : MVUState> : CmdBuilder<T> by StateCmdBuilder()

@ValidationDsl
fun <T : MVUState> ValidationReducer(block: ValidationReducer.Builder<T>.() -> Unit) =
    ValidationReducer.Builder<T>().apply(block).build()

fun check() {
    class TestState : MVUState()

    val r = ValidationReducer<TestState> {
        registerMapperFrom {
            ValidationState()
        }

        registerMapperTo { testState, validationState ->
            testState
        }

        whenError {
            effect { None }
            state { it }
        }
        whenSuccess {
            effect { None }
            state { it }
        }
    }
}