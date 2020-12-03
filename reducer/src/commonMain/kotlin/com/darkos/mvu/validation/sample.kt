package com.darkos.mvu.validation

import com.darkos.mvu.Reducer
import com.darkos.mvu.common.none
import com.darkos.mvu.model.*
import com.darkos.mvu.validation.model.Field
import com.darkos.mvu.validation.model.FieldValidationStatus
import com.darkos.mvu.validation.model.ValidationFieldType
import com.darkos.mvu.validation.model.ValidationState
import com.darkos.mvu.validation.model.mvu.ValidationMessage

//internal object Sample {
//    data class State(
//        val email: String,
//        val emailValid: Boolean
//    ): MVUState()
//
//    abstract class Messages: Message()
//    class SubmitClick: Messages()
//
//    class StateReducer: Reducer<State> {
//        private val validation = ValidationReducer<State> {
//            val emailId = 1L
//
//            errorEffect { None }
//
//            registerMapperTo { state, validationState ->
//                state.copy(
//                    emailValid = validationState.fields[emailId]?.status == FieldValidationStatus.VALID
//                )
//            }
//            registerMapperFrom {
//                mapOf(
//                    emailId to Field(
//                        id = emailId,
//                        type = ValidationFieldType.Email,
//                        value = it.email
//                    )
//                ).let {
//                    ValidationState(it)
//                }
//            }
//        }
//
//        override fun update(state: State, message: Message): StateCmdData<State> {
//            return when(message){
//                is SubmitClick -> validation.callUpdate(state, ValidationMessage.Triggered)
//                else -> state.none()
//            }
//        }
//    }
//
//    fun usage(){
//        val reducer = StateReducer()
//        val state = State(
//            email = "email",
//            emailValid = true
//        )
//
//        reducer.update(state, ValidationMessage.Triggered)
//    }
//}