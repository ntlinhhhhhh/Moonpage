package com.diary.moonpage.ui.tutorial

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

data class TutorialController(
    val activeStep: TutorialStep? = null,
    val onTargetBoundsChanged: (TutorialStep, Rect) -> Unit = { _, _ -> },
    val onStepCompleted: (TutorialStep) -> Unit = {}
)

val LocalTutorialController = compositionLocalOf { TutorialController() }

fun LayoutCoordinates.tutorialBounds(): Rect = boundsInRoot()

fun Modifier.tutorialTarget(step: TutorialStep, enabled: Boolean = true): Modifier = composed {
    val controller = LocalTutorialController.current
    if (enabled && controller.activeStep == step) {
        this.onGloballyPositioned { coordinates ->
            controller.onTargetBoundsChanged(step, coordinates.tutorialBounds())
        }
    } else {
        this
    }
}
