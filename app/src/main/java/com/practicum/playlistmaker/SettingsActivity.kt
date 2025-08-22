package com.practicum.playlistmaker


 import android.content.Intent
 import android.os.Bundle
 import android.widget.ImageView
 import androidx.appcompat.app.AppCompatActivity
 import androidx.appcompat.app.AppCompatDelegate
 import androidx.appcompat.widget.Toolbar
 import androidx.core.net.toUri
 import androidx.core.view.ViewCompat
 import androidx.core.view.WindowCompat
 import androidx.core.view.WindowInsetsCompat
 import com.google.android.material.switchmaterial.SwitchMaterial
 import com.google.android.material.textview.MaterialTextView

 class SettingsActivity : AppCompatActivity() {
     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)

         WindowCompat.setDecorFitsSystemWindows(window, false)
         setContentView(R.layout.activity_settings)

         ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
             val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
             view.setPadding(view.paddingLeft, statusBar.top, view.paddingRight, view.paddingBottom)
             insets
         }

         val toolbar = findViewById<Toolbar>(R.id.toolbar)
         setSupportActionBar(toolbar)


         findViewById<ImageView>(R.id.back_button).setOnClickListener {
             finish()
         }


         setupDarkThemeSwitch()

         findViewById<MaterialTextView>(R.id.contact_support_button).setOnClickListener {
             val email = getString(R.string.support_email)
             val subject = getString(R.string.email_subject)
             val body = getString(R.string.email_body)

             val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                 data = "mailto:".toUri()
                 putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                 putExtra(Intent.EXTRA_SUBJECT, subject)
                 putExtra(Intent.EXTRA_TEXT, body)
             }
             startActivity(emailIntent)
         }

         findViewById<MaterialTextView>(R.id.share_app_button).setOnClickListener {
             val shareText = getString(
                R.string.share_message_text,
                getString(R.string.practicum_android_course_url)
            )
             val sendIntent = Intent(Intent.ACTION_SEND).apply {
                 type = "text/plain"
                 putExtra(Intent.EXTRA_TEXT, shareText)
             }
             startActivity(Intent.createChooser(sendIntent, getString(R.string.share_app)))
         }

         findViewById<MaterialTextView>(R.id.user_agreement_button).setOnClickListener {
             val url = getString(R.string.practicum_offer_url)
             val intent = Intent(Intent.ACTION_VIEW, url.toUri())
             startActivity(intent)
         }
     }

     private fun setupDarkThemeSwitch() {
         val switchDarkTheme = findViewById<SwitchMaterial>(R.id.switch_dark_theme)


         switchDarkTheme.isChecked = isDarkThemeEnabled()


         switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->
             if (isChecked) {
                 enableDarkTheme()
             } else {
                 enableLightTheme()
             }
         }
     }


     private fun isDarkThemeEnabled(): Boolean {

         return AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
     }

     private fun enableDarkTheme() {
         AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
     }

     private fun enableLightTheme() {
         AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
     }
 }