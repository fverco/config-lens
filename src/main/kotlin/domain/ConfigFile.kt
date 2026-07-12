package com.fverco.plugin.domain

import com.intellij.openapi.vfs.VirtualFile

data class ConfigFile(
    val name: String,
    val projectRelativePath: String,
    val type: ConfigFileType,
    val virtualFile: VirtualFile
)