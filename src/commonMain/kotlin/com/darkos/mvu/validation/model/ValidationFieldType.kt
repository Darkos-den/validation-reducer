package com.darkos.mvu.validation.model

sealed class ValidationFieldType {
    object Email : ValidationFieldType()
    data class MinSymbols(val minCount: Int): ValidationFieldType()
    data class MaxSymbols(val maxCount: Int): ValidationFieldType()
    data class IntervalSymbols(val minCount: Int, val maxCount: Int): ValidationFieldType()
    data class Custom(val id: Long) : ValidationFieldType()
}