package com.darkos.mvu.validation.common

import com.darkos.mvu.model.MVUState
import com.darkos.mvu.validation.ValidationDsl

@ValidationDsl
class ValidationCmdBuilder<T : MVUState> : CmdBuilder<T> by StateCmdBuilder()