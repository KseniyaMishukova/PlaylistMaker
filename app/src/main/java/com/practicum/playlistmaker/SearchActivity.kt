package com.practicum.playlistmaker

 import android.content.Context
 import android.os.Bundle
 import android.text.Editable
 import android.text.TextWatcher
 import android.view.View
 import android.view.inputmethod.InputMethodManager
 import android.widget.EditText
 import android.widget.TextView
 import android.widget.ImageView
 import androidx.appcompat.app.AppCompatActivity
 import androidx.core.view.WindowCompat
 import androidx.core.view.ViewCompat
 import androidx.core.view.WindowInsetsCompat

 class SearchActivity : AppCompatActivity() {

     private lateinit var searchEditText: EditText
     private lateinit var clearButton: TextView
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
 }