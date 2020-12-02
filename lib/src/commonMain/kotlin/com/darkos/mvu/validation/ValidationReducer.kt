package com.darkos.mvu.validation

import com.darkos.mvu.validation.model.ValidationState
import com.darkos.mvu.Reducer
import com.darkos.mvu.common.map
import com.darkos.mvu.model.*
import com.darkos.mvu.validation.model.Field
import com.darkos.mvu.validation.model.FieldValidationStatus
import com.darkos.mvu.validation.model.mvu.ValidationEffect
import com.darkos.mvu.validation.model.mvu.ValidationMessage

class ValidationReducer<T : MVUState> private constructor(
    private val withValidationProcessors: List<WithValidationReducer<T>>,
    val mapper: (T, ValidationState) -> T,
    private val errorEffect: Effect?
) : Reducer<ValidationState> {

    fun map(state: T): ValidationState {
        return ValidationState(
            fields = withValidationProcessors.map {
                it.map(state)
            }.map {
                it.id to it
            }.let {
                mapOf(*it.toTypedArray())
            }
        )
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
        private var processors: List<WithValidationReducer<T>> = emptyList()
        var errorEffect: Effect? = null
        private var mapper: ((T, ValidationState) -> T)? = null

        fun registerValidationMapper(block: (T, ValidationState) -> T) {
            mapper = block
        }

        fun registerField(
            fieldId: Long,
            map: (T) -> Field
        ) {
            processors = processors + WithValidationReducer(
                fieldId, map
            )
        }

        fun build() = ValidationReducer(
            withValidationProcessors = processors,
            mapper = mapper!!,
            errorEffect = errorEffect
        )
    }
}