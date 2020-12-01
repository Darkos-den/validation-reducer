package com.darkos.mvu.validation

import com.darkos.mvu.validation.model.Field

internal fun List<Field>.replaceById(id: Long, newValue: Field): List<Field> {
    return this.map {
        if (it.id == id) {
            newValue
        } else {
            it
        }
    }
}