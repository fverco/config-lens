package com.fverco.plugin.settings

import com.fverco.plugin.scanner.ExcludedDirectoriesScope
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil.normalize
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile

@Service(Service.Level.PROJECT)
class ExcludedDirectoriesService(
    val project: Project
) {

    private val settings = ConfigLensSettings.getInstance(project)

    fun getExcludedDirectories(): List<String> {
        return excludedDirectories()
    }

    fun add(directory: String): Boolean {
        val normalized = normalize(directory)
        if (normalized in excludedDirectories()) {
            return false
        }
        return excludedDirectories().add(normalized)
    }

    fun remove(directory: String): Boolean {
        val normalized = normalize(directory)
        if (normalized in excludedDirectories()) {
            return excludedDirectories().remove(normalized)
        }
        return false
    }

    fun remove(index: Int): Boolean {
        if (index >= 0 && index < excludedDirectories().size) {
            excludedDirectories().removeAt(index)
            return true
        }
        return false
    }

    fun getSearchScope(): ExcludedDirectoriesScope {
        return ExcludedDirectoriesScope(project, convertDirectoriesToVirtualFiles())
    }

    private fun convertDirectoriesToVirtualFiles(): Set<VirtualFile> {
        val basePath = project.basePath ?: throw RuntimeException("Unable to get project path.")
        return excludedDirectories()
            .mapNotNull { relativePath -> LocalFileSystem.getInstance().findFileByPath("$basePath/$relativePath") }
            .toSet()
    }

    private fun excludedDirectories(): MutableList<String> = settings.state.excludedDirectories

}