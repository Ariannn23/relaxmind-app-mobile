package com.relaxmind.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val SkeletonBase = Color(0xFFE9EDF2)
private val SkeletonHighlight = Color(0xFFF8FAFC)

@Composable
fun ShimmerEffect(): Modifier {
    val transition = rememberInfiniteTransition(label = "relax-shimmer")
    val offset = transition.animateFloat(
        initialValue = -450f,
        targetValue = 950f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "relax-shimmer-offset"
    )

    return Modifier.background(
        brush = Brush.linearGradient(
            colors = listOf(SkeletonBase, SkeletonHighlight, SkeletonBase),
            start = androidx.compose.ui.geometry.Offset(offset.value, 0f),
            end = androidx.compose.ui.geometry.Offset(offset.value + 420f, 420f)
        )
    )
}

@Composable
fun DashboardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        SkeletonLine(widthFraction = 0.48f, height = 30)
        SkeletonLine(widthFraction = 0.34f, height = 14)
        SkeletonBlock(height = 150)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SkeletonBlock(modifier = Modifier.weight(1f), height = 132)
            SkeletonBlock(modifier = Modifier.weight(1f), height = 132)
        }
        repeat(3) {
            SkeletonBlock(height = 88)
        }
    }
}

@Composable
fun PatientListSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        repeat(4) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .then(ShimmerEffect())
                    .padding(18.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .then(ShimmerEffect())
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SkeletonLine(widthFraction = 0.58f, height = 16)
                    SkeletonLine(widthFraction = 0.42f, height = 12)
                    SkeletonLine(widthFraction = 0.32f, height = 22)
                }
            }
        }
    }
}

@Composable
fun ProgressCalendarSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SkeletonLine(widthFraction = 0.42f, height = 24)
        repeat(5) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(7) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .then(ShimmerEffect())
                    )
                }
            }
        }
    }
}

@Composable
private fun SkeletonBlock(
    modifier: Modifier = Modifier,
    height: Int
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(26.dp))
            .then(ShimmerEffect())
    )
}

@Composable
private fun SkeletonLine(
    widthFraction: Float,
    height: Int
) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height.dp)
            .clip(RoundedCornerShape(50))
            .then(ShimmerEffect())
    )
}
