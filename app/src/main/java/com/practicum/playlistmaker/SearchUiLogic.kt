package com.practicum.playlistmaker

object SearchUiLogic {

    fun shouldShowHistory(
        hasFocus: Boolean,
        queryText: CharSequence?,
        historyIsEmpty: Boolean
    ): Boolean {
        return hasFocus && (queryText.isNullOrEmpty()) && !historyIsEmpty
    }
}