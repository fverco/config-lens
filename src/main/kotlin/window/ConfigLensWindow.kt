package com.fverco.plugin.window

import com.fverco.plugin.domain.ConfigFile
import com.fverco.plugin.scanner.ConfigFileScannerRegistry
import com.fverco.plugin.window.renderer.ConfigFileCellRenderer
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.ui.TabbedPaneWrapper
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.concurrency.AppExecutorUtil
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class ConfigLensWindow(
    private val project: Project,
    private val disposable: Disposable
) {

    private val log = Logger.getInstance(ConfigLensWindow::class.java)

    private val content: JComponent

    private val fileListModel: DefaultListModel<ConfigFile> = DefaultListModel()

    init {
        val tabs = TabbedPaneWrapper(disposable)

        tabs.addTab("Files", createFilePanel())
        tabs.addTab("Settings", createSettingsPanel())

        content = tabs.component
    }

    private fun createSettingsPanel(): JComponent {
        return JPanel(BorderLayout()).apply {
            val excludeField = JPanel(BorderLayout())
            excludeField.add(JLabel("Exclude directories: "), BorderLayout.WEST)
            excludeField.add(JTextField(), BorderLayout.CENTER)
            add(excludeField, BorderLayout.NORTH)
        }
    }

    private fun createFilePanel(): JComponent {
        return JBPanel<JBPanel<*>>(BorderLayout()).apply {
            val refreshButton = JButton("Scan Project").apply {
                addActionListener {
                    scanConfigFiles()
                }
            }

            add(refreshButton, BorderLayout.NORTH)
            add(JBScrollPane(createFileList()), BorderLayout.CENTER)
        }
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

    fun getContent(): JComponent = content

}