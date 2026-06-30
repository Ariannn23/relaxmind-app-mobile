package com.relaxmind.app.features.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.relaxmind.app.data.model.LibraryArticle
import com.relaxmind.app.data.remote.FirestoreRepository
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.themes.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    articleId: String,
    role: String, // "patient" | "caregiver"
    onNavigateBack: () -> Unit,
    repository: FirestoreRepository = remember { FirestoreRepository() }
) {
    var article by remember { mutableStateOf<LibraryArticle?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var usefulVote by remember { mutableStateOf<Boolean?>(null) } // null = no vote, true = yes, false = no

    val themeColor = if (role == "caregiver") CaregiverIndigo else PatientGreen

    LaunchedEffect(articleId) {
        isLoading = true
        val result = repository.getArticleById(articleId)
        if (result.isSuccess) {
            article = result.getOrNull()
        }
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SoftGradientBackground(animateBlobs = true)

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = themeColor)
            }
        } else {
            val currentArticle = article
            if (currentArticle == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "El artículo no pudo ser encontrado.",
                        fontFamily = LexendFontFamily,
                        color = TextSecondary
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Large Cover Image or Colored Box placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        if (currentArticle.coverImageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = currentArticle.coverImageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(themeColor.copy(alpha = 0.5f), themeColor)
                                        )
                                    )
                            )
                        }

                        // Floating Back Button over Cover Image
                        Box(
                            modifier = Modifier
                                .padding(top = 44.dp, start = 20.dp)
                                .size(44.dp)
                                .shadow(elevation = 6.dp, shape = CircleShape)
                                .background(Color.White, CircleShape)
                                .clickable(onClick = onNavigateBack),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Volver",
                                tint = themeColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Content details
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // Category Tag
                        Box(
                            modifier = Modifier
                                .background(themeColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = currentArticle.category.uppercase(),
                                fontSize = 11.sp,
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = themeColor
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Title
                        Text(
                            text = currentArticle.title,
                            fontFamily = Outfit,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = TextPrimary,
                            lineHeight = 32.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Author & Read time
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val infoText = buildString {
                                if (!currentArticle.author.isNullOrBlank()) {
                                    append(currentArticle.author)
                                    append("  •  ")
                                }
                                append("${currentArticle.readTimeMinutes} min de lectura")
                            }
                            Text(
                                text = infoText,
                                fontFamily = Urbanist,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = BorderSoft, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Article Body Text with expandable sections
                        val sections = parseArticleSections(currentArticle.content)
                        sections.forEachIndexed { index, section ->
                            ExpandableSection(
                                section = section,
                                themeColor = themeColor,
                                isExpandedInitially = index == 0 // Expand the first one by default
                            )
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                        HorizontalDivider(color = BorderSoft, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(20.dp))

                        // Is useful vote section
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "¿Te fue útil esta lectura?",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Vote Yes Button
                                VoteButton(
                                    label = "👍 Sí",
                                    isSelected = usefulVote == true,
                                    onClick = { usefulVote = true }
                                )
                                // Vote No Button
                                VoteButton(
                                    label = "👎 No",
                                    isSelected = usefulVote == false,
                                    onClick = { usefulVote = false }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun VoteButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0xFFE2F3EB) else Color.White)
            .border(
                width = 1.dp,
                color = if (isSelected) PatientGreen else BorderSoft,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (isSelected) PatientGreen else TextPrimary
        )
    }
}

fun parseMarkdownToAnnotatedString(text: String, primaryColor: Color): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")
        lines.forEachIndexed { index, line ->
            val trimmedLine = line.trim()
            if (trimmedLine.startsWith("- ")) {
                append("   •   ")
                val cleanLine = trimmedLine.substring(2)
                parseBoldText(cleanLine, primaryColor)
            } else {
                parseBoldText(line, primaryColor)
            }
            if (index < lines.lastIndex) {
                append("\n")
            }
        }
    }
}

fun AnnotatedString.Builder.parseBoldText(text: String, primaryColor: Color) {
    val parts = text.split("**")
    parts.forEachIndexed { i, part ->
        if (i % 2 == 1) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor)) {
                append(part)
            }
        } else {
            append(part)
        }
    }
}

data class ArticleSection(val title: String, val content: String)

fun parseArticleSections(content: String): List<ArticleSection> {
    if (!content.contains("## ")) {
        return listOf(ArticleSection("", content))
    }
    val sections = mutableListOf<ArticleSection>()
    val lines = content.split("\n")
    var currentTitle = ""
    var currentContent = StringBuilder()

    for (line in lines) {
        if (line.trim().startsWith("## ")) {
            if (currentTitle.isNotEmpty() || currentContent.isNotBlank()) {
                sections.add(ArticleSection(currentTitle, currentContent.toString().trim()))
            }
            currentTitle = line.trim().removePrefix("## ").trim()
            currentContent = StringBuilder()
        } else {
            currentContent.append(line).append("\n")
        }
    }
    if (currentTitle.isNotEmpty() || currentContent.isNotBlank()) {
        sections.add(ArticleSection(currentTitle, currentContent.toString().trim()))
    }
    return sections
}

@Composable
fun ExpandableSection(
    section: ArticleSection,
    themeColor: Color,
    isExpandedInitially: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(isExpandedInitially) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = section.title,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                    tint = themeColor
                )
            }
            
            if (isExpanded && section.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                val formattedBody = parseMarkdownToAnnotatedString(section.content, themeColor)
                Text(
                    text = formattedBody,
                    fontFamily = Urbanist,
                    fontSize = 15.sp,
                    color = TextPrimary,
                    lineHeight = 24.sp
                )
            }
        }
    }
}
