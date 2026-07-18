package com.fverco.config_lens.scanner

import com.fverco.config_lens.scanner.impl.PropertiesScanner
import com.fverco.config_lens.scanner.impl.YAMLScanner
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