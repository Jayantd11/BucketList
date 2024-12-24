package edu.vt.mobiledev.dreamcatcher

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.vt.mobiledev.dreamcatcher.databinding.ListItemDreamBinding
import java.util.UUID

class DreamHolder(val binding: ListItemDreamBinding) : RecyclerView.ViewHolder(binding.root)
{
    lateinit var boundDream: Dream
        private set
    @SuppressLint("SetTextI18n")
    fun bind(dream: Dream, onDreamClicked: (dreamId: UUID) -> Unit) {
        boundDream = dream

        binding.root.setOnClickListener {
            Log.d("NavigationDebug", "Clicked on dream with ID: ${dream.id.toString()}")
            onDreamClicked(dream.id)
        }
        binding.listItemTitle.text = dream.title

        val iconResId = when {
            dream.isFulfilled -> R.drawable.ic_dream_fulfilled
            dream.isDeferred -> R.drawable.ic_dream_deferred
            else -> 0
        }
        if (iconResId != 0) {
            binding.listItemImage.setImageResource(iconResId)
            binding.listItemImage.visibility = View.VISIBLE
        } else {
            binding.listItemImage.visibility = View.GONE
        }
        binding.listItemReflectionCount.text = "Reflections: ${dream.entries.count { it.kind == DreamEntryKind.REFLECTION }}"
    }
}

class DreamListAdapter(private val dreams: List<Dream>, private val onDreamClicked:(dreamId: UUID) -> Unit
) : RecyclerView.Adapter<DreamHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DreamHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemDreamBinding.inflate(inflater, parent, false)
        return DreamHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DreamHolder, position: Int) {
        val dream = dreams[position]
        holder.bind(dream, onDreamClicked)

        }


    override fun getItemCount(): Int {
        return dreams.size
    }
}
