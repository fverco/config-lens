package com.fverco.config_lens.window

import com.fverco.config_lens.domain.ConfigFile
import com.fverco.config_lens.model.PropertiesTableModel
import com.fverco.config_lens.scanner.ConfigFileScannerRegistry
import com.fverco.config_lens.settings.ExcludedDirectoriesService
import com.fverco.config_lens.window.renderer.ConfigFileCellRenderer
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.Splitter
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.ui.TabbedPaneWrapper
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import com.intellij.ui.table.JBTable
import com.intellij.util.concurrency.AppExecutorUtil
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import com.intellij.ui.components.JBScrollPane

class ConfigLensWindow(
    private val project: Project,
    private val disposable: Disposable
) {

    private val log = Logger.getInstance(ConfigLensWindow::class.java)

    private val content: JComponent

    private val fileListModel: DefaultListModel<ConfigFile> = DefaultListModel()

    private val propertyListModel: PropertiesTableModel = PropertiesTableModel()

    private val excludedDirectoryListModel: DefaultListModel<String> = DefaultListModel()

    private val excludedDirectoriesService: ExcludedDirectoriesService =
        project.getService(ExcludedDirectoriesService::class.java)

    init {
        val tabs = TabbedPaneWrapper(disposable)

        tabs.addTab("Files", createFilePanel())
        tabs.addTab("Settings", createSettingsPanel())

        content = tabs.component
    }

    private fun createSettingsPanel(): JComponent {
        return JPanel(BorderLayout()).apply {
            add(createExcludedDirectoriesPanel(), BorderLayout.CENTER)
        }
    }

    private fun createExcludedDirectoriesPanel(): JComponent {
        return JBPanel<JBPanel<*>>(BorderLayout()).apply {
            val label = JLabel("Excluded directories")
            val list = JBList(excludedDirectoryListModel)
            excludedDirectoryListModel.addAll(excludedDirectoriesService.getExcludedDirectories())
            val decorator = ToolbarDecorator.createDecorator(list)
                .setAddAction {
                    val descriptor = FileChooserDescriptor(
                        false,
                        true,
                        false,
                        false,
                        false,
                        false
                    )
                    val projectRoot =
                        project.guessProjectDir() ?: return@setAddAction // todo: Might need to handle this case better
                    val selectedDir = FileChooser.chooseFile(descriptor, project, projectRoot)
                    if (selectedDir != null && VfsUtilCore.isAncestor(projectRoot, selectedDir, false)) {
                        val relativePath = VfsUtilCore.getRelativePath(selectedDir, projectRoot, '/')
                            ?: return@setAddAction // todo: Need to handle this case better
                        val added = excludedDirectoriesService.add(relativePath)
                        if (added) {
                            excludedDirectoryListModel.addElement(relativePath)
                        }
                    }
                }
                .setRemoveAction {
                    val index = list.selectedIndex
                    if (index >= 0) {
                        val removed = excludedDirectoriesService.remove(index)
                        if (removed) {
                            excludedDirectoryListModel.remove(index)
                        }
                    }
                }
                .createPanel()

            add(label, BorderLayout.NORTH)
            add(decorator, BorderLayout.CENTER)
        }
    }

    private fun createFilePanel(): JComponent {
        return JBPanel<JBPanel<*>>(BorderLayout()).apply {
            add(createRefreshButton(), BorderLayout.NORTH)
            add(createPropertyExplorerPane(), BorderLayout.CENTER)
        }
    }

    private fun createRefreshButton(): JButton {
        return JButton("Scan Project").apply {
            addActionListener {
                scanConfigFiles()
            }
        }
    }

    private fun createPropertyExplorerPane(): JComponent {
        val splitter = Splitter(
            true,
            0.35f
        )
        splitter.firstComponent = createFileList()
        splitter.secondComponent = JBScrollPane(createPropertyTable())
        return splitter
    }

    private fun createPropertyTable(): JBTable = JBTable(propertyListModel)

    private fun createFileList(): JBList<ConfigFile?> {
        val fileList = JBList(fileListModel)
        fileList.cellRenderer = ConfigFileCellRenderer
        fileList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        fileList.addListSelectionListener { event ->
            if (event.valueIsAdjusting) {
                return@addListSelectionListener
            }
            val selectedFile = fileList.selectedValue ?: return@addListSelectionListener
            propertyListModel.setRows(selectedFile.getProperties())
        }
        return fileList
    }

    private fun scanConfigFiles() {
        val excludedDirectoriesService = project.getService(ExcludedDirectoriesService::class.java)
        val searchScope = excludedDirectoriesService.getSearchScope()

        ReadAction
            .nonBlocking<List<ConfigFile>> {
                ConfigFileScannerRegistry.scanners
                    .flatMap { it.scan(project, searchScope) }
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