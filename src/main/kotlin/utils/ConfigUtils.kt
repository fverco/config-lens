package com.fverco.config_lens.utils

import com.fverco.config_lens.domain.ConfigFile
import com.fverco.config_lens.domain.impl.PropertiesConfigFile
import com.fverco.config_lens.domain.impl.YAMLConfigFile
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.yaml.psi.YAMLFile

object ConfigUtils {

    internal fun toYAMLConfigFile(file: YAMLFile, project: Project): ConfigFile {
        val projectRelativePath = FileUtils.getProjectRelativePath(file, project)
        return YAMLConfigFile(file, projectRelativePath)
    }

    internal fun toPropertiesConfigFile(file: PropertiesFile, project: Project): ConfigFile {
        val projectRelativePath = FileUtils.getProjectRelativePath(file as PsiFile, project)
        return PropertiesConfigFile(file, projectRelativePath)
    }

}