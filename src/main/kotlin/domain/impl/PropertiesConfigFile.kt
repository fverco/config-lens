package com.fverco.config_lens.domain.impl

import com.fverco.config_lens.domain.ConfigFile
import com.fverco.config_lens.domain.ConfigFileType
import com.fverco.config_lens.domain.ConfigProperty
import com.intellij.lang.properties.IProperty
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.vfs.VirtualFile

class PropertiesConfigFile(
    val file: PropertiesFile,
    override val projectRelativePath: String
) : ConfigFile {

    override val name: String
        get() = file.name

    override val type: ConfigFileType
        get() = ConfigFileType.PROPERTIES

    override val virtualFile: VirtualFile
        get() = file.virtualFile

    override fun getProperty(key: String): ConfigProperty? {
        val property = file.findPropertyByKey(key)
        return toConfigProperty(property)
    }

    override fun getProperties(): Set<ConfigProperty> {
        return file.properties.mapNotNull { toConfigProperty(it) }.toSet()
    }

    private fun toConfigProperty(property: IProperty?): ConfigProperty? {
        if (property == null) return null
        return ConfigProperty(
            property.key ?: return null,
            property.value.orEmpty(),
            null
        )
    }

}