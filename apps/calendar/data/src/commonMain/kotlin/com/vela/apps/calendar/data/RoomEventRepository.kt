/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.data

import com.vela.apps.calendar.data.local.EventDao
import com.vela.apps.calendar.data.local.EventEntity
import com.vela.apps.calendar.domain.model.Event
import com.vela.apps.calendar.domain.model.Recurrence
import com.vela.apps.calendar.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Room-backed [EventRepository]; maps between the Room entity and the domain model. */
class RoomEventRepository(
    private val dao: EventDao,
) : EventRepository {

    override fun observeInRange(startMillis: Long, endMillis: Long): Flow<List<Event>> =
        dao.observeInRange(startMillis, endMillis).map { rows -> rows.map(EventEntity::toDomain) }

    override fun observeAll(): Flow<List<Event>> =
        dao.observeAll().map { rows -> rows.map(EventEntity::toDomain) }

    override fun observeEvent(id: Long): Flow<Event?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun getEvent(id: Long): Event? = dao.getById(id)?.toDomain()

    override suspend fun save(event: Event): Long = dao.upsert(event.toEntity())

    override suspend fun delete(id: Long) = dao.deleteById(id)
}

private fun EventEntity.toDomain() = Event(
    id = id,
    title = title,
    description = description,
    location = location,
    startMillis = startMillis,
    endMillis = endMillis,
    allDay = allDay,
    colorIndex = colorIndex,
    recurrence = recurrence.toRecurrence(),
    reminderMinutes = reminderMinutes,
)

private fun Event.toEntity() = EventEntity(
    id = id,
    title = title,
    description = description,
    location = location,
    startMillis = startMillis,
    endMillis = endMillis,
    allDay = allDay,
    colorIndex = colorIndex,
    recurrence = recurrence.name,
    reminderMinutes = reminderMinutes,
)

private fun String.toRecurrence(): Recurrence =
    Recurrence.entries.firstOrNull { it.name == this } ?: Recurrence.NONE
