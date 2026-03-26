package com.practicum.playlistmaker.presentation.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.models.Track
import com.practicum.playlistmaker.presentation.audio.AudioPlayerFragment
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

    private lateinit var historyScroll: View
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

        historyScroll = view.findViewById(R.id.history_scroll)
        historySection = view.findViewById(R.id.history_section)
        historyTitle = view.findViewById(R.id.ys_medium_1)
        clearHistoryBtn = view.findViewById(R.id.button1)
        historyRecycler = view.findViewById(R.id.history_recycler)
        historyRecycler.layoutManager = LinearLayoutManager(requireContext())
        historyAdapter = TrackAdapter(mutableListOf(), useRowClickListener = false)
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
        adapter = TrackAdapter(mutableListOf(), useRowClickListener = false)
        recycler.adapter = adapter

        attachTrackListOpensPlayer(historyRecycler, historyAdapter)
        attachTrackListOpensPlayer(recycler, adapter)

        observeViewModel()
    }

    /**
     * Стандартный itemView.setOnClickListener на эмуляторе с открытой IME часто не срабатывает.
     * Один тап обрабатываем через GestureDetector + позиция строки; на DOWN снимаем фокус с поля поиска.
     */
    private fun attachTrackListOpensPlayer(list: RecyclerView, listAdapter: TrackAdapter) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        @Suppress("DEPRECATION")
        val gestureDetector = GestureDetector(
            requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    val under = list.findChildViewUnder(e.x, e.y) ?: return false
                    val row = list.findContainingItemView(under) ?: return false
                    val pos = list.getChildAdapterPosition(row)
                    if (pos == RecyclerView.NO_POSITION) return false
                    val track = listAdapter.getTrackAt(pos) ?: return false
                    viewModel.onTrackClicked(track)
                    openPlayer(track)
                    return true
                }
            }
        )
        list.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                if (e.actionMasked == MotionEvent.ACTION_DOWN) {
                    searchEditText.clearFocus()
                    imm.hideSoftInputFromWindow(rv.windowToken, 0)
                }
                gestureDetector.onTouchEvent(e)
                return false
            }
        })
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchScreenState.Idle -> {
                    progressBar.visibility = View.GONE
                    recycler.visibility = View.GONE
                    historyScroll.visibility = View.GONE
                    historySection.visibility = View.GONE
                    hidePlaceholders()
                    hideError()
                }
                is SearchScreenState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                    historyScroll.visibility = View.GONE
                    historySection.visibility = View.GONE
                    hidePlaceholders()
                    hideError()
                }
                is SearchScreenState.History -> {
                    progressBar.visibility = View.GONE
                    historyScroll.visibility = View.VISIBLE
                    historySection.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                    hidePlaceholders()
                    hideError()
                    historyAdapter.setItems(state.tracks)
                }
                is SearchScreenState.Content -> {
                    progressBar.visibility = View.GONE
                    recycler.visibility = View.VISIBLE
                    historyScroll.visibility = View.GONE
                    historySection.visibility = View.GONE
                    hidePlaceholders()
                    hideError()
                    adapter.setItems(state.tracks)
                }
                is SearchScreenState.Empty -> {
                    progressBar.visibility = View.GONE
                    recycler.visibility = View.GONE
                    historyScroll.visibility = View.GONE
                    historySection.visibility = View.GONE
                    showEmptyPlaceholder()
                    hideError()
                }
                is SearchScreenState.Error -> {
                    progressBar.visibility = View.GONE
                    recycler.visibility = View.GONE
                    historyScroll.visibility = View.GONE
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

    private fun openPlayer(track: Track) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        val args = Bundle().apply {
            putSerializable(AudioPlayerFragment.ARG_TRACK, track)
        }
        if (!isAdded) return
        val navHost =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHost.navController.navigate(R.id.audioPlayerFragment, args)
    }
}