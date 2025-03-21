package edu.vt.mobiledev.dreamcatcher

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.vt.mobiledev.dreamcatcher.databinding.FragmentDreamListBinding
import kotlinx.coroutines.launch

private const val TAG = "DreamListFragment"

class DreamListFragment : Fragment() {

    private var _binding: FragmentDreamListBinding? = null
    private val binding get() = checkNotNull(_binding) { "Binding is null" }
    private val viewModel: DreamListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDreamListBinding.inflate(inflater, container, false)

        requireActivity().addMenuProvider(object : MenuProvider {
            // blank for now
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_dream_list, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.new_dream -> {
                        showNewDream()
                        true
                    }
                    else -> false
                }            }
        }, viewLifecycleOwner)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getItemTouchHelper().attachToRecyclerView(binding.dreamRecyclerView)
        binding.dreamRecyclerView.layoutManager = LinearLayoutManager(context)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dreams.collect { dreams ->
                    binding.dreamRecyclerView.adapter =
                        DreamListAdapter(dreams){ dreamId ->
                            findNavController().navigate(
                                DreamListFragmentDirections.showDreamDetail(dreamId)
                            )
                        }
                    if (dreams.isEmpty()) {
                        binding.noDreamText.visibility = View.VISIBLE
                        binding.noDreamAddButton.visibility = View.VISIBLE
                    } else {
                        binding.noDreamText.visibility = View.GONE
                        binding.noDreamAddButton.visibility = View.GONE
                    }

                }
            }
        }
        binding.noDreamAddButton.setOnClickListener {
            showNewDream()
        }
    }

    private fun showNewDream() {
        viewLifecycleOwner.lifecycleScope.launch {
            val newDream = Dream()
            viewModel.addDream(newDream)
            findNavController().navigate(
                DreamListFragmentDirections.showDreamDetail(newDream.id)
            )
        }
    }

    private fun getItemTouchHelper(): ItemTouchHelper {

        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = true

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // You must implement this on your own
                val dreamHolder = viewHolder as DreamHolder
                val dream = dreamHolder.boundDream
                viewModel.deleteDream(dream)
            }
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
