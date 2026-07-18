package com.fverco.config_lens.domain.impl

import com.fverco.config_lens.domain.ConfigFile
import com.fverco.config_lens.domain.ConfigFileType
import com.fverco.config_lens.domain.ConfigProperty
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.yaml.YAMLUtil.getQualifiedKeyInFile
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping

class YAMLConfigFile(
    val file: YAMLFile,
    override val projectRelativePath: String
) : ConfigFile {

    override val name: String
        get() = file.name

    override val type: ConfigFileType
        get() = ConfigFileType.YAML

    override val virtualFile: VirtualFile
        get() = file.virtualFile

    override fun getProperty(key: String): ConfigProperty? {
        val property = getQualifiedKeyInFile(file, key) ?: return null
        return toConfigProperty(property)
    }

    override fun getProperties(): Set<ConfigProperty> {
        val properties = mutableSetOf<ConfigProperty>()
        file.documents.forEach { document ->
            val yamlMapping = document.topLevelValue as? YAMLMapping ?: return@forEach
            collectProperties(yamlMapping, "", properties)
        }
        return properties
    }

    // todo: This is not handling all YAML property types like sequences, scalars, etc. It only handles mappings for now.
    private fun collectProperties(
        mapping: YAMLMapping,
        prefix: String,
        result: MutableSet<ConfigProperty>
    ) {
        for (keyValue in mapping.keyValues) {
            val qualifiedKey =
                if (prefix.isEmpty())
                    keyValue.keyText
                else
                    "$prefix.${keyValue.keyText}"

            when (val value = keyValue.value) {
                is YAMLMapping ->
                    collectProperties(value, qualifiedKey, result)

                else -> {
                    val configProperty = toConfigProperty(qualifiedKey, keyValue.valueText)
                    result.add(configProperty)
                }
            }
        }
    }

    private fun toConfigProperty(property: YAMLKeyValue): ConfigProperty {
        return toConfigProperty(property.keyText, property.valueText)
    }

    private fun toConfigProperty(
        key: String,
        value: String
    ): ConfigProperty {
        return ConfigProperty(
            key,
            value,
            null
        )
    }

}