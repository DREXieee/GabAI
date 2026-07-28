package com.gabai.data.remote.api

import com.gabai.data.remote.models.HistoryItemDto
import com.gabai.domain.model.ChatMessage
import com.gabai.domain.model.CrisisLevel
import com.gabai.domain.model.HistoryItem
import com.gabai.domain.repository.HistoryPage
import com.gabai.domain.repository.HistoryRepository

class RemoteHistoryRepositoryImpl(private val apiService: ApiService) : HistoryRepository {

    override suspend fun getHistory(cursor: String?, limit: Int): HistoryPage {
        val response = apiService.getHistory(cursor, limit)
        return HistoryPage(items = response.items.mapNotNull { it.toDomain() }, nextCursor = response.nextCursor)
    }

    override suspend fun getChatSessionTranscript(sessionId: String): List<ChatMessage> {
        return apiService.getChatSessionDetail(sessionId).messages.map { message ->
            ChatMessage(
                id = message.id,
                text = message.text,
                isUser = message.sender == "USER",
                crisisLevel = runCatching { CrisisLevel.valueOf(message.crisisLevel) }.getOrDefault(CrisisLevel.NONE)
            )
        }
    }
}

private fun HistoryItemDto.toDomain(): HistoryItem? = when (type) {
    "CHAT_SESSION" -> chatSessionId?.let {
        HistoryItem.ChatSessionItem(
            date = date,
            chatSessionId = it,
            messageCount = messageCount ?: 0,
            lastMessagePreview = lastMessagePreview,
            hadEscalation = hadEscalation ?: false
        )
    }

    "ESCALATION" -> escalationId?.let {
        HistoryItem.EscalationItem(
            date = date,
            escalationId = it,
            crisisLevel = runCatching { CrisisLevel.valueOf(crisisLevel ?: "NONE") }.getOrDefault(CrisisLevel.NONE),
            status = escalationStatus ?: "PENDING",
            claimedByStaffName = claimedByStaffName
        )
    }

    "CONSULTATION" -> consultationId?.let {
        HistoryItem.ConsultationItem(
            date = date,
            consultationId = it,
            status = consultationStatus ?: "CONFIRMED",
            doctorName = doctorName ?: "Doctor",
            slotStartTime = slotStartTime ?: date,
            slotEndTime = slotEndTime ?: date
        )
    }

    else -> null
}
