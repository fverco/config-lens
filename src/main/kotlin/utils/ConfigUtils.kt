package com.fverco.plugin.utils

import com.fverco.plugin.domain.ConfigFile
import com.fverco.plugin.domain.ConfigFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

object ConfigUtils {

    internal fun toConfigFile(psiFile: PsiFile, project: Project, configFileType: ConfigFileType): ConfigFile {
        val name = psiFile.name
        val projectRelativePath = FileUtils.getProjectRelativePath(psiFile, project)
        return ConfigFile(name, projectRelativePath, configFileType, psiFile.virtualFile)
    }

}