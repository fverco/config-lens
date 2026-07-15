package com.fverco.plugin.scanner.impl

import com.fverco.plugin.domain.ConfigFile
import com.fverco.plugin.domain.ConfigFileType
import com.fverco.plugin.scanner.ConfigFileScanner
import com.fverco.plugin.utils.ConfigUtils
import com.intellij.lang.properties.PropertiesFileType
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope

internal object PropertiesScanner : ConfigFileScanner {

    override fun scan(project: Project, searchScope: GlobalSearchScope): List<ConfigFile> {
        val psiManager = PsiManager.getInstance(project)
        return FileTypeIndex.getFiles(
            PropertiesFileType.INSTANCE,
            searchScope
        ).mapNotNull { file ->
            val propertiesFile = psiManager.findFile(file) as? PropertiesFile ?: return@mapNotNull null
            ConfigUtils.toConfigFile(propertiesFile as PsiFile, project, ConfigFileType.PROPERTIES)
        }
    }

}