package com.fverco.config_lens.model

import com.fverco.config_lens.domain.ConfigProperty
import javax.swing.table.AbstractTableModel

class PropertiesTableModel : AbstractTableModel() {

    private val rows = mutableSetOf<ConfigProperty>()

    override fun getRowCount() = rows.size

    override fun getColumnCount() = 3

    override fun getColumnName(column: Int) = when (column) {
        0 -> "Property"
        1 -> "Current"
        2 -> "Default"
        else -> ""
    }

    override fun getValueAt(row: Int, column: Int): Any? {
        val property = rows.elementAtOrNull(row) ?: return null
        return when (column) {
            0 -> property.key
            1 -> property.currentValue
            2 -> property.defaultValue
            else -> null
        }
    }

    fun getPropertyAt(row: Int): ConfigProperty? {
        return rows.elementAtOrNull(row)
    }

    fun setRows(newRows: Set<ConfigProperty>) {
        rows.clear()
        rows.addAll(newRows)
        fireTableDataChanged()
    }
}