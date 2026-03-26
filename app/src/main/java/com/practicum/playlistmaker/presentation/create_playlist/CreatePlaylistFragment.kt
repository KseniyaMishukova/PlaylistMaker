package com.practicum.playlistmaker.presentation.create_playlist

import android.content.res.Configuration
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doAfterTextChanged
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.presentation.main.MainActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

open class CreatePlaylistFragment : Fragment() {

    protected open val editorViewModel: CreatePlaylistViewModel by viewModel()

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        editorViewModel.setCoverUri(uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_create_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(view)
        setupCoverPicker(view)
        setupInputs(view)
        setupInputsTheme(view)
        setupCreateButton(view)
        setupBackPressed()
        observeState(view)
    }

    private fun setupToolbar(view: View) {
        view.findViewById<View>(R.id.back_button).setOnClickListener {
            editorViewModel.onBackPressed()
        }
    }

    private fun setupCoverPicker(view: View) {
        view.findViewById<View>(R.id.cover_container).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun setupInputs(view: View) {
        view.findViewById<TextInputEditText>(R.id.name_input).doAfterTextChanged {
            editorViewModel.setName(it?.toString() ?: "")
        }
        view.findViewById<TextInputEditText>(R.id.description_input).doAfterTextChanged {
            editorViewModel.setDescription(it?.toString() ?: "")
        }
    }

    private fun setupInputsTheme(view: View) {
        val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val hintColor: Int
        val borderColor: Int
        val textColor: Int
        if (isDark) {
            hintColor = 0xFFFFFFFF.toInt()
            borderColor = 0xFFFFFFFF.toInt()
            textColor = 0xFFFFFFFF.toInt()
        } else {
            hintColor = Color.parseColor("#1A1B22")
            borderColor = Color.parseColor("#AEAFB4")
            textColor = Color.parseColor("#1A1B22")
        }

        val fullColorStateList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf(android.R.attr.state_hovered), intArrayOf()),
            intArrayOf(hintColor, hintColor, hintColor)
        )
        val borderColorStateList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf(android.R.attr.state_hovered), intArrayOf()),
            intArrayOf(borderColor, borderColor, borderColor)
        )

        listOf(R.id.name_input_layout, R.id.description_input_layout).forEach { id ->
            view.findViewById<TextInputLayout>(id).apply {
                setHintTextColor(fullColorStateList)
                setBoxStrokeColorStateList(borderColorStateList)
            }
        }
        view.findViewById<TextInputEditText>(R.id.name_input).apply {
            setTextColor(textColor)
            setHintTextColor(hintColor)
        }
        view.findViewById<TextInputEditText>(R.id.description_input).apply {
            setTextColor(textColor)
            setHintTextColor(hintColor)
        }
    }

    private fun setupCreateButton(view: View) {
        view.findViewById<android.widget.Button>(R.id.create_button).setOnClickListener {
            editorViewModel.submitPlaylist(coverUri = editorViewModel.state.value?.coverUri)
        }
    }

    private fun setupBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    editorViewModel.onBackPressed()
                }
            }
        )
    }

    private fun observeState(view: View) {
        val nameInput = view.findViewById<TextInputEditText>(R.id.name_input)
        val descriptionInput = view.findViewById<TextInputEditText>(R.id.description_input)
        editorViewModel.state.observe(viewLifecycleOwner) { state ->
            state ?: return@observe
            if (nameInput.text.isNullOrEmpty() && state.name.isNotEmpty()) nameInput.setText(state.name)
            if (descriptionInput.text.isNullOrEmpty() && state.description.isNotEmpty()) descriptionInput.setText(state.description)
            view.findViewById<android.widget.Button>(R.id.create_button).isEnabled = state.isCreateEnabled
            updateCover(view, state.coverUri)
            if (state.showBackDialog) {
                showBackDialog()
            }
            when (val action = state.pendingAction) {
                PendingAction.NavigateBack -> {
                    editorViewModel.clearPendingAction()
                    findNavController().navigateUp()
                }
                is PendingAction.PlaylistCreated -> {
                    editorViewModel.clearPendingAction()
                    showPlaylistCreatedToast(getString(R.string.playlist_created, action.playlistName))
                    findNavController().navigateUp()
                }
                null -> {}
            }
        }
    }

    private fun updateCover(view: View, uri: Uri?) {
        val coverBorder = view.findViewById<View>(R.id.cover_border)
        val coverImage = view.findViewById<android.widget.ImageView>(R.id.cover_image)
        val coverPlaceholder = view.findViewById<android.widget.ImageView>(R.id.cover_placeholder)
        val dm = resources.displayMetrics
        if (uri != null) {
            coverBorder.visibility = View.GONE
            coverImage.visibility = View.VISIBLE
            coverPlaceholder.visibility = View.GONE
            val radius = resources.getDimensionPixelSize(R.dimen.playlist_cover_corner_radius)
            Glide.with(this)
                .load(uri)
                .centerCrop()
                .transform(RoundedCorners(radius))
                .into(coverImage)
        } else {
            coverBorder.background = DashedBorderDrawable(
                color = Color.parseColor("#AEAFB4"),
                strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, dm),
                cornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, dm)
            )
            coverBorder.visibility = View.VISIBLE
            coverImage.visibility = View.GONE
            coverPlaceholder.visibility = View.VISIBLE
        }
    }

    private fun showBackDialog() {
        editorViewModel.dismissBackDialog()
        MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_FinishPlaylistDialog)
            .setTitle(R.string.finish_dialog_title)
            .setMessage(R.string.finish_dialog_message)
            .setPositiveButton(R.string.finish) { _, _ ->
                editorViewModel.confirmBack()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setOnDismissListener {
                editorViewModel.dismissBackDialog()
            }
            .show()
    }

    private fun showPlaylistCreatedToast(message: String) {
        val activity = requireActivity()
        val content = activity.findViewById<ViewGroup>(android.R.id.content)

        val toast = LayoutInflater.from(requireContext()).inflate(R.layout.view_playlist_created_toast, content, false)
        toast.findViewById<TextView>(R.id.toast_text).text = message
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM
            bottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30f, resources.displayMetrics).toInt()
        }
        content.addView(toast, params)

        toast.postDelayed({
            (activity as? MainActivity)?.syncBottomNavigationWithNavDestination()
        }, 100L)
        toast.postDelayed({
            content.removeView(toast)
            (activity as? MainActivity)?.syncBottomNavigationWithNavDestination()
        }, 2000L)
    }
}
