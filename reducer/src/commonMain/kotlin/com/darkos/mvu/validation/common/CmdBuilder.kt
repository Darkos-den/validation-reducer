package com.darkos.mvu.validation.common

import com.darkos.mvu.model.Effect
import com.darkos.mvu.model.MVUState
import com.darkos.mvu.model.StateCmdData

interface CmdBuilder<T : MVUState> {
    fun effect(block: (T) -> Effect)
    fun state(block: (T) -> T)
    fun build(state: T): StateCmdData<T>
}