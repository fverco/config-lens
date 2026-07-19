package com.fverco.config_lens.window

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class ConfigLensWindowFactory : ToolWindowFactory {
    override fun shouldBeAvailable(project: Project) = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val disposable: Disposable = Disposer.newDisposable("ConfigLensWindow")
        val configLensWindow = ConfigLensWindow(project, disposable)
        val content = ContentFactory.getInstance().createContent(configLensWindow.content, null, false)

        content.setDisposer(disposable)
        toolWindow.contentManager.addContent(content)
    }

}
