package com.darkos.mvu.validation.common

import com.darkos.mvu.model.Effect
import com.darkos.mvu.model.MVUState
import com.darkos.mvu.model.None
import com.darkos.mvu.model.StateCmdData

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