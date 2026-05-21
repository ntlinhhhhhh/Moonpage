package com.diary.moonpage.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.moonpage.core.util.normalizeAppImageUrl
import com.diary.moonpage.domain.repository.DailyLogRepository
import com.diary.moonpage.domain.repository.MomentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

data class GalleryPhotoItem(
    val id: String,
    val imageUrl: String,
    val localPath: String? = null,
    val sortInstant: Instant,
    val momentId: String? = null,
    val dailyLogDate: String? = null
)

data class GalleryUiState(
    val items: List<GalleryPhotoItem> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val momentRepository: MomentRepository,
    private val dailyLogRepository: DailyLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            momentRepository.getMyMoments()
            combine(
                momentRepository.moments,
                momentRepository.localPaths,
                dailyLogRepository.getAllDailyLogsFlow()
            ) { moments, localPaths, dailyLogs ->
                val momentItems = moments.mapNotNull { moment ->
                    val imageUrl = normalizeAppImageUrl(moment.imageUrl) ?: return@mapNotNull null
                    GalleryPhotoItem(
                        id = "moment_${moment.id}",
                        imageUrl = imageUrl,
                        localPath = localPaths[moment.id] ?: localPaths[moment.imageUrl],
                        sortInstant = runCatching { Instant.parse(moment.capturedAt) }.getOrElse { Instant.EPOCH },
                        momentId = moment.id
                    )
                }

                val dailyLogItems = dailyLogs.flatMap { log ->
                    val sortInstant = runCatching {
                        log.createdAt?.let(Instant::parse)
                            ?: LocalDate.parse(log.date).atTime(23, 59, 59).toInstant(ZoneOffset.UTC)
                    }.getOrElse { Instant.EPOCH }

                    log.dailyPhotos.orEmpty().mapIndexedNotNull { index, photoUrl ->
                        val normalizedUrl = normalizeAppImageUrl(photoUrl) ?: return@mapIndexedNotNull null
                        GalleryPhotoItem(
                            id = "dailylog_${log.id}_$index",
                            imageUrl = normalizedUrl,
                            sortInstant = sortInstant,
                            dailyLogDate = log.date
                        )
                    }
                }

                (momentItems + dailyLogItems)
                    .distinctBy { it.imageUrl }
                    .sortedByDescending { it.sortInstant }
            }.collect { items ->
                _uiState.update { it.copy(items = items, isLoading = false) }
            }
        }
    }
}
