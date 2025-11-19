package com.practicum.playlistmaker.presentation.search

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.inputmethod.EditorInfo
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.domain.usecase.HistoryInteractor
import com.practicum.playlistmaker.domain.usecase.SearchInteractor
import com.practicum.playlistmaker.presentation.Creator
import com.practicum.playlistmaker.presentation.audio.AudioPlayerActivity
import com.practicum.playlistmaker.presentation.adapter.TrackAdapter

class SearchActivity : AppCompatActivity() {

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var backButton: ImageView

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: TrackAdapter

    private lateinit var historySection: View
    private lateinit var historyTitle: TextView
    private lateinit var clearHistoryBtn: View
    private lateinit var historyRecycler: RecyclerView
    private lateinit var historyAdapter: TrackAdapter

    private lateinit var placeholderImage: View
    private lateinit var placeholderText: View

    private lateinit var errorImage: View
    private lateinit var errorText: View
    private lateinit var retryButton: View

    private lateinit var progressBar: View

    private var lastQuery: String = ""
    private var searchText: String = ""

    private lateinit var searchInteractor: SearchInteractor
    private lateinit var historyInteractor: HistoryInteractor
    private var showHistoryFlag: Boolean = false

    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private val clickHandler = Handler(Looper.getMainLooper())
    private var isClickAllowed = true
    private var isLoading = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_search)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setPadding(view.paddingLeft, statusBar.top, view.paddingRight, view.paddingBottom)
            insets
        }

        searchInteractor = Creator.provideSearchInteractor()
        historyInteractor = Creator.provideHistoryInteractor(this)

        progressBar = findViewById(R.id.progressBar)

        historySection = findViewById(R.id.history_section)
        historyTitle = findViewById(R.id.ys_medium_1)
        clearHistoryBtn = findViewById(R.id.button1)
        historyRecycler = findViewById(R.id.history_recycler)
        historyRecycler.layoutManager = LinearLayoutManager(this)
        historyAdapter = TrackAdapter(mutableListOf()) { track ->
            if (clickDebounce()) {
                historyInteractor.addTrack(track)
                startActivity(
                    Intent(this, AudioPlayerActivity::class.java).putExtra(
                        AudioPlayerActivity.EXTRA_TRACK,
                        track
                    )
                )
            }
        }
        historyRecycler.adapter = historyAdapter

        clearHistoryBtn.setOnClickListener {
            historyInteractor.clearHistory()
            updateHistoryVisibility(false)
        }

        errorImage = findViewById(R.id.il_internet)
        errorText = findViewById(R.id.tv_placeholder_error)
        retryButton = findViewById(R.id.btn_retry)
        retryButton.setOnClickListener {
            if (lastQuery.isNotEmpty()) performSearch(lastQuery)
        }

        initViews()
        setupSearchLogic()

        placeholderImage = findViewById(R.id.il_search)
        placeholderText = findViewById(R.id.tv_placeholder_empty)

        recycler = findViewById(R.id.rvTracks)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = TrackAdapter(mutableListOf()) { track ->
            if (clickDebounce()) {
                historyInteractor.addTrack(track)
                startActivity(
                    Intent(this, AudioPlayerActivity::class.java).putExtra(
                        AudioPlayerActivity.EXTRA_TRACK,
                        track
                    )
                )
            }
        }
        recycler.adapter = adapter

        updateHistoryVisibility(false)

        searchEditText.requestFocus()
        showHistoryFlag = shouldShowHistory(
            hasFocus = true,
            queryText = searchEditText.text,
            historyIsEmpty = historyInteractor.getHistory().isEmpty()
        )
        updateHistoryVisibility(showHistoryFlag)
        renderHistory(historyInteractor.getHistory())
    }

    private fun showEmptyPlaceholder() {
        recycler.visibility = View.GONE
        placeholderImage.visibility = View.VISIBLE
        placeholderText.visibility = View.VISIBLE
    }

    private fun showError() {
        recycler.visibility = View.GONE
        placeholderImage.visibility = View.GONE
        placeholderText.visibility = View.GONE
        errorImage.visibility = View.VISIBLE
        errorText.visibility = View.VISIBLE
        retryButton.visibility = View.VISIBLE
    }

    private fun hideError() {
        errorImage.visibility = View.GONE
        errorText.visibility = View.GONE
        retryButton.visibility = View.GONE
    }

    private fun hidePlaceholders() {
        placeholderImage.visibility = View.GONE
        placeholderText.visibility = View.GONE
    }

    private fun setLoading(loading: Boolean) {
        isLoading = loading
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        if (loading) {
            hideError()
            hidePlaceholders()
            recycler.visibility = View.GONE
            updateHistoryVisibility(false)
        }
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) return
        lastQuery = query

        setLoading(true)
        searchInteractor.searchTracks(query) { result ->
            setLoading(false)
            result.onSuccess { tracks ->
                hideError()
                if (tracks.isEmpty()) {
                    adapter.setItems(emptyList())
                    showEmptyPlaceholder()
                } else {
                    adapter.setItems(tracks)
                    hidePlaceholders()
                    recycler.visibility = View.VISIBLE
                    updateHistoryVisibility(false)
                }
            }.onFailure {
                adapter.setItems(emptyList())
                showError()
            }
        }
    }

    private fun initViews() {
        searchEditText = findViewById(R.id.search_edit_text)
        clearButton = findViewById(R.id.clear_button)
        backButton = findViewById(R.id.back_button)
        backButton.setOnClickListener { finish() }
    }

    private fun setupSearchLogic() {
        val searchTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                searchText = s?.toString() ?: ""

                debounceSearch(searchText.trim())

                showHistoryFlag = shouldShowHistory(
                    hasFocus = searchEditText.hasFocus(),
                    queryText = s,
                    historyIsEmpty = historyInteractor.getHistory().isEmpty()
                )
                updateHistoryVisibility(showHistoryFlag)
                if (showHistoryFlag) {
                    renderHistory(historyInteractor.getHistory())
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        searchEditText.addTextChangedListener(searchTextWatcher)

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchHandler.removeCallbacksAndMessages(null)
                searchRunnable = null
                performSearch(searchText.trim())
                true
            } else false
        }

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            showHistoryFlag = shouldShowHistory(
                hasFocus = hasFocus,
                queryText = searchEditText.text,
                historyIsEmpty = historyInteractor.getHistory().isEmpty()
            )
            updateHistoryVisibility(showHistoryFlag)
            if (showHistoryFlag) {
                renderHistory(historyInteractor.getHistory())
            }
        }

        clearButton.setOnClickListener {
            searchHandler.removeCallbacksAndMessages(null)
            searchRunnable = null

            searchEditText.setText("")
            searchEditText.clearFocus()
            searchText = ""
            lastQuery = ""
            adapter.setItems(emptyList())
            recycler.visibility = View.GONE
            hidePlaceholders()
            hideError()
            updateHistoryVisibility(false)
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
        }
    }

    private fun debounceSearch(query: String) {
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
        if (query.isEmpty()) return
        if (query == lastQuery && !isLoading) return
        val runnable = Runnable { performSearch(query) }
        searchRunnable = runnable
        searchHandler.postDelayed(runnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun clickDebounce(): Boolean {
        val allowed = isClickAllowed
        if (allowed) {
            isClickAllowed = false
            clickHandler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return allowed
    }

    private fun updateHistoryVisibility(visible: Boolean) {
        historySection.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible) recycler.visibility = View.GONE
    }

    private fun renderHistory(list: List<Track>) {
        historyAdapter.setItems(list)
        if (list.isEmpty()) historySection.visibility = View.GONE
    }

    private fun shouldShowHistory(
        hasFocus: Boolean,
        queryText: CharSequence?,
        historyIsEmpty: Boolean
    ): Boolean {
        return hasFocus && (queryText.isNullOrEmpty()) && !historyIsEmpty
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("search_text", searchText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val restoredText = savedInstanceState.getString("search_text", "")
        searchText = restoredText
        searchEditText.setText(restoredText)
    }

    override fun onDestroy() {
        super.onDestroy()
        searchHandler.removeCallbacksAndMessages(null)
        clickHandler.removeCallbacksAndMessages(null)
    }
}