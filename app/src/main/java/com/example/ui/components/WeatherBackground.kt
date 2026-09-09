package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.model.WeatherType

@Composable
fun WeatherBackground(
    weatherCode: Int,
    isDay: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val condition = WeatherType.fromWmo(weatherCode, isDay)
    val targetColors = if (isDay) condition.backgroundGradientDay else condition.backgroundGradientNight

    val color1 by animateColorAsState(
        targetValue = targetColors.firstOrNull() ?: Color(0xFF1E3C72),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "bg_color1"
    )
    val color2 by animateColorAsState(
        targetValue = targetColors.getOrNull(1) ?: Color(0xFF2A5298),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "bg_color2"
    )
    val color3 by animateColorAsState(
        targetValue = targetColors.lastOrNull() ?: Color(0xFF141E30),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "bg_color3"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(color1, color2, color3)
                )
            )
    ) {
        // Atmospheric subtle light bloom/canvas decoration
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            if (isDay) {
                // Gentle sun aura
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x33FFE082), Color.Transparent),
                        center = Offset(canvasWidth * 0.8f, canvasHeight * 0.15f),
                        radius = canvasWidth * 0.7f
                    ),
                    center = Offset(canvasWidth * 0.8f, canvasHeight * 0.15f),
                    radius = canvasWidth * 0.7f
                )
            } else {
                // Night soft moonlight glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x2280D8FF), Color.Transparent),
                        center = Offset(canvasWidth * 0.75f, canvasHeight * 0.12f),
                        radius = canvasWidth * 0.5f
                    ),
                    center = Offset(canvasWidth * 0.75f, canvasHeight * 0.12f),
                    radius = canvasWidth * 0.5f
                )
            }
        }

        content()
    }
}
