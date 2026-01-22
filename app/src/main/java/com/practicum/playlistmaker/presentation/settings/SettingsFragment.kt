package com.practicum.playlistmaker.presentation.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import com.practicum.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }

    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDarkThemeSwitch(view)

        view.findViewById<MaterialTextView>(R.id.contact_support_button).setOnClickListener {
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

        view.findViewById<MaterialTextView>(R.id.share_app_button).setOnClickListener {
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

        view.findViewById<MaterialTextView>(R.id.user_agreement_button).setOnClickListener {
            val url = getString(R.string.practicum_offer_url)
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(intent)
        }
    }

    private fun setupDarkThemeSwitch(view: View) {
        val switchDarkTheme = view.findViewById<SwitchMaterial>(R.id.switch_dark_theme)

        val isDark = viewModel.isDarkTheme()
        if (switchDarkTheme.isChecked != isDark) {
            switchDarkTheme.isChecked = isDark
        }

        switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDarkTheme(isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }
}