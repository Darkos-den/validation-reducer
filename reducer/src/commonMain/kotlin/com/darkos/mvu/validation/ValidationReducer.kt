package com.darkos.mvu.validation

import com.darkos.mvu.validation.model.ValidationState
import com.darkos.mvu.Reducer
import com.darkos.mvu.common.map
import com.darkos.mvu.model.*
import com.darkos.mvu.validation.model.FieldValidationStatus
import com.darkos.mvu.validation.model.mvu.ValidationEffect
import com.darkos.mvu.validation.model.mvu.ValidationMessage

class ValidationReducer<T : MVUState> internal constructor(
    private val mapperTo: (T, ValidationState) -> T,
    private val mapperFrom: (T)->ValidationState,
    private val errorEffectBuilder: (()->Effect)?,
    private val onSuccess: ((T)->StateCmdData<T>)?
) {

    fun callUpdate(
        state: T,
        message: Message
    ): StateCmdData<T> {
        if(message is ValidationMessage.Success){
            onSuccess?.invoke(state)?.let {
                return it
            }
        }

        return mapperFrom(state).let {
            update(it, message)
        }.map {
            mapperTo(state, it)
        }
    }

    private fun update(
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
                    effect = errorEffectBuilder?.invoke() ?: None
                )
            }
            else -> throw IllegalArgumentException()
        }
    }

    @ValidationDsl
    class Builder<T : MVUState> {
        private var errorEffectBuilder: (()->Effect)? = null
        private var mapperTo: ((T, ValidationState) -> T)? = null
        private var mapperFrom: ((T) -> ValidationState)? = null
        private var onSuccess: ((T)->StateCmdData<T>)? = null

        fun registerMapperTo(block: (T, ValidationState) -> T) {
            mapperTo = block
        }

        fun registerMapperFrom(block: (T) -> ValidationState) {
            mapperFrom = block
        }

        fun errorEffect(block: ()->Effect){
            errorEffectBuilder = block
        }

        fun mapErrorState(block: (T)->T){

        }

        fun error(block: StateCmdBuilder<T>.()->Unit){

        }

        fun processSuccess(block: (T) -> StateCmdData<T>){
            onSuccess = block
        }

        fun build() = ValidationReducer(
            mapperTo = mapperTo!!,
            mapperFrom = mapperFrom!!,
            errorEffectBuilder = errorEffectBuilder,
            onSuccess = onSuccess
        )
    }
}

class StateCmdBuilder<T: MVUState>{
    fun effect(block: () -> Effect){

    }

    fun state(block: (T) -> T){

    }

    fun build() = StateCmdData<T>()
}

@ValidationDsl
fun <T: MVUState>ValidationReducer(block: ValidationReducer.Builder<T>.() -> Unit) =
    ValidationReducer.Builder<T>().apply(block).build()

fun check(){
    class TestState: MVUState()
    val r = ValidationReducer<TestState> {
        processSuccess {
            StateCmdData(
                state = it,
                effect = None
            )
        }

        error {
            effect { None }
            state { it }
        }

        errorEffect {
            None
        }

        mapErrorState {
            it
        }
    }
}