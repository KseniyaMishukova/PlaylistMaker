package com.practicum.playlistmaker

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
 import androidx.recyclerview.widget.LinearLayoutManager
 import androidx.recyclerview.widget.RecyclerView


 class SearchActivity : AppCompatActivity() {

     private lateinit var searchEditText: EditText
     private lateinit var clearButton: ImageView
     private lateinit var backButton: ImageView


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

         initViews()
         setupSearchLogic()

         val recycler = findViewById<RecyclerView>(R.id.rvTracks)
         recycler.layoutManager = LinearLayoutManager(this)
         val data = createMockTracks()
         recycler.adapter = TrackAdapter(data)
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

         clearButton.setOnClickListener {
             searchEditText.setText("")
             searchEditText.clearFocus()

             searchText = ""

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

     private fun createMockTracks(): ArrayList<Track> = arrayListOf(
         Track("Smells Like Teen Spirit", "Nirvana", "5:01", "https://is5-ssl.mzstatic.com/image/thumb/Music115/v4/7b/58/c2/7b58c21a-2b51-2bb2-e59a-9bb9b96ad8c3/00602567924166.rgb.jpg/100x100bb.jpg"),
         Track("Billie Jean", "Michael Jackson", "4:35", "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/3d/9d/38/3d9d3811-71f0-3a0e-1ada-3004e56ff852/827969428726.jpg/100x100bb.jpg"),
         Track("Stayin' Alive", "Bee Gees", "4:10", "https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/1f/80/1f/1f801fc1-8c0f-ea3e-d3e5-387c6619619e/16UMGIM86640.rgb.jpg/100x100bb.jpg"),
         Track("Whole Lotta Love", "Led Zeppelin", "5:33", "https://is2-ssl.mzstatic.com/image/thumb/Music62/v4/7e/17/e3/7e17e33f-2efa-2a36-e916-7f808576cf6b/mzm.fyigqcbs.jpg/100x100bb.jpg"),
         Track("Sweet Child O'Mine", "Guns N' Roses", "5:03", "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg")
     )

 }