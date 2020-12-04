package com.darkos.mvu.validation

import com.darkos.mvu.model.*
import com.darkos.mvu.validation.common.ValidationCmdBuilder
import com.darkos.mvu.validation.model.Field
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

    fun update(
        state: T,
        message: Message
    ): StateCmdData<T> {
        return when (message) {
            is ValidationMessage.Triggered -> {
                StateCmdData(
                    state = state,
                    effect = ValidationEffect.Validate(state.getValidateFields())
                )
            }
            is ValidationMessage.Success -> {
                successCmdBuilder?.build(state) ?: StateCmdData(
                    state = state,
                    effect = None
                )
            }
            is ValidationMessage.Error -> {
                calculateNewState(state, message)
                        .let(this::buildErrorCmdData)
            }
            else -> throw IllegalArgumentException()
        }
    }

    private fun T.getValidateFields(): List<Field> {
        return mapperFrom(this).fields.values.toList()
    }

    private fun buildErrorCmdData(state: T): StateCmdData<T>{
        return errorCmdBuilder?.build(state) ?: StateCmdData(
                state = state,
                effect = None
        )
    }

    private fun calculateNewState(state: T, message: ValidationMessage.Error): T {
        return mapperTo(
                state,
                updateWrongFields(
                        mapperFrom(state),
                        message.wrongFields
                )
        )
    }

    private fun updateWrongFields(state: ValidationState, wrongFields: List<Long>): ValidationState{
        return HashMap(state.fields).also { map ->
            wrongFields.forEach { wrongId ->
                map[wrongId]?.let {
                    map[wrongId] = it.copy(status = FieldValidationStatus.INVALID)
                }
            }
        }.let {
            ValidationState(it)
        }
    }

    class MapperContext<T: MVUState>{
        internal var mapperFrom: ((T)->ValidationState)? = null
        internal var mapperTo: ((T, ValidationState) -> T)? = null

        fun toValidationState(block: (T)->ValidationState){
            mapperFrom = block
        }

        fun fromValidationState(block: (T, ValidationState) -> T) {
            mapperTo = block
        }
    }

    @ValidationDsl
    class Builder<T : MVUState> {
        private var errorCmdBuilder: ValidationCmdBuilder<T>? = null
        private var successCmdBuilder: ValidationCmdBuilder<T>? = null

        private var mapper: MapperContext<T> = MapperContext()

        fun mapState(block: MapperContext<T>.()->Unit){
            mapper = MapperContext<T>().apply(block)
        }

        fun whenError(block: ValidationCmdBuilder<T>.() -> Unit) {
            errorCmdBuilder = ValidationCmdBuilder<T>().apply(block)
        }

        fun whenSuccess(block: ValidationCmdBuilder<T>.() -> Unit) {
            successCmdBuilder = ValidationCmdBuilder<T>().apply(block)
        }

        fun build() = ValidationReducer(
            mapperTo = mapper.mapperTo!!,
            mapperFrom = mapper.mapperFrom!!,
            errorCmdBuilder = errorCmdBuilder,
            successCmdBuilder = successCmdBuilder
        )
    }
}

@ValidationDsl
fun <T : MVUState> ValidationReducer(block: ValidationReducer.Builder<T>.() -> Unit) =
    ValidationReducer.Builder<T>().apply(block).build()