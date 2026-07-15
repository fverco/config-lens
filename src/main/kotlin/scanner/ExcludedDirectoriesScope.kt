package com.fverco.plugin.scanner

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope

class ExcludedDirectoriesScope(
    project: Project,
    private val excludedDirectories: Set<VirtualFile>
) : GlobalSearchScope(project) {

    private val delegate = projectScope(project)

    override fun isSearchInModuleContent(module: Module): Boolean {
        return delegate.isSearchInModuleContent(module)
    }

    override fun isSearchInLibraries(): Boolean {
        return delegate.isSearchInLibraries
    }

    override fun contains(file: VirtualFile): Boolean {
        if (!delegate.contains(file)) {
            return false
        }
        return excludedDirectories.none { excludedDir ->
            VfsUtilCore.isAncestor(excludedDir, file, false)
        }
    }

    override fun compare(
        file1: VirtualFile,
        file2: VirtualFile
    ): Int {
        return delegate.compare(file1, file2)
    }
}