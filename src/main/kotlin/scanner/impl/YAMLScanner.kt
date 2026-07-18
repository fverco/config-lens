package com.fverco.config_lens.scanner.impl

import com.fverco.config_lens.domain.ConfigFile
import com.fverco.config_lens.scanner.ConfigFileScanner
import com.fverco.config_lens.utils.ConfigUtils
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLFile

internal object YAMLScanner : ConfigFileScanner {

    override fun scan(project: Project, searchScope: GlobalSearchScope): List<ConfigFile> {
        val psiManager = PsiManager.getInstance(project)
        return FileTypeIndex.getFiles(
            YAMLFileType.YML,
            searchScope
        ).mapNotNull { file ->
            val yamlFile = psiManager.findFile(file) as? YAMLFile ?: return@mapNotNull null
            ConfigUtils.toYAMLConfigFile(yamlFile, project)
        }
    }

}