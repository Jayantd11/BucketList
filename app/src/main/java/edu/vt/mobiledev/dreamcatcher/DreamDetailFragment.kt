package edu.vt.mobiledev.dreamcatcher

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.core.view.doOnLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.mobiledev.dreamcatcher.databinding.FragmentDreamDetailBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.File.separator

private const val TAG = "DreamDetailFragment"

class DreamDetailFragment : Fragment() {

    private var _binding: FragmentDreamDetailBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    private val args: DreamDetailFragmentArgs by navArgs()
    private val viewModel: DreamDetailViewModel by viewModels {
        DreamDetailViewModelFactory(
            args.dreamId
        )
    }

    private var isUpdatingUI = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDreamDetailBinding.inflate(inflater, container, false)
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_dream_detail, menu)
                val captureImageIntent = takePhoto.contract.createIntent(
                    requireContext(),
                    Uri.EMPTY // NOTE: The "null" used in BNRG is obsolete now
                )
                menu.findItem(R.id.take_photo_menu).isVisible = canResolveIntent(captureImageIntent)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when(menuItem.itemId) {
                    R.id.share_dream_menu -> {

                        viewModel.dream.value?.let { shareDream(it) }
                        return true
                    }
                    R.id.take_photo_menu -> {
                        viewModel.dream.value?.let {
                            val photoFile = File(
                                requireContext().applicationContext.filesDir,
                                it.photoFileName
                            )
                            val photoUri = FileProvider.getUriForFile(
                                requireContext(),
                                "edu.vt.cs5254.dreamcatcher.fileprovider",
                                photoFile
                            )
                            takePhoto.launch(photoUri)
                        }
                        true
                    }
                    else -> {
                        return false
                    }
                }
            }
        }, viewLifecycleOwner)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getItemTouchHelper().attachToRecyclerView(binding.dreamEntryRecycler)

        binding.dreamEntryRecycler.layoutManager = LinearLayoutManager(context)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dream.collect { dream ->
                    dream?.let {
                        Log.d(TAG, "Dream entries before updating UI: ${dream.entries}")
                        updateUI(dream)
                        binding.dreamEntryRecycler.adapter = DreamEntryAdapter(it.entries)

                    }
                }
            }
        }

        setFragmentResultListener(
            ReflectionDialogFragment.REQUEST_KEY
        ) { _, bundle ->
            val newref = bundle.getString(ReflectionDialogFragment.BUNDLE_KEY)
            if (newref != null) {
                viewModel.updateDream { oldDream ->
                    oldDream.copy().apply {
                        entries = oldDream.entries + DreamEntry(
                            kind = DreamEntryKind.REFLECTION,
                            text = newref,
                            dreamId = oldDream.id
                        )
                    }
                }
                Log.d(TAG, "Added Reflection Entry: $newref")
            }
        }

        binding.fulfilledCheckbox.setOnClickListener {
            if (!isUpdatingUI) {
                val isChecked = binding.fulfilledCheckbox.isChecked
                viewModel.updateDream { oldDream ->
                    oldDream.copy().apply {
                        entries = if (isChecked) {
                            oldDream.entries + DreamEntry(
                                kind = DreamEntryKind.FULFILLED,
                                dreamId = oldDream.id
                            )
                        } else {
                            oldDream.entries.filter { it.kind != DreamEntryKind.FULFILLED }
                        }
                    }
                }
            }
        }

        binding.deferredCheckbox.setOnClickListener {
            val isChecked = binding.deferredCheckbox.isChecked
            viewModel.updateDream { oldDream ->
                oldDream.copy().apply {
                    entries = if (isChecked) {
                        oldDream.entries + DreamEntry(
                            kind = DreamEntryKind.DEFERRED,
                            dreamId = oldDream.id
                        )
                    } else {
                        oldDream.entries.filter { it.kind != DreamEntryKind.DEFERRED }
                    }
                }
            }
        }

        binding.titleText.doOnTextChanged { text, _, _, _ ->
            if (!isUpdatingUI) {
                viewModel.updateDream { oldDream ->
                    if (oldDream.title != text.toString()) {
                        ////////checkkkkkkkkkkkkkkkkkkkkkkk
                        oldDream.copy(title = text.toString()).apply { entries = oldDream.entries }
                    } else {
                        oldDream
                    }
                }
            }
        }

        binding.addReflectionButton.setOnClickListener {
            findNavController()
                .navigate(
                    DreamDetailFragmentDirections.addReflection()
                )
        }
        binding.dreamPhoto.setOnClickListener {
            val photoTag = binding.dreamPhoto.tag?.toString()
            if (!photoTag.isNullOrEmpty()) {
                findNavController().navigate(
                    DreamDetailFragmentDirections.showPhotoDetail(photoTag)
                )
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun updateUI(dream: Dream) {
        updatePhoto(dream)

        isUpdatingUI = true

        binding.lastUpdatedText.text = DateFormat.format(
            "'Last updated' yyyy-MM-dd 'at' hh:mm:ss a",
            dream.lastUpdated
        )
        if (binding.titleText.text.toString() != dream.title) {
            binding.titleText.setText(dream.title)
        }
        Log.d(TAG, "Entries in updateUI:")
        dream.entries.forEach { entry ->
            Log.d(TAG, "Kind: ${entry.kind}, Text: ${entry.text}")
        }

        binding.fulfilledCheckbox.isChecked = dream.isFulfilled
        binding.deferredCheckbox.isChecked = dream.isDeferred
        binding.fulfilledCheckbox.isEnabled = !dream.isDeferred
        binding.deferredCheckbox.isEnabled = !dream.isFulfilled

        if (dream.isFulfilled) {
            binding.addReflectionButton.hide()
        } else {
            binding.addReflectionButton.show()
        }

       // updateButtons(dream.entries)

        isUpdatingUI = false
    }
    private fun updatePhoto(dream: Dream) {
        with(binding.dreamPhoto) {
            if (tag != dream.photoFileName) {
                val photoFile =
                    File(requireContext().applicationContext.filesDir, dream.photoFileName)
                if (photoFile.exists()) {
                    this.doOnLayout { measuredView ->
                        val scaledBM = getScaledBitmap(
                            photoFile.path,
                            measuredView.width,
                            measuredView.height
                        )
                        setImageBitmap(scaledBM)
                        tag = dream.photoFileName

                        isEnabled = true
                    }
                } else {
                    this.setImageBitmap(null)
                    this.tag = null

                    isEnabled = false
                }
            }
        }
    }
//
//    private fun updateButtons(entries: List<DreamEntry>) {
//        val buttonList = listOf(
//            binding.entry0Button,
//            binding.entry1Button,
//            binding.entry2Button,
//            binding.entry3Button,
//            binding.entry4Button
//        )
//
//        buttonList.forEach { button ->
//            button.visibility = View.GONE
//            button.setOnClickListener(null)
//        }
//
//        entries.take(5).zip(buttonList).forEach { (entry, button) ->
//            button.visibility = View.VISIBLE
//            when (entry.kind) {
//                DreamEntryKind.CONCEIVED -> {
//                    button.text = "CONCEIVED"
//                    button.setBackgroundWithContrastingText("red")
//                }
//                DreamEntryKind.REFLECTION -> {
//                    button.text = entry.text
//                    button.isAllCaps = false
//                    button.setBackgroundWithContrastingText("purple")
//                }
//                DreamEntryKind.DEFERRED -> {
//                    button.text = "DEFERRED"
//                    button.setBackgroundWithContrastingText("yellow")
//                }
//                DreamEntryKind.FULFILLED -> {
//                    button.text = "FULFILLED"
//                    button.setBackgroundWithContrastingText("teal")
//                }
//            }
//        }
//    }
    fun shareDream(dream: Dream){

        val dateFormat = DateFormat.format("'Last updated' yyyy-MM-dd 'at' hh:mm:ss A",
            dream.lastUpdated).toString()
        val reflections = dream.entries.filter { it.kind == DreamEntryKind.REFLECTION }
            .takeIf{it.isNotEmpty() }
            ?.joinToString(separator = "\n * ", prefix = " * ") { it.text }
            ?:""
        val status = when {
            dream.entries.any { it.kind == DreamEntryKind.DEFERRED } -> "This dream has been Deferred."
            dream.entries.any { it.kind == DreamEntryKind.FULFILLED } -> "This dream has been Fulfilled."
            else -> null
        }
        val reflection = "Reflections:"

        val dreamText = listOfNotNull(
            dream.title,
            dateFormat,
            reflection.takeIf { reflections.isNotEmpty() },
            reflections,
            status
        ).joinToString (separator  = "\n")

        val reportIntent: Intent = Intent().apply{
            type = "text/plain"
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, dreamText)
        }
        val chooserIntent = Intent.createChooser(reportIntent, null)
    context?.startActivity(chooserIntent)
    }
    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { didTakePhoto: Boolean ->
        if (didTakePhoto) {
            binding.dreamPhoto.tag = null
            viewModel.dream.value?.let { updatePhoto(it) }
        }
    }
    private fun canResolveIntent(intent: Intent): Boolean {
        val packageManager: PackageManager = requireActivity().packageManager
        val resolvedActivity: ResolveInfo? =
            packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        return resolvedActivity != null
    }

    private fun getItemTouchHelper(): ItemTouchHelper {

        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, 0) {

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val entryHolder = viewHolder as DreamEntryHolder
                val entry = entryHolder.boundEntry
                return if (entry.kind == DreamEntryKind.REFLECTION) {
                    ItemTouchHelper.LEFT
                } else {
                    0
                }
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false


            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val entryHolder = viewHolder as DreamEntryHolder
                val entry = entryHolder.boundEntry
                viewModel.updateDream { oldDream ->
                    oldDream.copy().apply {
                        entries = oldDream.entries.filter { it != entry }

                    }
                }
            }
        })
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}

