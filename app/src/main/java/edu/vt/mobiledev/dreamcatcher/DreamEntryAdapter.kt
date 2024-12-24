package edu.vt.mobiledev.dreamcatcher

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.vt.mobiledev.dreamcatcher.databinding.ListItemDreamEntryBinding

class DreamEntryHolder(private val binding: ListItemDreamEntryBinding) : RecyclerView.ViewHolder(binding.root) {

    lateinit var boundEntry: DreamEntry

    @SuppressLint("SetTextI18n")
    fun bind(entry: DreamEntry) {
        boundEntry = entry
        binding.entry1Button.apply {
            visibility = View.VISIBLE
            text = when (entry.kind) {
                DreamEntryKind.CONCEIVED -> {
                    setBackgroundWithContrastingText("red")
                    "CONCEIVED"

                }

                DreamEntryKind.REFLECTION -> {
                    isAllCaps = false
                    setBackgroundWithContrastingText("purple")
                    entry.text
                }

                DreamEntryKind.DEFERRED -> {
                    setBackgroundWithContrastingText("yellow")
                    "DEFERRED"
                }

                DreamEntryKind.FULFILLED -> {
                    setBackgroundWithContrastingText("teal")
                    "FULFILLED"
                }
            }
        }
    }
}


    class DreamEntryAdapter(
        private val entries: List<DreamEntry>
    ) : RecyclerView.Adapter<DreamEntryHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DreamEntryHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ListItemDreamEntryBinding.inflate(inflater, parent, false)
            return DreamEntryHolder(binding)
        }

        override fun onBindViewHolder(holder: DreamEntryHolder, position: Int) {
            val entry = entries[position]
            holder.bind(entry)
        }

        override fun getItemCount(): Int {
            return entries.size
        }
    }

