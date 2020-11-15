package com.darkos.mvu.validation

import com.darkos.mvu.Reducer
import com.darkos.mvu.models.MVUState
import com.darkos.mvu.models.Message
import com.darkos.mvu.models.None
import com.darkos.mvu.models.StateCmdData
import com.darkos.mvu.validation.model.Field
import com.darkos.mvu.validation.model.FieldValidationStatus
import com.darkos.mvu.validation.model.mvu.ValidationMessage
import kotlin.reflect.KClass

internal class WithValidationReducer<T : MVUState>(
    val fieldId: Long,
    val map: (T) -> Field
) : Reducer<Field> {

    override fun update(
        state: Field,
        message: Message
    ): StateCmdData<Field> {
        return StateCmdData(
            state = state.copy(
                value = (message as ValidationMessage.FieldValueChanged).newValue,
                status = FieldValidationStatus.VALID
            ),
            effect = None
        )
    }
}