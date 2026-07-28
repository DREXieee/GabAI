package com.gabai.domain.repository

import com.gabai.domain.model.ChatMessage
import com.gabai.domain.model.HistoryItem

data class HistoryPage(val items: List<HistoryItem>, val nextCursor: String?)

interface HistoryRepository {
    suspend fun getHistory(cursor: String?, limit: Int = 20): HistoryPage

    /** Read-only transcript for History's chat-session tap-through. */
    suspend fun getChatSessionTranscript(sessionId: String): List<ChatMessage>
}
