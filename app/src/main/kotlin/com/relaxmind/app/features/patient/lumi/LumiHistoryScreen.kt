package com.relaxmind.app.features.patient.lumi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.relaxmind.app.data.model.LumiSession
import com.relaxmind.app.ui.components.FullScreenLoadingScreen
import com.relaxmind.app.ui.themes.LexendFontFamily
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

private val LumiBlue = Color(0xFF38A9F2)
private val LumiBlueDark = Color(0xFF0B5C99)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LumiHistoryScreen(
    onNavigateBack: () -> Unit,
    onSessionSelected: (String) -> Unit,
    viewModel: LumiHistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.White, Color(0xFFF2FAFF), Color(0xFFE4F4FF))
                )
            )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    color = Color.White.copy(alpha = 0.72f),
                    shadowElevation = 4.dp
                ) {
                    TopAppBar(
                        title = { 
                            Text(
                                text = "Historial de Lumi",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = LumiBlueDark
                            ) 
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = LumiBlueDark)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            }
        ) { padding ->
            if (uiState.isLoading) {
                FullScreenLoadingScreen(text = "Cargando historial...")
            } else if (uiState.sessions.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No hay conversaciones archivadas.", color = Color.Gray, fontFamily = LexendFontFamily)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.sessions) { session ->
                        LumiHistoryItem(
                            session = session,
                            onClick = { onSessionSelected(session.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LumiHistoryItem(session: LumiSession, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(session.createdAt))

    // Generate a unique color based on session ID to visually distinguish "Chat con Lumi" default titles
    val colorPalette = listOf(
        Color(0xFFE3F2FD), Color(0xFFF3E5F5), Color(0xFFE8F5E9), 
        Color(0xFFFFF3E0), Color(0xFFFCE4EC), Color(0xFFE0F2F1)
    )
    val colorIndex = (session.id.hashCode().absoluteValue) % colorPalette.size
    val badgeColor = colorPalette[colorIndex]

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = LumiBlueDark.copy(alpha = 0.08f),
                spotColor = LumiBlueDark.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(badgeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                LumiAvatar(size = 40)
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = LexendFontFamily,
                    fontSize = 16.sp,
                    color = Color(0xFF2C3E50),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateString,
                    fontFamily = LexendFontFamily,
                    fontSize = 13.sp,
                    color = Color(0xFF7F8C8D)
                )
            }
            
            Icon(
                imageVector = Icons.Rounded.ArrowForwardIos,
                contentDescription = "Ver",
                tint = LumiBlue.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
