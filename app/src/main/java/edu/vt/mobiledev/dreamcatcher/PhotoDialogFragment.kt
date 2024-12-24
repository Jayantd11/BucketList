package edu.vt.mobiledev.dreamcatcher

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.view.doOnLayout
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import edu.vt.mobiledev.dreamcatcher.databinding.FragmentPhotoDialogBinding
import java.io.File
private const val TAG = "PhotoDialogFragment"

class PhotoDialogFragment : DialogFragment() {

    private val args: PhotoDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding = FragmentPhotoDialogBinding.inflate(layoutInflater)

        val photoFileName = args.dreamPhotoFilename
        val photoFile = File(requireContext().applicationContext.filesDir, photoFileName)

        if (photoFile.exists())
            binding.root.doOnLayout { measuredView -> //verify measured view values
                val scaledBitmap = getScaledBitmap(photoFile.path, measuredView.width, measuredView.height)
                binding.photoDetail.setImageBitmap(scaledBitmap)
                binding.photoDetail.tag = photoFileName
            }
        else {
            binding.photoDetail.setImageBitmap(null)
            binding.photoDetail.tag = null
        }

            return AlertDialog.Builder(requireContext())
                .setView(binding.root)
                .show()
        }
}