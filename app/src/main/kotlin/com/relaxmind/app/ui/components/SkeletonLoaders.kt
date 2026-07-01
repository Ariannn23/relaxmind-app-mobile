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
import androidx.compose.ui.Alignment
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

@Composable
fun MeditateSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SkeletonLine(widthFraction = 0.48f, height = 40)
        SkeletonLine(widthFraction = 0.78f, height = 18)
        Spacer(modifier = Modifier.height(18.dp))

        SkeletonBlock(height = 104, modifier = Modifier.clip(RoundedCornerShape(32.dp)))

        repeat(6) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .then(ShimmerEffect())
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .then(ShimmerEffect())
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SkeletonLine(widthFraction = 0.68f, height = 20)
                    SkeletonLine(widthFraction = 0.44f, height = 14)
                }
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .then(ShimmerEffect())
                )
            }
        }
    }
}

@Composable
fun ScheduleSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SkeletonLine(widthFraction = 0.46f, height = 40)
                SkeletonLine(widthFraction = 0.70f, height = 16)
            }
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .then(ShimmerEffect())
            )
        }
        SkeletonBlock(height = 54, modifier = Modifier.clip(RoundedCornerShape(18.dp)))
        SkeletonBlock(height = 128, modifier = Modifier.clip(RoundedCornerShape(28.dp)))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .then(ShimmerEffect())
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonLine(widthFraction = 0.56f, height = 22)
                SkeletonLine(widthFraction = 0.42f, height = 14)
            }
        }
        repeat(3) {
            SkeletonBlock(height = 86, modifier = Modifier.clip(RoundedCornerShape(24.dp)))
        }
    }
}

@Composable
fun ProgressScreenSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SkeletonLine(widthFraction = 0.50f, height = 40)
        SkeletonLine(widthFraction = 0.62f, height = 16)
        SkeletonBlock(height = 132, modifier = Modifier.clip(RoundedCornerShape(28.dp)))
        SkeletonBlock(height = 360, modifier = Modifier.clip(RoundedCornerShape(30.dp)))
        SkeletonLine(widthFraction = 0.32f, height = 28)
        SkeletonBlock(height = 92, modifier = Modifier.clip(RoundedCornerShape(24.dp)))
        SkeletonLine(widthFraction = 0.34f, height = 28)
        repeat(3) {
            SkeletonBlock(height = 88, modifier = Modifier.clip(RoundedCornerShape(24.dp)))
        }
    }
}

@Composable
fun SettingsSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonBlock(modifier = Modifier.size(72.dp).clip(CircleShape), height = 72)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonLine(widthFraction = 0.5f, height = 24)
                SkeletonLine(widthFraction = 0.3f, height = 16)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        repeat(5) {
            SkeletonBlock(height = 64)
        }
    }
}

@Composable
fun AlertsHistorySkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        repeat(5) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .then(ShimmerEffect())
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .then(ShimmerEffect())
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    SkeletonLine(widthFraction = 0.76f, height = 18)
                    SkeletonLine(widthFraction = 0.48f, height = 14)
                }
                Box(
                    modifier = Modifier
                        .width(86.dp)
                        .height(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .then(ShimmerEffect())
                )
            }
        }
    }
}

@Composable
fun CaregiverDashboardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Card
        SkeletonBlock(height = 200, modifier = Modifier.clip(RoundedCornerShape(32.dp)))
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Horizontal Scroll Area
        SkeletonLine(widthFraction = 0.4f, height = 24)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SkeletonBlock(modifier = Modifier.weight(1f), height = 180)
            SkeletonBlock(modifier = Modifier.weight(1f), height = 180)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Alerts
        SkeletonLine(widthFraction = 0.4f, height = 24)
        repeat(3) {
            SkeletonBlock(height = 90, modifier = Modifier.clip(RoundedCornerShape(20.dp)))
        }
    }
}
