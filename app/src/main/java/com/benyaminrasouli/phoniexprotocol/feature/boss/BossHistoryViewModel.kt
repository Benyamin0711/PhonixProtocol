package com.benyaminrasouli.phoniexprotocol.feature.boss

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benyaminrasouli.phoniexprotocol.core.data.db.entity.Boss
import com.benyaminrasouli.phoniexprotocol.core.domain.repository.BossRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class BossFilter { ALL, ACTIVE, COMPLETED, FAILED }

data class BossHistoryState(
    val bosses: List<Boss> = emptyList(),
    val filter: BossFilter = BossFilter.ALL
)

@HiltViewModel
class BossHistoryViewModel @Inject constructor(
    private val bossRepository: BossRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(BossFilter.ALL)
    private val _state = MutableStateFlow(BossHistoryState())
    val state: StateFlow<BossHistoryState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                bossRepository.getAllBosses(),
                _filter
            ) { bosses, filter ->
                val filtered = when (filter) {
                    BossFilter.ALL -> bosses
                    BossFilter.ACTIVE -> bosses.filter { it.status == "ACTIVE" }
                    BossFilter.COMPLETED -> bosses.filter { it.status == "COMPLETED" }
                    BossFilter.FAILED -> bosses.filter { it.status == "FAILED" }
                }
                BossHistoryState(bosses = filtered, filter = filter)
            }.collect { _state.value = it }
        }
    }

    fun setFilter(filter: BossFilter) {
        _filter.value = filter
    }
}
