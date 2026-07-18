package com.fverco.config_lens.domain

data class ConfigProperty (
    val key: String,
    val currentValue: String,
    val defaultValue: String?
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConfigProperty) return false

        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

}