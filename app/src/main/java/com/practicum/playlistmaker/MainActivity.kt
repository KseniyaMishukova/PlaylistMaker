 package com.practicum.playlistmaker

 import android.content.Intent
 import android.os.Bundle
 import androidx.appcompat.app.AppCompatActivity
 import androidx.core.view.WindowCompat
 import androidx.core.view.ViewCompat
 import androidx.core.view.WindowInsetsCompat
 import com.google.android.material.button.MaterialButton

 class MainActivity : AppCompatActivity() {
     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)

         WindowCompat.setDecorFitsSystemWindows(window, false)
         setContentView(R.layout.activity_main)

         ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
             val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
             view.setPadding(view.paddingLeft, statusBar.top, view.paddingRight, view.paddingBottom)
             insets
         }

         val searchCard = findViewById<MaterialButton>(R.id.card_search)
         val mediaCard = findViewById<MaterialButton>(R.id.card_media)
         val settingsCard = findViewById<MaterialButton>(R.id.card_settings)

         searchCard.setOnClickListener {
             val intent = Intent(this, SearchActivity::class.java)
             startActivity(intent)
         }

         mediaCard.setOnClickListener {
             val intent = Intent(this, MediaLibraryActivity::class.java)
             startActivity(intent)
         }

         settingsCard.setOnClickListener {
             val intent = Intent(this, SettingsActivity::class.java)
             startActivity(intent)
         }
     }
 }