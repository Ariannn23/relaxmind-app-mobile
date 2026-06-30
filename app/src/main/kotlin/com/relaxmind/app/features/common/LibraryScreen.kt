package com.relaxmind.app.features.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.relaxmind.app.data.model.LibraryArticle
import com.relaxmind.app.data.remote.FirestoreRepository
import com.relaxmind.app.ui.components.RelaxCard
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.themes.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    role: String, // "patient" | "caregiver"
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit,
    repository: FirestoreRepository = remember { FirestoreRepository() }
) {
    var articles by remember { mutableStateOf<List<LibraryArticle>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }

    // Categories based on role
    val categories = if (role == "patient") {
        listOf("Todos", "ansiedad", "depresion", "estres", "sueño", "autoestima", "general")
    } else {
        listOf("Todos", "comunicacion", "cuidado_del_cuidador", "crisis", "general")
    }

    LaunchedEffect(role) {
        isLoading = true
        val result = repository.getLibraryArticles(role)
        if (result.isSuccess) {
            articles = result.getOrNull() ?: emptyList()
        }
        isLoading = false
    }

    // Filter and Search Logic
    val filteredArticles = articles.filter { article ->
        val matchesSearch = article.title.contains(searchQuery, ignoreCase = true) ||
                article.summary.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "Todos" || article.category.lowercase() == selectedCategory.lowercase()
        matchesSearch && matchesCategory
    }

    val featuredArticles = filteredArticles.filter { it.featured }
    val regularArticles = filteredArticles.filter { !it.featured }

    val themeColor = if (role == "caregiver") CaregiverIndigo else PatientGreen

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Biblioteca de Apoyo",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 20.dp)
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
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SoftGradientBackground(animateBlobs = true)

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = themeColor)
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { 
                            Text(
                                text = "Buscar artículos...",
                                fontFamily = Urbanist,
                                color = Color.LightGray
                            ) 
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = themeColor
                            )
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColor,
                            unfocusedBorderColor = BorderSoft,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    )

                    // Horizontal Category Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            val isSelected = category == selectedCategory
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) themeColor else Color.White)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) themeColor else BorderSoft,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { selectedCategory = category }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = category.replace("_", " ").replaceFirstChar { it.uppercase() },
                                    fontFamily = LexendFontFamily,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        // Featured Section
                        if (featuredArticles.isNotEmpty()) {
                            item {
                                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                                    Text(
                                        text = "Destacados",
                                        fontFamily = LexendFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
                                    )
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 20.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        items(featuredArticles) { article ->
                                            FeaturedArticleCard(
                                                article = article,
                                                themeColor = themeColor,
                                                onClick = { onNavigateToDetail(article.id) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Regular Articles Header
                        if (regularArticles.isNotEmpty() || (featuredArticles.isEmpty() && filteredArticles.isNotEmpty())) {
                            item {
                                Text(
                                    text = "Artículos de Apoyo",
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)
                                )
                            }

                            items(regularArticles.ifEmpty { filteredArticles }) { article ->
                                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                                    RegularArticleCard(
                                        article = article,
                                        themeColor = themeColor,
                                        onClick = { onNavigateToDetail(article.id) }
                                    )
                                }
                            }
                        }

                        // Empty State
                        if (filteredArticles.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 80.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "📚",
                                        fontSize = 56.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Aún no hay artículos en esta categoría",
                                        fontFamily = LexendFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeaturedArticleCard(
    article: LibraryArticle,
    themeColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .width(280.dp)
            .height(180.dp)
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (article.coverImageUrl.isNotEmpty()) {
                AsyncImage(
                    model = article.coverImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(themeColor.copy(alpha = 0.5f), themeColor)))
                )
            }

            // Dark overlay gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )

            // Category tag
            Box(
                modifier = Modifier
                    .padding(14.dp)
                    .align(Alignment.TopStart)
                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = article.category.replace("_", " ").uppercase(),
                    fontSize = 9.sp,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = themeColor
                )
            }

            // Title
            Text(
                text = article.title,
                fontFamily = Outfit,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun RegularArticleCard(
    article: LibraryArticle,
    themeColor: Color,
    onClick: () -> Unit
) {
    RelaxCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.White
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Cover Image
            if (article.coverImageUrl.isNotEmpty()) {
                AsyncImage(
                    model = article.coverImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(themeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "📄", fontSize = 28.sp)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right Content Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = article.title,
                    fontFamily = Outfit,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = article.summary,
                    fontFamily = Urbanist,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${article.readTimeMinutes} min de lectura",
                            fontFamily = Urbanist,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    // Category tag
                    Box(
                        modifier = Modifier
                            .background(themeColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = article.category.replace("_", " ").replaceFirstChar { it.uppercase() },
                            fontSize = 10.sp,
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = themeColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
