package com.relaxmind.app.features.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.PatientGreenLight
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary

@Composable
fun AchievementLibraryScreen(
    viewModel: PatientViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val achievements by viewModel.achievements.collectAsState()
    val unlockedKeys = achievements.map { it.achievementKey }.toSet()

    LaunchedEffect(Unit) {
        viewModel.loadProgressData()
    }
    
    val allAchievements = AchievementCatalog.items
    val unlockedCount = allAchievements.count { it.key in unlockedKeys }
    val lockedCount = allAchievements.size - unlockedCount
    
    var selectedTab by remember { mutableStateOf(0) } // 0: Todos, 1: Completos, 2: Bloqueados
    
    val displayedAchievements = when (selectedTab) {
        1 -> allAchievements.filter { it.key in unlockedKeys }
        2 -> allAchievements.filter { it.key !in unlockedKeys }
        else -> allAchievements
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Biblioteca de logros",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Descubre tus insignias y celebra tu progreso",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SoftGradientBackground(animateBlobs = false) // Static soft background
            
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Stats Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CheckCircle,
                        iconTint = PatientGreen,
                        label = "Completos",
                        value = unlockedCount.toString()
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Lock,
                        iconTint = TextSecondary,
                        label = "Bloqueados",
                        value = lockedCount.toString()
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CheckCircle, // You can change this
                        iconTint = Color(0xFF3B82F6), // Blue
                        label = "Totales",
                        value = allAchievements.size.toString()
                    )
                }
                
                // Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TabPill(
                        text = "Todos",
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    TabPill(
                        text = "Completos",
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1.3f)
                    )
                    TabPill(
                        text = "Bloqueados",
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        modifier = Modifier.weight(1.2f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedAchievements) { achievement ->
                        val isUnlocked = achievement.key in unlockedKeys
                        AchievementLibraryCard(
                            achievement = achievement,
                            isUnlocked = isUnlocked
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    value: String
) {
    Column(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.05f))
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Text(
            text = value,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = iconTint
        )
    }
}

@Composable
fun TabPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) PatientGreenLight.copy(alpha = 0.3f) else Color.White
    val textColor = if (selected) PatientGreen else TextSecondary
    val borderColor = if (selected) PatientGreen else Color.LightGray.copy(alpha = 0.5f)
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = textColor
        )
    }
}

@Composable
fun AchievementLibraryCard(
    achievement: AchievementCatalogItem,
    isUnlocked: Boolean
) {
    val alpha = if (isUnlocked) 1f else 0.5f
    
    Column(
        modifier = Modifier
            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.05f))
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hexagon icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    if (isUnlocked) PatientGreenLight.copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.3f),
                    RoundedCornerShape(24.dp)
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = achievement.defaultIconUrl,
                contentDescription = achievement.title,
                modifier = Modifier.size(50.dp).clip(CircleShape),
                contentScale = ContentScale.Fit,
                alpha = alpha
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = achievement.title,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                contentDescription = null,
                tint = if (isUnlocked) PatientGreen else TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isUnlocked) "Completo" else "Bloqueado",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = if (isUnlocked) PatientGreen else TextSecondary
            )
        }
    }
}
