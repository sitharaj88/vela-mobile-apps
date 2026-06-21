/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.presentation.alarms

import androidx.lifecycle.viewModelScope
import com.vela.apps.clock.domain.Alarm
import com.vela.apps.clock.domain.AlarmRepository
import com.vela.apps.clock.domain.AlarmScheduler
import com.vela.apps.clock.domain.WeekDay
import com.vela.apps.clock.domain.normalizeRepeatDays
import com.vela.core.common.MviStore
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class AlarmsState(
    val alarms: PersistentList<Alarm> = persistentListOf(),
    val editor: AlarmDraft? = null,
)

/** Working copy of an alarm being created/edited in the bottom sheet/dialog. */
data class AlarmDraft(
    val id: Long = 0,
    val hour: Int = DEFAULT_HOUR,
    val minute: Int = 0,
    val label: String = "",
    val repeatDays: PersistentList<WeekDay> = persistentListOf(),
) {
    private companion object {
        const val DEFAULT_HOUR = 7
    }
}

sealed interface AlarmsIntent {
    data class ToggleEnabled(val id: Long, val enabled: Boolean) : AlarmsIntent
    data class Delete(val id: Long) : AlarmsIntent
    data object AddNew : AlarmsIntent
    data class Edit(val id: Long) : AlarmsIntent
    data object DismissEditor : AlarmsIntent
    data class DraftTime(val hour: Int, val minute: Int) : AlarmsIntent
    data class DraftLabel(val label: String) : AlarmsIntent
    data class ToggleDraftDay(val day: WeekDay) : AlarmsIntent
    data object SaveDraft : AlarmsIntent
}

/** Manages the alarm list and the create/edit draft; persists via [repository] and (re)schedules. */
class AlarmsStore(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler,
) : MviStore<AlarmsState, AlarmsIntent, Nothing>(AlarmsState()) {

    init {
        repository.observeAlarms()
            .onEach { alarms -> setState { copy(alarms = alarms.toPersistentList()) } }
            .launchIn(viewModelScope)
    }

    @Suppress("CyclomaticComplexMethod")
    override fun onIntent(intent: AlarmsIntent) {
        when (intent) {
            is AlarmsIntent.ToggleEnabled -> toggleEnabled(intent.id, intent.enabled)
            is AlarmsIntent.Delete -> delete(intent.id)
            AlarmsIntent.AddNew -> setState { copy(editor = AlarmDraft()) }
            is AlarmsIntent.Edit -> startEdit(intent.id)
            AlarmsIntent.DismissEditor -> setState { copy(editor = null) }
            is AlarmsIntent.DraftTime -> editDraft { copy(hour = intent.hour, minute = intent.minute) }
            is AlarmsIntent.DraftLabel -> editDraft { copy(label = intent.label) }
            is AlarmsIntent.ToggleDraftDay -> toggleDay(intent.day)
            AlarmsIntent.SaveDraft -> saveDraft()
        }
    }

    private fun toggleEnabled(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            repository.setEnabled(id, enabled)
            val alarm = currentState.alarms.firstOrNull { it.id == id } ?: return@launch
            val updated = alarm.copy(enabled = enabled)
            if (enabled) scheduler.schedule(updated) else scheduler.cancel(id)
        }
    }

    private fun delete(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
            scheduler.cancel(id)
        }
    }

    private fun startEdit(id: Long) {
        val alarm = currentState.alarms.firstOrNull { it.id == id } ?: return
        setState {
            copy(
                editor = AlarmDraft(
                    id = alarm.id,
                    hour = alarm.hour,
                    minute = alarm.minute,
                    label = alarm.label,
                    repeatDays = alarm.repeatDays,
                ),
            )
        }
    }

    private fun editDraft(transform: AlarmDraft.() -> AlarmDraft) {
        setState { copy(editor = editor?.transform()) }
    }

    private fun toggleDay(day: WeekDay) {
        editDraft {
            val next = if (day in repeatDays) repeatDays.remove(day) else repeatDays.add(day)
            copy(repeatDays = normalizeRepeatDays(next))
        }
    }

    private fun saveDraft() {
        val draft = currentState.editor ?: return
        val alarm = Alarm(
            id = draft.id,
            hour = draft.hour,
            minute = draft.minute,
            label = draft.label,
            enabled = true,
            repeatDays = draft.repeatDays,
        )
        setState { copy(editor = null) }
        viewModelScope.launch {
            val id = repository.save(alarm)
            scheduler.schedule(alarm.copy(id = id))
        }
    }
}
