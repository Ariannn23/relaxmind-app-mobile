package com.relaxmind.app.features.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.ButtonVariant
import com.relaxmind.app.ui.components.RelaxButton
import com.relaxmind.app.ui.themes.BackgroundLight
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.RelaxMindTheme
import com.relaxmind.app.utils.OnboardingPreferences
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Slide data
// ---------------------------------------------------------------------------

private data class OnboardingSlide(
    val title: String,
    val description: String,
    val illustrationColor: Color
)

private val slides = listOf(
    OnboardingSlide(
        title = "Tu bienestar, cada día",
        description = "Registra cómo te sientes y construye hábitos saludables paso a paso.",
        illustrationColor = Color(0xFFB2DFDB)   // soft teal
    ),
    OnboardingSlide(
        title = "Mindfulness y respiración",
        description = "Ejercicios guiados para calmar tu mente cuando más lo necesitas.",
        illustrationColor = Color(0xFFC8E6C9)   // soft green
    ),
    OnboardingSlide(
        title = "Siempre acompañado",
        description = "Tu cuidador siempre conectado para estar ahí cuando lo necesites.",
        illustrationColor = Color(0xFFDCEDC8)   // soft lime
    )
)

// Gradient end color as specified in the design brief
private val GradientEnd = Color(0xFFE8F5F0)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()

    /** Marks onboarding done and navigates to login. */
    fun finishOnboarding() {
        OnboardingPreferences.markSeen(context)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundLight, GradientEnd)
                )
            )
    ) {
        // ── "Omitir" top-end button ──────────────────────────────────────
        TextButton(
            onClick = { finishOnboarding() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(end = 8.dp, top = 4.dp)
        ) {
            Text(
                text = "Omitir",
                style = MaterialTheme.typography.bodyLarge,
                color = PatientGreen
            )
        }

        // ── Main content ─────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp)) // clear the "Omitir" button

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { pageIndex ->
                SlideContent(
                    slide = slides[pageIndex],
                    // Fade: fully opaque when settled, translucent while scrolling
                    alpha = animateFloatAsState(
                        targetValue = if (pagerState.currentPage == pageIndex &&
                            pagerState.currentPageOffsetFraction == 0f
                        ) 1f else 0.4f,
                        animationSpec = tween(durationMillis = 350),
                        label = "slide-alpha-$pageIndex"
                    ).value
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dot indicators
            PageDots(
                pageCount = slides.size,
                currentPage = pagerState.currentPage
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Primary action button
            val isLastPage = pagerState.currentPage == slides.lastIndex
            RelaxButton(
                text = if (isLastPage) "Comenzar" else "Siguiente",
                onClick = {
                    if (isLastPage) {
                        finishOnboarding()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.PRIMARY,
                role = AppRole.PATIENT
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Slide content
// ---------------------------------------------------------------------------

@Composable
private fun SlideContent(slide: OnboardingSlide, alpha: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration placeholder — replaced with real asset in a later sprint
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(slide.illustrationColor)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Title — Outfit Bold 24sp → headlineSmall in our typography scale
        Text(
            text = slide.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Description — Urbanist Regular 16sp → bodyLarge in our scale
        Text(
            text = slide.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ---------------------------------------------------------------------------
// Dot indicator
// ---------------------------------------------------------------------------

@Composable
private fun PageDots(pageCount: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isCurrent = index == currentPage
            val size by animateFloatAsState(
                targetValue = if (isCurrent) 10f else 8f,
                animationSpec = tween(durationMillis = 250),
                label = "dot-size-$index"
            )
            Box(
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape)
                    .background(
                        color = if (isCurrent) PatientGreen
                        else PatientGreen.copy(alpha = 0.25f)
                    )
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Preview(name = "WelcomeScreen Light", showBackground = true, showSystemUi = true)
@Composable
private fun WelcomeScreenPreview() {
    RelaxMindTheme(darkTheme = false) {
        WelcomeScreen(onFinish = {})
    }
}
