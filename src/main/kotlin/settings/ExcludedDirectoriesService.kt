package com.fverco.plugin.settings

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil.normalize

@Service(Service.Level.PROJECT)
class ExcludedDirectoriesService(
    private val project: Project
) {

    private val settings = ConfigLensSettings.getInstance(project)

    fun getExcludedDirectories(): List<String> {
        return settings.state.excludedDirectories
    }

    fun add(directory: String): Boolean {
        val normalized = normalize(directory)
        if (normalized in settings.state.excludedDirectories) {
            return false
        }
        return settings.state.excludedDirectories.add(normalized)
    }

    fun remove(path: String): Boolean {
        val normalized = normalize(path)
        if (normalized in settings.state.excludedDirectories) {
            return settings.state.excludedDirectories.remove(normalized)
        }
        return false
    }

    fun remove(index: Int): Boolean {
        if (index >= 0 && index < settings.state.excludedDirectories.size) {
            settings.state.excludedDirectories.removeAt(index)
            return true
        }
        return false
    }

}