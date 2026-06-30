package com.relaxmind.app.features.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.relaxmind.app.R
import com.relaxmind.app.ui.components.RelaxIcons
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.themes.*

@Composable
fun TermsAndConditionsScreen(
    role: String,
    onNavigateBack: () -> Unit
) {
    val themeColor = if (role == "caregiver") CaregiverIndigo else PatientGreen

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        typography = LexendTypography
    ) {
        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Background decoration
                SoftGradientBackground(animateBlobs = true)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Circular Shadow Back Button & Header Title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        com.relaxmind.app.ui.components.RelaxBackButton(
                            onClick = onNavigateBack
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title and Subtitle
                    Text(
                        text = stringResource(id = R.string.terms_title),
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = R.string.terms_subtitle),
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Main Scrollable Content Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = Color(0xFFECEEF2).copy(alpha = 0.5f),
                                spotColor = Color(0xFFECEEF2).copy(alpha = 0.5f)
                            )
                            .background(Color.White, RoundedCornerShape(28.dp))
                            .border(1.dp, Color(0xFFECEEF2), RoundedCornerShape(28.dp))
                            .clip(RoundedCornerShape(28.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            TermsSection(
                                title = stringResource(id = R.string.terms_section1_title),
                                description = stringResource(id = R.string.terms_section1_desc)
                            )
                            TermsSection(
                                title = stringResource(id = R.string.terms_section2_title),
                                description = stringResource(id = R.string.terms_section2_desc)
                            )
                            TermsSection(
                                title = stringResource(id = R.string.terms_section3_title),
                                description = stringResource(id = R.string.terms_section3_desc)
                            )
                            TermsSection(
                                title = stringResource(id = R.string.terms_section4_title),
                                description = stringResource(id = R.string.terms_section4_desc)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TermsSection(
    title: String,
    description: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TextPrimary
        )
        Text(
            text = description,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = TextSecondary,
            lineHeight = 20.sp
        )
    }
}
