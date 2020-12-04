package com.darkos.mvu.validation.common

import com.darkos.mvu.model.MVUState
import com.darkos.mvu.validation.model.ValidationState

class MapperContext<T: MVUState>{
        internal var mapperFrom: ((T)-> ValidationState)? = null
        internal var mapperTo: ((T, ValidationState) -> T)? = null

        fun toValidationState(block: (T)->ValidationState){
            mapperFrom = block
        }

        fun fromValidationState(block: (T, ValidationState) -> T) {
            mapperTo = block
        }
    }