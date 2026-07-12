package com.fverco.plugin.scanner

import com.fverco.plugin.domain.ConfigFile
import com.intellij.openapi.project.Project

fun interface ConfigFileScanner {
    fun scan(project: Project): List<ConfigFile>
}