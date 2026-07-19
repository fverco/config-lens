package com.fverco.config_lens.window.renderer

import com.fverco.config_lens.domain.ConfigFile
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JLabel
import javax.swing.JList

internal object ConfigFileCellRenderer : DefaultListCellRenderer() {

    private fun readResolve(): Any = ConfigFileCellRenderer

    override fun getListCellRendererComponent(
        list: JList<*>,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {

        val label = super.getListCellRendererComponent(
            list,
            value,
            index,
            isSelected,
            cellHasFocus
        ) as JLabel

        // Set the text and icon for the label based on the ConfigFile object
        if (value is ConfigFile) {
            label.text = value.projectRelativePath
            label.icon = value.psiFile.fileType.icon
        }

        return label
    }

}