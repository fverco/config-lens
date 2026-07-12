package com.fverco.plugin.scanner.impl

import com.fverco.plugin.domain.ConfigFile
import com.fverco.plugin.domain.ConfigFileType
import com.fverco.plugin.scanner.ConfigFileScanner
import com.fverco.plugin.utils.ConfigUtils
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLFile

internal object YAMLScanner : ConfigFileScanner {

    // todo: This implementation is not optimal, as it will load all YAML files in the project, even if they are not configuration files. We should implement a more efficient way to find only the relevant configuration files.
    override fun scan(project: Project): List<ConfigFile> {
        val psiManager = PsiManager.getInstance(project)

        return FileTypeIndex.getFiles(
            YAMLFileType.YML,
            GlobalSearchScope.projectScope(project)
        ).mapNotNull { file ->
            val yamlFile = psiManager.findFile(file) as? YAMLFile ?: return@mapNotNull null
            ConfigUtils.toConfigFile(yamlFile, project, ConfigFileType.YAML)
        }
    }

}