package com.practicum.playlistmaker.presentation.search

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    companion object {
        fun newInstance(): SearchFragment {
            return SearchFragment()
        }
    }

    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageView

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

    private val viewModel: SearchViewModel by viewModel()

    private val clickHandler = Handler(Looper.getMainLooper())
    private var isClickAllowed = true

    private var searchText: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)

        historySection = view.findViewById(R.id.history_section)
        historyTitle = view.findViewById(R.id.ys_medium_1)
        clearHistoryBtn = view.findViewById(R.id.button1)
        historyRecycler = view.findViewById(R.id.history_recycler)
        historyRecycler.layoutManager = LinearLayoutManager(requireContext())
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

        errorImage = view.findViewById(R.id.il_internet)
        errorText = view.findViewById(R.id.tv_placeholder_error)
        retryButton = view.findViewById(R.id.btn_retry)
        retryButton.setOnClickListener {
            viewModel.onRetryClicked()
        }

        initViews(view)
        setupSearchLogic()

        placeholderImage = view.findViewById(R.id.il_search)
        placeholderText = view.findViewById(R.id.tv_placeholder_empty)

        recycler = view.findViewById(R.id.rvTracks)
        recycler.layoutManager = LinearLayoutManager(requireContext())
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
        viewModel.state.observe(viewLifecycleOwner) { state ->
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

    private fun initViews(view: View) {
        searchEditText = view.findViewById(R.id.search_edit_text)
        clearButton = view.findViewById(R.id.clear_button)
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
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
        }
    }

    private fun clickDebounce(): Boolean {
        val allowed = isClickAllowed
        if (allowed) {
            isClickAllowed = false
            clickHandler.postDelayed({ isClickAllowed = true }, 1000L)
        }
        return allowed
    }

    private fun openPlayer(track: Track) {
        findNavController().navigate(
            R.id.audioPlayerFragment,
            Bundle().apply {
                putSerializable("arg_track", track)
            }
        )
    }
}