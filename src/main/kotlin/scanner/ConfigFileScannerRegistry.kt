package com.fverco.plugin.scanner

import com.fverco.plugin.scanner.impl.PropertiesScanner
import com.fverco.plugin.scanner.impl.YAMLScanner
import com.intellij.openapi.diagnostic.Logger

internal object ConfigFileScannerRegistry {

    private val log = Logger.getInstance(ConfigFileScannerRegistry::class.java)

    internal val scanners = listOf(
        PropertiesScanner,
        YAMLScanner
    ).also { scanners ->
        scanners.forEach {
            log.debug("Registered scanner: ${it::class.simpleName}")
        }
    }

}