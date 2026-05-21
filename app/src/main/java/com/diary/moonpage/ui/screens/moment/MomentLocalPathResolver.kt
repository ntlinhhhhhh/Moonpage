package com.diary.moonpage.ui.screens.moment

import com.diary.moonpage.domain.model.Moment

internal fun resolveMomentLocalPath(moment: Moment, localPaths: Map<String, String>): String? {
    return localPaths[moment.id] ?: localPaths[moment.imageUrl]
}
