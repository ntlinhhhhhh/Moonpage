package com.diary.moonpage.ui.screens.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlarmOn
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.diary.moonpage.core.theme.MoonTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.*

@Composable
fun SleepRecordDialog(
    initialBedTime: LocalTime,
    initialWakeTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime, LocalTime) -> Unit
) {
    fun timeToAngle(t: LocalTime): Float {
        val totalMinutes = t.hour * 60f + t.minute
        return (totalMinutes / (24 * 60f)) * 360f - 90f
    }

    fun angleToTime(angleDeg: Float): LocalTime {
        val a = ((angleDeg + 90f) % 360f + 360f) % 360f
        val totalMinutes = ((a / 360f) * 24 * 60).toInt().coerceIn(0, 24 * 60 - 1)
        return LocalTime.of(totalMinutes / 60, totalMinutes % 60)
    }

    var bedAngle by remember { mutableStateOf(timeToAngle(initialBedTime)) }
    var wakeAngle by remember { mutableStateOf(timeToAngle(initialWakeTime)) }

    val bedTime = angleToTime(bedAngle)
    val wakeTime = angleToTime(wakeAngle)

    val bedMin = bedTime.hour * 60 + bedTime.minute
    val wakeMin = wakeTime.hour * 60 + wakeTime.minute
    val diffMin = if (wakeMin >= bedMin) wakeMin - bedMin else (24 * 60 - bedMin) + wakeMin
    val sleepH = diffMin / 60
    val sleepM = diffMin % 60

    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val fmt = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MoonTheme.customColors.popupBgColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Record sleep",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ---- Circular 24h Clock ----
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    val density = LocalDensity.current
                    val boxWidthPx = with(density) { maxWidth.toPx() }
                    val trackWidthDp = 38.dp
                    val trackWidthPx = with(density) { trackWidthDp.toPx() }
                    val handleRadiusDp = 20.dp
                    val handleRadiusPx = with(density) { handleRadiusDp.toPx() }

                    // Combined drag handler — determine which handle is closest
                    val dragModifier = Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { startPos ->
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val r = (min(size.width, size.height) / 2f) - trackWidthPx / 2f
                                val bedRad = Math.toRadians(bedAngle.toDouble())
                                val wakeRad = Math.toRadians(wakeAngle.toDouble())
                                val bedPos = Offset(cx + r * cos(bedRad).toFloat(), cy + r * sin(bedRad).toFloat())
                                val wakePos = Offset(cx + r * cos(wakeRad).toFloat(), cy + r * sin(wakeRad).toFloat())
                                val distBed = (startPos - bedPos).getDistance()
                                val distWake = (startPos - wakePos).getDistance()
                                // Store which is closer (use a side-effect variable)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val pos = change.position
                                val angle = atan2(pos.y - cy, pos.x - cx) * (180f / PI.toFloat())
                                val r = (min(size.width, size.height) / 2f) - trackWidthPx / 2f
                                val bedRad = Math.toRadians(bedAngle.toDouble())
                                val wakeRad = Math.toRadians(wakeAngle.toDouble())
                                val bedPos = Offset(cx + r * cos(bedRad).toFloat(), cy + r * sin(bedRad).toFloat())
                                val wakePos = Offset(cx + r * cos(wakeRad).toFloat(), cy + r * sin(wakeRad).toFloat())
                                val distBed = (pos - bedPos).getDistance()
                                val distWake = (pos - wakePos).getDistance()
                                if (distBed < distWake) bedAngle = angle else wakeAngle = angle
                            }
                        )
                    }

                    Canvas(
                        modifier = Modifier
                            .size(maxWidth)
                            .align(Alignment.Center)
                            .then(dragModifier)
                    ) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val r = (size.minDimension / 2f) - trackWidthPx / 2f
                        val stroke = Stroke(width = trackWidthPx, cap = StrokeCap.Round)

                        // Background track
                        drawArc(
                            color = surfaceVariantColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(cx - r, cy - r),
                            size = Size(r * 2, r * 2),
                            style = stroke
                        )

                        // Sleep arc
                        var sweep = ((wakeAngle - bedAngle) % 360f + 360f) % 360f
                        if (sweep == 0f) sweep = 360f
                        drawArc(
                            color = primaryColor,
                            startAngle = bedAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(cx - r, cy - r),
                            size = Size(r * 2, r * 2),
                            style = stroke
                        )

                        // Hour tick marks
                        for (h in 0 until 24) {
                            val a = Math.toRadians((h * 15.0) - 90.0)
                            val major = h % 6 == 0
                            val outerR = r + trackWidthPx / 2f + 4f
                            val innerR = outerR + if (major) 14f else 7f
                            drawLine(
                                color = onSurfaceColor.copy(alpha = if (major) 0.3f else 0.15f),
                                start = Offset(cx + outerR * cos(a).toFloat(), cy + outerR * sin(a).toFloat()),
                                end = Offset(cx + innerR * cos(a).toFloat(), cy + innerR * sin(a).toFloat()),
                                strokeWidth = if (major) 2.5f else 1.5f
                            )
                        }

                        // Bed handle (filled circle)
                        val bedRad = Math.toRadians(bedAngle.toDouble())
                        val bedX = cx + r * cos(bedRad).toFloat()
                        val bedY = cy + r * sin(bedRad).toFloat()
                        drawCircle(color = primaryColor, radius = handleRadiusPx, center = Offset(bedX, bedY))
                        drawCircle(color = onSurfaceColor.copy(alpha = 0.15f), radius = handleRadiusPx + 3f, center = Offset(bedX, bedY), style = Stroke(3f))

                        // Wake handle
                        val wakeRad = Math.toRadians(wakeAngle.toDouble())
                        val wakeX = cx + r * cos(wakeRad).toFloat()
                        val wakeY = cy + r * sin(wakeRad).toFloat()
                        drawCircle(color = primaryColor, radius = handleRadiusPx, center = Offset(wakeX, wakeY))
                        drawCircle(color = onSurfaceColor.copy(alpha = 0.15f), radius = handleRadiusPx + 3f, center = Offset(wakeX, wakeY), style = Stroke(3f))
                    }

                    // Center time display
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Time asleep", fontSize = 11.sp, color = onSurfaceColor.copy(alpha = 0.55f))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("$sleepH", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = onSurfaceColor)
                            Text(" h ", fontSize = 13.sp, color = onSurfaceColor.copy(alpha = 0.65f))
                            Text("$sleepM", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = onSurfaceColor)
                            Text(" m", fontSize = 13.sp, color = onSurfaceColor.copy(alpha = 0.65f))
                        }
                    }

                    // Bed icon on handle
                    val bedRadF = bedAngle.toDouble()
                    val r2px = boxWidthPx / 2f - trackWidthPx / 2f
                    val bedIconX = with(density) { (boxWidthPx / 2f + r2px * cos(Math.toRadians(bedRadF)).toFloat()).toDp() }
                    val bedIconY = with(density) { (boxWidthPx / 2f + r2px * sin(Math.toRadians(bedRadF)).toFloat()).toDp() }
                    Icon(
                        Icons.Rounded.Bedtime, contentDescription = null,
                        tint = onPrimaryColor,
                        modifier = Modifier
                            .size(17.dp)
                            .align(Alignment.TopStart)
                            .offset(x = bedIconX - 8.5.dp, y = bedIconY - 8.5.dp)
                    )

                    // Wake icon on handle
                    val wakeRadF = wakeAngle.toDouble()
                    val wakeIconX = with(density) { (boxWidthPx / 2f + r2px * cos(Math.toRadians(wakeRadF)).toFloat()).toDp() }
                    val wakeIconY = with(density) { (boxWidthPx / 2f + r2px * sin(Math.toRadians(wakeRadF)).toFloat()).toDp() }
                    Icon(
                        Icons.Rounded.AlarmOn, contentDescription = null,
                        tint = onPrimaryColor,
                        modifier = Modifier
                            .size(17.dp)
                            .align(Alignment.TopStart)
                            .offset(x = wakeIconX - 8.5.dp, y = wakeIconY - 8.5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Time display row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = surfaceVariantColor
                        ) {
                            Text(
                                bedTime.format(fmt),
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = onSurfaceColor,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Went to bed", fontSize = 11.sp, color = onSurfaceColor.copy(alpha = 0.6f))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = surfaceVariantColor
                        ) {
                            Text(
                                wakeTime.format(fmt),
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = onSurfaceColor,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Woke up", fontSize = 11.sp, color = onSurfaceColor.copy(alpha = 0.6f))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cancel / OK — themed with app colors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MoonTheme.customColors.cancelBtnBgColor,
                            contentColor = MoonTheme.customColors.cancelBtnTextColor
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Button(
                        onClick = { onConfirm(bedTime, wakeTime) },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = onPrimaryColor
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("OK", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
