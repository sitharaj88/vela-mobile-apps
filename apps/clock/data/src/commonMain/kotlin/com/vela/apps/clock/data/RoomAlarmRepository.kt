/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.data

import com.vela.apps.clock.data.local.AlarmDao
import com.vela.apps.clock.data.local.AlarmEntity
import com.vela.apps.clock.domain.Alarm
import com.vela.apps.clock.domain.AlarmRepository
import com.vela.apps.clock.domain.WeekDay
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Room-backed [AlarmRepository]; maps between the Room entity and the domain model. */
class RoomAlarmRepository(
    private val dao: AlarmDao,
) : AlarmRepository {

    override fun observeAlarms(): Flow<List<Alarm>> =
        dao.observe().map { rows -> rows.map(AlarmEntity::toDomain) }

    override suspend fun save(alarm: Alarm): Long = dao.upsert(alarm.toEntity())

    override suspend fun setEnabled(alarmId: Long, enabled: Boolean) = dao.setEnabled(alarmId, enabled)

    override suspend fun delete(alarmId: Long) = dao.deleteById(alarmId)
}

internal fun AlarmEntity.toDomain(): Alarm = Alarm(
    id = id,
    hour = hour,
    minute = minute,
    label = label,
    enabled = enabled,
    repeatDays = decodeRepeatDays(repeatDaysCsv),
)

internal fun Alarm.toEntity(): AlarmEntity = AlarmEntity(
    id = id,
    hour = hour,
    minute = minute,
    label = label,
    enabled = enabled,
    repeatDaysCsv = encodeRepeatDays(repeatDays),
)

internal fun encodeRepeatDays(days: List<WeekDay>): String =
    days.joinToString(",") { it.ordinal.toString() }

internal fun decodeRepeatDays(csv: String) =
    if (csv.isBlank()) {
        persistentListOf()
    } else {
        csv.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it in WeekDay.entries.indices }
            .map { WeekDay.entries[it] }
            .distinct()
            .sortedBy { it.ordinal }
            .toPersistentList()
    }
