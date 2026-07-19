package com.fverco.config_lens.domain

import com.intellij.psi.PsiFile

interface ConfigFile {
    val name: String
    val projectRelativePath: String
    val type: ConfigFileType
    val psiFile: PsiFile
    fun getProperty(key: String): ConfigProperty?
    fun getProperties(): Set<ConfigProperty>
}