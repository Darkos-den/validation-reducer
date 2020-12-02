package com.darkos.mvu.validation

import com.darkos.mvu.validation.model.ValidationState
import com.darkos.mvu.Reducer
import com.darkos.mvu.common.map
import com.darkos.mvu.model.*
import com.darkos.mvu.validation.model.Field
import com.darkos.mvu.validation.model.FieldValidationStatus
import com.darkos.mvu.validation.model.ValidationFieldType
import com.darkos.mvu.validation.model.mvu.ValidationEffect
import com.darkos.mvu.validation.model.mvu.ValidationMessage

class ValidationReducer<T : MVUState> internal constructor(
    private val mapperTo: (T, ValidationState) -> T,
    private val mapperFrom: (T)->ValidationState,
    private val errorEffect: Effect?
) : Reducer<ValidationState> {

    fun callUpdate(
        state: T,
        message: Message
    ): StateCmdData<T> {
        return mapperFrom(state).let {
            update(it, message)
        }.map {
            mapperTo(state, it)
        }
    }

    override fun update(
        state: ValidationState,
        message: Message
    ): StateCmdData<ValidationState> {
        return when (message) {
            is ValidationMessage.Triggered -> {
                StateCmdData(
                    state = state,
                    effect = ValidationEffect.Validate(state.fields.values.toList())
                )
            }
            is ValidationMessage.Error -> {
                val map = HashMap(state.fields)
                message.wrongFields.forEach { wrongId ->
                    map[wrongId]?.let {
                        map[wrongId] = it.copy(status = FieldValidationStatus.INVALID)
                    }
                }

                StateCmdData(
                    state = state.copy(fields = map),
                    effect = errorEffect ?: None
                )
            }
            else -> throw IllegalArgumentException()
        }
    }

    @ValidationDsl
    class Builder<T : MVUState> {
        var errorEffect: Effect? = null
        private var mapperTo: ((T, ValidationState) -> T)? = null
        private var mapperFrom: ((T) -> ValidationState)? = null

        fun registerMapperTo(block: (T, ValidationState) -> T) {
            mapperTo = block
        }

        fun registerMapperFrom(block: (T) -> ValidationState) {
            mapperFrom = block
        }

        fun build() = ValidationReducer(
            mapperTo = mapperTo!!,
            mapperFrom = mapperFrom!!,
            errorEffect = errorEffect
        )
    }
}

@ValidationDsl
fun <T: MVUState>ValidationReducer(block: ValidationReducer.Builder<T>.() -> Unit) =
    ValidationReducer.Builder<T>().apply(block).build()