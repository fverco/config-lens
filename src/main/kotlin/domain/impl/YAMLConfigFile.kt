package com.fverco.config_lens.domain.impl

import com.fverco.config_lens.domain.ConfigFile
import com.fverco.config_lens.domain.ConfigFileType
import com.fverco.config_lens.domain.ConfigProperty
import com.fverco.config_lens.window.ConfigLensWindow
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.NlsSafe
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiFile
import org.jetbrains.yaml.YAMLUtil.getQualifiedKeyInFile
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar
import org.jetbrains.yaml.psi.YAMLSequence
import org.jetbrains.yaml.psi.YAMLValue

class YAMLConfigFile(
    val file: YAMLFile,
    override val projectRelativePath: String
) : ConfigFile {

    private val log = Logger.getInstance(ConfigLensWindow::class.java)

    override val name: String
        get() = file.name

    override val type: ConfigFileType
        get() = ConfigFileType.YAML

    override val psiFile: PsiFile
        get() = file

    override fun getProperty(key: String): ConfigProperty? {
        val property = getQualifiedKeyInFile(file, key) ?: return null
        return toConfigProperty(property)
    }

    override fun getProperties(): Set<ConfigProperty> {
        val properties = mutableSetOf<ConfigProperty>()
        file.documents.forEach { document ->
            val yamlValue = document.topLevelValue ?: return@forEach
            collectProperties(yamlValue, "", properties)
        }
        return properties
    }


    /**
     * Recursively collects configuration properties from a given YAML structure.
     *
     * @param yamlValue The YAMLValue to analyze, which can be a mapping, sequence, or scalar.
     * @param prefix The key prefix to use for constructing fully qualified property keys.
     * @param result A collection where the discovered ConfigProperty instances are stored.
     */
    private fun collectProperties(
        yamlValue: YAMLValue,
        prefix: String,
        result: MutableSet<ConfigProperty>
    ) {
        when (yamlValue) {

            is YAMLMapping ->
                handleYAMLMapping(yamlValue, prefix, result)

            is YAMLSequence ->
                handleYAMLSequence(yamlValue, prefix, result)

            is YAMLScalar ->
                addYAMLScalar(prefix, yamlValue.textValue, result, yamlValue)

            // Log unknown value type.
            else ->
                log.warn("Unhandled YAMLValue: ${yamlValue::class}")
        }
    }

    /**
     * Handles a YAMLSequence as part of the property collecting process.
     * YAMLSequence: Represents a list, e.g.
     * ```
     *     servers:
     *         - localhost
     *         - production
     * ```
     * `servers` is a YAMLSequence
     *
     * @param yamlSequence The YAMLSequence to handle.
     * @param prefix The prefix for the qualified key.
     * @param result The collection where the constructed ConfigProperty is added.
     */
    private fun handleYAMLSequence(
        yamlSequence: YAMLSequence,
        prefix: String,
        result: MutableSet<ConfigProperty>
    ) {
        val items = yamlSequence.items
        for ((index, item) in items.withIndex()) {
            val qualifiedKey = getQualifiedKey(prefix, index)
            val itemValue = item.value
            if (itemValue != null) {
                collectProperties(itemValue, qualifiedKey, result)
            } else {
                val configProperty = toConfigProperty(prefix, item.text, item)
                result.add(configProperty)
            }
        }
    }

    /**
     * Handles a YAMLMapping as part of the property collecting process.
     * YAMLMapping: Represents a part of an entire key, e.g.
     * ```
     *      server:
     *          port: 8080
     *          host: localhost
     * ```
     * `server` is a YAMLMapping
     *
     * @param yamlMapping The YAMLMapping to handle.
     * @param prefix The prefix for the qualified key.
     * @param result The collection where the constructed ConfigProperty is added.
     */
    private fun handleYAMLMapping(
        yamlMapping: YAMLMapping,
        prefix: String,
        result: MutableSet<ConfigProperty>
    ) {
        for (keyValue in yamlMapping.keyValues) {
            val qualifiedKey = getQualifiedKey(prefix, keyValue)
            val value = keyValue.value ?: continue
            collectProperties(value, qualifiedKey, result)
        }
    }

    /**
     * Adds a YAML scalar value to the collection of configuration properties.
     * YAMLScalar: Represents a leaf node in the property key.
     * ```
     *       server:
     *           port: 8080
     *           host: localhost
     *  ```
     *  `port` and `host` are YAMLScalars
     *
     * @param qualifiedKey The fully qualified key for the YAML scalar.
     * @param value The string value of the YAML scalar.
     * @param result The collection where the constructed ConfigProperty is added.
     */
    private fun addYAMLScalar(
        qualifiedKey: @NlsSafe String,
        value: String,
        result: MutableSet<ConfigProperty>,
        navigatable: Navigatable
    ) {
        val configProperty = toConfigProperty(qualifiedKey, value, navigatable)
        result.add(configProperty)
    }

    private fun getQualifiedKey(
        prefix: String,
        index: Int
    ): String {
        return "$prefix[$index]"
    }

    private fun getQualifiedKey(
        prefix: String,
        keyValue: YAMLKeyValue?
    ): @NlsSafe String {
        return if (prefix.isEmpty())
            keyValue?.keyText ?: ""
        else
            "$prefix.${keyValue?.keyText ?: ""}"
    }

    private fun toConfigProperty(property: YAMLKeyValue): ConfigProperty {
        return toConfigProperty(property.keyText, property.valueText, property)
    }

    private fun toConfigProperty(
        key: String,
        value: String,
        navigatable: Navigatable
    ): ConfigProperty {
        return ConfigProperty(
            key,
            value,
            null,
            navigatable
        )
    }

}