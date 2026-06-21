/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.filemanager.presentation

import app.cash.turbine.test
import com.vela.apps.filemanager.domain.FileNode
import com.vela.apps.filemanager.domain.FileSystem
import com.vela.apps.filemanager.domain.SortField
import com.vela.apps.filemanager.domain.SortOrder
import com.vela.core.common.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FileBrowserStoreTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private class TestDispatchers(private val d: CoroutineDispatcher) : DispatcherProvider {
        override val main = d
        override val default = d
        override val io = d
    }

    /** Minimal in-memory tree keyed by path; supports the operations the store exercises. */
    private class FakeFileSystem : FileSystem {
        private val tree = linkedMapOf<String, MutableList<FileNode>>()

        init {
            tree["/root"] = mutableListOf(
                dir("/root/photos", "photos"),
                file("/root/notes.txt", "notes.txt", 120, 50),
                file("/root/.hidden", ".hidden", 10, 10),
                file("/root/archive.zip", "archive.zip", 9000, 90),
            )
            tree["/root/photos"] = mutableListOf(file("/root/photos/sunset.png", "sunset.png", 2048, 70))
        }

        override fun roots() = listOf(dir("/root", "root"))
        override fun list(path: String) = tree[path]?.toList() ?: emptyList()
        override fun parentOf(path: String) = if (path == "/root") null else path.substringBeforeLast('/')

        override fun createFolder(parentPath: String, name: String): FileNode? {
            val node = dir("$parentPath/$name", name)
            tree.getOrPut(parentPath) { mutableListOf() }.add(node)
            tree[node.path] = mutableListOf()
            return node
        }

        override fun rename(path: String, newName: String): FileNode? {
            val parent = path.substringBeforeLast('/')
            val siblings = tree[parent] ?: return null
            val index = siblings.indexOfFirst { it.path == path }
            if (index < 0) return null
            val renamed = siblings[index].copy(path = "$parent/$newName", name = newName)
            siblings[index] = renamed
            return renamed
        }

        override fun delete(path: String): Boolean {
            val parent = path.substringBeforeLast('/')
            return tree[parent]?.removeAll { it.path == path } ?: false
        }

        override fun copy(sourcePath: String, targetDir: String): FileNode? {
            val source = findNode(sourcePath) ?: return null
            val node = source.copy(path = "$targetDir/${source.name}")
            tree.getOrPut(targetDir) { mutableListOf() }.add(node)
            return node
        }

        override fun move(sourcePath: String, targetDir: String): FileNode? {
            val moved = copy(sourcePath, targetDir) ?: return null
            delete(sourcePath)
            return moved
        }

        override fun search(path: String, query: String): List<FileNode> =
            tree.values.flatten().filter { it.name.contains(query, ignoreCase = true) }

        private fun findNode(path: String) = tree.values.flatten().firstOrNull { it.path == path }
        private fun dir(path: String, name: String) = FileNode(path, name, true, 0, 0, "")
        private fun file(path: String, name: String, size: Long, modified: Long) =
            FileNode(path, name, false, size, modified, name.substringAfterLast('.', ""))
    }

    private lateinit var fs: FakeFileSystem
    private lateinit var store: FileBrowserStore

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        fs = FakeFileSystem()
        store = FileBrowserStore(fs, TestDispatchers(dispatcher))
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun initial_state_loads_root_and_hides_hidden_files() = runTest(dispatcher) {
        val state = store.state.value
        assertEquals("/root", state.currentPath)
        assertFalse(state.canGoUp)
        assertEquals(listOf("photos", "archive.zip", "notes.txt"), state.visibleEntries.map { it.name })
    }

    @Test
    fun toggle_hidden_reveals_dot_files() = runTest(dispatcher) {
        assertFalse(store.state.value.visibleEntries.any { it.isHidden })
        store.onIntent(FileBrowserIntent.ToggleHidden)
        assertTrue(store.state.value.visibleEntries.any { it.name == ".hidden" })
    }

    @Test
    fun opening_a_directory_navigates_and_enables_up() = runTest(dispatcher) {
        store.onIntent(FileBrowserIntent.Open("/root/photos"))
        val state = store.state.value
        assertEquals("/root/photos", state.currentPath)
        assertTrue(state.canGoUp)
        assertEquals(listOf("sunset.png"), state.visibleEntries.map { it.name })
    }

    @Test
    fun sort_by_size_descending_reorders_files() = runTest(dispatcher) {
        store.onIntent(FileBrowserIntent.SortChanged(SortOrder(SortField.SIZE, ascending = false)))
        val files = store.state.value.visibleEntries.filter { !it.isDirectory }.map { it.name }
        assertEquals(listOf("archive.zip", "notes.txt"), files)
    }

    @Test
    fun multi_select_then_delete_removes_entries_and_emits_message() = runTest(dispatcher) {
        store.effects.test {
            store.onIntent(FileBrowserIntent.ToggleSelect("/root/notes.txt"))
            store.onIntent(FileBrowserIntent.ToggleSelect("/root/archive.zip"))
            assertEquals(2, store.state.value.selection.size)

            store.onIntent(FileBrowserIntent.Delete(store.state.value.selection))

            val effect = awaitItem()
            assertTrue(effect is FileBrowserEffect.ShowMessage)
            val names = store.state.value.visibleEntries.map { it.name }
            assertFalse(names.contains("notes.txt"))
            assertFalse(names.contains("archive.zip"))
            assertTrue(store.state.value.selection.isEmpty())
        }
    }

    @Test
    fun create_folder_adds_entry_and_emits_message() = runTest(dispatcher) {
        store.effects.test {
            store.onIntent(FileBrowserIntent.CreateFolder("Documents"))
            assertTrue(awaitItem() is FileBrowserEffect.ShowMessage)
            assertTrue(store.state.value.visibleEntries.any { it.name == "Documents" && it.isDirectory })
        }
    }

    @Test
    fun blank_folder_name_emits_error() = runTest(dispatcher) {
        store.effects.test {
            store.onIntent(FileBrowserIntent.CreateFolder("   "))
            assertTrue(awaitItem() is FileBrowserEffect.ShowError)
        }
    }

    @Test
    fun rename_updates_entry_name() = runTest(dispatcher) {
        store.onIntent(FileBrowserIntent.Rename("/root/notes.txt", "renamed.txt"))
        assertTrue(store.state.value.visibleEntries.any { it.name == "renamed.txt" })
        assertFalse(store.state.value.visibleEntries.any { it.name == "notes.txt" })
    }

    @Test
    fun search_filters_to_matching_names_across_tree() = runTest(dispatcher) {
        store.onIntent(FileBrowserIntent.QueryChanged("sun"))
        assertTrue(store.state.value.isSearching)
        assertEquals(listOf("sunset.png"), store.state.value.visibleEntries.map { it.name })

        store.onIntent(FileBrowserIntent.ClearSearch)
        assertFalse(store.state.value.isSearching)
    }
}
