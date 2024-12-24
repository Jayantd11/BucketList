package edu.vt.mobiledev.dreamcatcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DreamListViewModel : ViewModel() {
    private val dreamRepository = DreamRepository.get()
    private val _dreams: MutableStateFlow<List<Dream>> = MutableStateFlow(emptyList())
    val dreams get() = _dreams.asStateFlow()
    init {
        viewModelScope.launch {
            dreamRepository.getDreams().collect {
                _dreams.value = it
            }
        }
    }
    suspend fun addDream(dream: Dream) {
        dreamRepository.addDream(dream)
    }
    fun deleteDream(dream: Dream) {
        viewModelScope.launch {
            dreamRepository.deleteDream(dream)
        }
    }
}
