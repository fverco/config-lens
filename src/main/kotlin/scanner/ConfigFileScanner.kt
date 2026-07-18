package com.fverco.config_lens.scanner

import com.fverco.config_lens.domain.ConfigFile
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope

fun interface ConfigFileScanner {
    fun scan(project: Project, searchScope: GlobalSearchScope): List<ConfigFile>
}