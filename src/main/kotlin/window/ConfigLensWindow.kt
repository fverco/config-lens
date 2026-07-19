package com.fverco.config_lens.window

import com.fverco.config_lens.domain.ConfigFile
import com.fverco.config_lens.domain.ConfigProperty
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
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.concurrency.AppExecutorUtil
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities

class ConfigLensWindow(
    private val project: Project,
    private val disposable: Disposable
) {

    private val log = Logger.getInstance(ConfigLensWindow::class.java)

    private val content: JComponent

    private val fileListModel: DefaultListModel<ConfigFile> = DefaultListModel()

    private val fileList: JBList<ConfigFile> = JBList(fileListModel)

    private val selectedFileLabel: JLabel = JLabel("Selected File: None")

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
        return JButton("Refresh").apply {
            addActionListener {
                scanConfigFiles()
                refreshFileList()
            }
        }
    }

    private fun createPropertyExplorerPane(): JComponent {
        val splitter = Splitter(
            true,
            0.35f
        )
        splitter.firstComponent = createFileListComponent()
        splitter.secondComponent = createPropertyTable()
        return splitter
    }

    private fun createPropertyTable(): JComponent {
        val panel = JPanel(BorderLayout())
        val propertiesTable = JBTable(propertyListModel)
        propertiesTable.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    val row = propertiesTable.rowAtPoint(e.point)

                    if (row >= 0) {
                        val modelRow = propertiesTable.convertRowIndexToModel(row)
                        val property = propertyListModel.getPropertyAt(modelRow)
                        if (property == null) {
                            log.warn("Unable to determine property at row $modelRow")
                            return
                        }
                        openProperty(property)
                    }
                }
            }

            private fun openProperty(property: ConfigProperty) {
                property.navigatable.navigate(true)
            }
        })

        panel.add(selectedFileLabel, BorderLayout.NORTH)
        panel.add(JBScrollPane(propertiesTable), BorderLayout.CENTER)
        return panel
    }

    private fun createFileListComponent(): JComponent {
        fileList.cellRenderer = ConfigFileCellRenderer
        fileList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        fileList.addListSelectionListener { event ->
            if (event.valueIsAdjusting) {
                return@addListSelectionListener
            }
            refreshFileList()
        }
        return JBScrollPane(fileList)
    }

    private fun refreshFileList() {
        refreshSelectedFileLabel()
        val selectedFile = fileList.selectedValue ?: return
        propertyListModel.setRows(selectedFile.getProperties())
    }

    private fun refreshSelectedFileLabel() {
        val selectedFile = fileList.selectedValue?.projectRelativePath ?: "None"
        selectedFileLabel.text = "Selected File: $selectedFile"
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