package com.fverco.config_lens.utils

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.PsiFile

object FileUtils {

    internal fun getProjectRelativePath(file: PsiFile, project: Project): String {
        val virtualFile = file.virtualFile
        val contentRoot = ProjectFileIndex.getInstance(project).getContentRootForFile(virtualFile)

        return if (contentRoot != null) {
            virtualFile.path.removePrefix(contentRoot.path).removePrefix("/")
        } else {
            virtualFile.path
        }
    }

}