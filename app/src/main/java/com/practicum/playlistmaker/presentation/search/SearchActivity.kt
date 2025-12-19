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
import androidx.lifecycle.ViewModelProvider
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.presentation.Creator
import com.practicum.playlistmaker.presentation.audio.AudioPlayerActivity


class SearchActivity : AppCompatActivity() {

    companion object {
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

    private lateinit var viewModel: SearchViewModel

    private val clickHandler = Handler(Looper.getMainLooper())
    private var isClickAllowed = true

    private var searchText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_search)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setPadding(view.paddingLeft, statusBar.top, view.paddingRight, view.paddingBottom)
            insets
        }

        viewModel = ViewModelProvider(
            this,
            Creator.provideSearchViewModelFactory(this)
        ).get(SearchViewModel::class.java)

        progressBar = findViewById(R.id.progressBar)

        historySection = findViewById(R.id.history_section)
        historyTitle = findViewById(R.id.ys_medium_1)
        clearHistoryBtn = findViewById(R.id.button1)
        historyRecycler = findViewById(R.id.history_recycler)
        historyRecycler.layoutManager = LinearLayoutManager(this)
        historyAdapter = TrackAdapter(mutableListOf()) { track ->
            if (clickDebounce()) {
                viewModel.onTrackClicked(track)
                openPlayer(track)
            }
        }
        historyRecycler.adapter = historyAdapter

        clearHistoryBtn.setOnClickListener {
            viewModel.onClearHistoryClicked()
        }

        errorImage = findViewById(R.id.il_internet)
        errorText = findViewById(R.id.tv_placeholder_error)
        retryButton = findViewById(R.id.btn_retry)
        retryButton.setOnClickListener {
            viewModel.onRetryClicked()
        }

        initViews()
        setupSearchLogic()

        placeholderImage = findViewById(R.id.il_search)
        placeholderText = findViewById(R.id.tv_placeholder_empty)

        recycler = findViewById(R.id.rvTracks)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = TrackAdapter(mutableListOf()) { track ->
            if (clickDebounce()) {
                viewModel.onTrackClicked(track)
                openPlayer(track)
            }
        }
        recycler.adapter = adapter

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is SearchScreenState.Idle -> {
                    progressBar.visibility = View.GONE
                    recycler.visibility = View.GONE
                    historySection.visibility = View.GONE
                    hidePlaceholders()
                    hideError()
                }
                is SearchScreenState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                    historySection.visibility = View.GONE
                    hidePlaceholders()
                    hideError()
                }
                is SearchScreenState.History -> {
                    progressBar.visibility = View.GONE
                    historySection.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                    hidePlaceholders()
                    hideError()
                    historyAdapter.setItems(state.tracks)
                }
                is SearchScreenState.Content -> {
                    progressBar.visibility = View.GONE
                    recycler.visibility = View.VISIBLE
                    historySection.visibility = View.GONE
                    hidePlaceholders()
                    hideError()
                    adapter.setItems(state.tracks)
                }
                is SearchScreenState.Empty -> {
                    progressBar.visibility = View.GONE
                    recycler.visibility = View.GONE
                    historySection.visibility = View.GONE
                    showEmptyPlaceholder()
                    hideError()
                }
                is SearchScreenState.Error -> {
                    progressBar.visibility = View.GONE
                    recycler.visibility = View.GONE
                    historySection.visibility = View.GONE
                    hidePlaceholders()
                    showError()
                }
            }
        }
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
                viewModel.onQueryChanged(searchText)
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        searchEditText.addTextChangedListener(searchTextWatcher)

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.onSearchButtonClicked(searchText)
                true
            } else false
        }

        clearButton.setOnClickListener {
            viewModel.onClearClicked()
            searchEditText.setText("")
            searchEditText.clearFocus()
            searchText = ""
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
        }
    }

    private fun clickDebounce(): Boolean {
        val allowed = isClickAllowed
        if (allowed) {
            isClickAllowed = false
            clickHandler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return allowed
    }

    private fun openPlayer(track: Track) {
        startActivity(
            Intent(this, AudioPlayerActivity::class.java).putExtra(
                AudioPlayerActivity.EXTRA_TRACK,
                track
            )
        )
    }
}