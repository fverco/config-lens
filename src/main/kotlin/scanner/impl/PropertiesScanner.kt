package com.fverco.config_lens.scanner.impl

import com.fverco.config_lens.domain.ConfigFile
import com.fverco.config_lens.scanner.ConfigFileScanner
import com.fverco.config_lens.utils.ConfigUtils
import com.intellij.lang.properties.PropertiesFileType
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.project.Project
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
            ConfigUtils.toPropertiesConfigFile(propertiesFile, project)
        }
    }

}