package com.practicum.playlistmaker

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.inputmethod.EditorInfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
 import android.content.Context
 import android.os.Bundle
 import android.text.Editable
 import android.text.TextWatcher
 import android.view.View
 import android.view.inputmethod.InputMethodManager
 import android.widget.EditText
 import android.widget.ImageView
 import androidx.appcompat.app.AppCompatActivity
 import androidx.core.view.WindowCompat
 import androidx.core.view.ViewCompat
 import androidx.core.view.WindowInsetsCompat


 class SearchActivity : AppCompatActivity() {

     private lateinit var searchEditText: EditText
     private lateinit var clearButton: ImageView
     private lateinit var backButton: ImageView

     private lateinit var recycler: RecyclerView
     private lateinit var adapter: TrackAdapter

     private lateinit var placeholderImage: View
     private lateinit var placeholderText: View

     private lateinit var errorImage: View
     private lateinit var errorText: View
     private lateinit var retryButton: View
     private var lastQuery: String = ""
     private var searchText: String = ""

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         WindowCompat.setDecorFitsSystemWindows(window, false)
         setContentView(R.layout.activity_search)

         errorImage = findViewById(R.id.il_internet)
         errorText = findViewById(R.id.tv_placeholder_error)
         retryButton = findViewById(R.id.btn_retry)
         retryButton.setOnClickListener {
             if (lastQuery.isNotEmpty()) performSearch(lastQuery)
         }
         ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
             val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
             view.setPadding(view.paddingLeft, statusBar.top, view.paddingRight, view.paddingBottom)
             insets
         }

         initViews()
         setupSearchLogic()

         placeholderImage = findViewById(R.id.il_search)
         placeholderText = findViewById(R.id.tv_placeholder_empty)

         recycler = findViewById<RecyclerView>(R.id.rvTracks)
         recycler.layoutManager = LinearLayoutManager(this)
         adapter = TrackAdapter(mutableListOf())
         recycler.adapter = adapter
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
     private fun performSearch(query: String) {
         if (query.isEmpty()) return
         lastQuery = query
         RetrofitProvider.api.search(query).enqueue(object : Callback<TracksResponse> {
             override fun onResponse(call: Call<TracksResponse>, response: Response<TracksResponse>) {
                 if (response.isSuccessful) {
                     val tracks = response.body()?.results?.map { it.toDomain() }.orEmpty()
                     hideError()
                     if (tracks.isEmpty()) {
                         adapter.setItems(emptyList())
                         showEmptyPlaceholder()
                     } else {
                         adapter.setItems(tracks)
                         hidePlaceholders()
                         recycler.visibility = View.VISIBLE
                     }
                 } else {
                     adapter.setItems(emptyList())
                     showError()
                 }
             }
             override fun onFailure(call: Call<TracksResponse>, t: Throwable) {
                 adapter.setItems(emptyList())
                 showError()
             }
         })
     }


     private fun initViews() {
         searchEditText = findViewById(R.id.search_edit_text)
         clearButton = findViewById(R.id.clear_button)
         backButton = findViewById(R.id.back_button)

         backButton.setOnClickListener {
             finish()
         }
     }

     private fun setupSearchLogic() {
         val searchTextWatcher = object : TextWatcher {
             override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

             override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                 clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                 searchText = s?.toString() ?: ""
             }

             override fun afterTextChanged(s: Editable?) {}
         }

         searchEditText.addTextChangedListener(searchTextWatcher)

         searchEditText.setOnEditorActionListener { _, actionId, _ ->
             if (actionId == EditorInfo.IME_ACTION_DONE) {
                 performSearch(searchText.trim())
                 true
             } else {
                 false
             }
         }

         clearButton.setOnClickListener {
             searchEditText.setText("")
             searchEditText.clearFocus()
             searchText = ""
             lastQuery = ""
             adapter.setItems(emptyList())
             recycler.visibility = View.GONE
             hidePlaceholders()
             hideError()
             val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
             imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
         }
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


 }