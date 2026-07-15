package com.fverco.plugin.scanner

import com.fverco.plugin.domain.ConfigFile
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope

fun interface ConfigFileScanner {
    fun scan(project: Project, searchScope: GlobalSearchScope): List<ConfigFile>
}