package com.fverco.config_lens.domain

import com.intellij.openapi.vfs.VirtualFile

interface ConfigFile {
    val name: String
    val projectRelativePath: String
    val type: ConfigFileType
    val virtualFile: VirtualFile
    fun getProperty(key: String): ConfigProperty?
    fun getProperties(): Set<ConfigProperty>
}