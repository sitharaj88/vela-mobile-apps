/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.filemanager.presentation

import androidx.lifecycle.viewModelScope
import com.vela.apps.filemanager.domain.FileSystem
import com.vela.apps.filemanager.domain.SortOrder
import com.vela.core.common.DispatcherProvider
import com.vela.core.common.MviStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * MVI store driving the file browser: navigation, sort/search/hidden toggles, multi-select, and the
 * create/rename/delete/copy/move operations. All blocking [FileSystem] calls run on [dispatchers].io.
 */
class FileBrowserStore(
    private val fileSystem: FileSystem,
    private val dispatchers: DispatcherProvider,
) : MviStore<FileBrowserState, FileBrowserIntent, FileBrowserEffect>(FileBrowserState(isLoading = true)) {

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            val roots = withContext(dispatchers.io) { fileSystem.roots() }
            val start = roots.firstOrNull()?.path
            if (start == null) {
                setState { copy(isLoading = false, error = "No accessible storage") }
            } else {
                setState { copy(roots = roots) }
                load(start)
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    override fun onIntent(intent: FileBrowserIntent) {
        when (intent) {
            is FileBrowserIntent.Open -> open(intent.path)
            FileBrowserIntent.GoUp -> goUp()
            FileBrowserIntent.Refresh -> reload()
            is FileBrowserIntent.QueryChanged -> onQueryChanged(intent.query)
            FileBrowserIntent.ClearSearch -> clearSearch()
            is FileBrowserIntent.SortChanged -> setSort(intent.order)
            FileBrowserIntent.ToggleHidden -> setState { copy(showHidden = !showHidden) }
            is FileBrowserIntent.ToggleSelect -> toggleSelect(intent.path)
            FileBrowserIntent.ClearSelection -> setState { copy(selection = emptySet()) }
            FileBrowserIntent.SelectAll -> selectAll()
            is FileBrowserIntent.CreateFolder -> createFolder(intent.name)
            is FileBrowserIntent.Rename -> rename(intent.path, intent.newName)
            is FileBrowserIntent.Delete -> delete(intent.paths)
            is FileBrowserIntent.Copy -> transfer(intent.paths, intent.targetDir, move = false)
            is FileBrowserIntent.Move -> transfer(intent.paths, intent.targetDir, move = true)
        }
    }

    private fun open(path: String) {
        val target = currentState.visibleEntries.firstOrNull { it.path == path }
        if (target != null && !target.isDirectory) return
        load(path)
    }

    private fun goUp() {
        val parent = fileSystem.parentOf(currentState.currentPath) ?: return
        load(parent)
    }

    private fun reload() = load(currentState.currentPath)

    private fun load(path: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null, selection = emptySet(), isSearching = false, query = "") }
            val entries = withContext(dispatchers.io) { fileSystem.list(path) }
            val parent = withContext(dispatchers.io) { fileSystem.parentOf(path) }
            setState {
                copy(
                    currentPath = path,
                    entries = entries,
                    canGoUp = parent != null,
                    isLoading = false,
                )
            }
        }
    }

    private fun onQueryChanged(query: String) {
        setState { copy(query = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            setState { copy(isSearching = false, searchResults = emptyList()) }
            return
        }
        searchJob = viewModelScope.launch {
            setState { copy(isSearching = true, isLoading = true) }
            val results = withContext(dispatchers.io) { fileSystem.search(currentState.currentPath, query) }
            setState { copy(searchResults = results, isLoading = false) }
        }
    }

    private fun clearSearch() {
        searchJob?.cancel()
        setState { copy(query = "", isSearching = false, searchResults = emptyList(), isLoading = false) }
    }

    private fun setSort(order: SortOrder) = setState { copy(sortOrder = order) }

    private fun toggleSelect(path: String) = setState {
        copy(selection = if (path in selection) selection - path else selection + path)
    }

    private fun selectAll() = setState { copy(selection = visibleEntries.map { it.path }.toSet()) }

    private fun createFolder(name: String) {
        val clean = name.trim()
        if (clean.isEmpty()) {
            emitEffect(FileBrowserEffect.ShowError("Folder name cannot be empty"))
            return
        }
        viewModelScope.launch {
            val created = withContext(dispatchers.io) { fileSystem.createFolder(currentState.currentPath, clean) }
            if (created == null) {
                emitEffect(FileBrowserEffect.ShowError("Could not create \"$clean\""))
            } else {
                emitEffect(FileBrowserEffect.ShowMessage("Created \"$clean\""))
                reload()
            }
        }
    }

    private fun rename(path: String, newName: String) {
        val clean = newName.trim()
        if (clean.isEmpty()) {
            emitEffect(FileBrowserEffect.ShowError("Name cannot be empty"))
            return
        }
        viewModelScope.launch {
            val renamed = withContext(dispatchers.io) { fileSystem.rename(path, clean) }
            if (renamed == null) emitEffect(FileBrowserEffect.ShowError("Rename failed")) else reload()
        }
    }

    private fun delete(paths: Set<String>) {
        if (paths.isEmpty()) return
        viewModelScope.launch {
            val failures = withContext(dispatchers.io) { paths.count { !fileSystem.delete(it) } }
            if (failures > 0) emitEffect(FileBrowserEffect.ShowError("$failures item(s) could not be deleted"))
            else emitEffect(FileBrowserEffect.ShowMessage("Deleted ${paths.size} item(s)"))
            setState { copy(selection = emptySet()) }
            reload()
        }
    }

    private fun transfer(paths: Set<String>, targetDir: String, move: Boolean) {
        if (paths.isEmpty()) return
        viewModelScope.launch {
            val failures = withContext(dispatchers.io) {
                paths.count { p ->
                    val result = if (move) fileSystem.move(p, targetDir) else fileSystem.copy(p, targetDir)
                    result == null
                }
            }
            val verb = if (move) "move" else "copy"
            if (failures > 0) emitEffect(FileBrowserEffect.ShowError("$failures item(s) could not be ${verb}d"))
            else emitEffect(FileBrowserEffect.ShowMessage("${if (move) "Moved" else "Copied"} ${paths.size} item(s)"))
            setState { copy(selection = emptySet()) }
            reload()
        }
    }
}
