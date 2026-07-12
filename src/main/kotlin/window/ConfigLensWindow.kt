package com.fverco.plugin.window

import com.fverco.plugin.domain.ConfigFile
import com.fverco.plugin.scanner.ConfigFileScannerRegistry
import com.fverco.plugin.window.renderer.ConfigFileCellRenderer
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.concurrency.AppExecutorUtil
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JButton

class ConfigLensWindow(
    private val project: Project,
    private val disposable: Disposable
) {

    private val log = Logger.getInstance(ConfigLensWindow::class.java)

    private val fileListModel: DefaultListModel<ConfigFile> = DefaultListModel()

    private val content = JBPanel<JBPanel<*>>(BorderLayout()).apply {
        val refreshButton = JButton("Scan Project").apply {
            addActionListener {
                scanConfigFiles()
            }
        }

        add(refreshButton, BorderLayout.NORTH)
        add(JBScrollPane(createFileList()), BorderLayout.CENTER)
    }

    private fun createFileList(): JBList<ConfigFile?> {
        val fileList = JBList(fileListModel)
        fileList.cellRenderer = ConfigFileCellRenderer
        return fileList
    }

    private fun scanConfigFiles() {
        ReadAction
            .nonBlocking<List<ConfigFile>> {
                ConfigFileScannerRegistry.scanners
                    .flatMap { it.scan(project) }
                    .sortedBy { it.projectRelativePath }
            }
            .inSmartMode(project)
            .expireWith(disposable)
            .finishOnUiThread(ModalityState.defaultModalityState()) { files ->
                fileListModel.clear()

                if (files.isNotEmpty()) {
                    files.forEach { file ->
                        log.debug("Found config file: ${file.projectRelativePath}")
                        fileListModel.addElement(file)
                    }
                }
            }
            .submit(AppExecutorUtil.getAppExecutorService())
    }

    fun getContent(): JBPanel<JBPanel<*>> = content

}