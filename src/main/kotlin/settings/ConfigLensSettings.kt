package com.fverco.config_lens.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(
    name = "ConfigLensProjectSettings",
    storages = [Storage("configLens.xml")]
)
class ConfigLensSettings : PersistentStateComponent<ConfigLensSettings.State> {

    data class State(
        var excludedDirectories: MutableList<String> = mutableListOf()
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {
        fun getInstance(project: Project): ConfigLensSettings =
            project.getService(ConfigLensSettings::class.java)
    }
}