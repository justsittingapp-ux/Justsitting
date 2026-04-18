package com.lawrence.justsitting

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WisdomScreen() {
    var currentSection by remember { mutableStateOf<WisdomSection?>(null) }
    var selectedTeaching by remember { mutableStateOf<Teaching?>(null) }

    val sections = WisdomRepository.sections

    BackHandler(enabled = currentSection != null) {
        if (selectedTeaching != null) {
            selectedTeaching = null
        } else {
            currentSection = null
        }
    }

    Scaffold(
        topBar = {
            if (currentSection != null) {
                TopAppBar(
                    title = { Text(if (selectedTeaching != null) "" else currentSection!!.title) },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (selectedTeaching != null) selectedTeaching = null
                            else currentSection = null
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when {
                selectedTeaching != null -> {
                    TeachingDetailContent(selectedTeaching!!, currentSection?.author ?: "")
                }
                currentSection != null -> {
                    TeachingsListView(currentSection!!) { teaching ->
                        selectedTeaching = teaching
                    }
                }
                else -> {
                    WisdomMainView(sections) { section ->
                        currentSection = section
                    }
                }
            }
        }
    }
}

@Composable
fun WisdomMainView(sections: List<WisdomSection>, onSectionSelect: (WisdomSection) -> Unit) {
    val context = LocalContext.current
    var isConfirmed by remember { mutableStateOf(false) }

    LaunchedEffect(isConfirmed) {
        if (isConfirmed) {
            kotlinx.coroutines.delay(1500)
            isConfirmed = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Wisdom",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            OutlinedButton(
                onClick = {
                    ProgressManager.resetAllProgress(context)
                    isConfirmed = true
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isConfirmed) Color(0xFF4CAF50) else Color.Gray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isConfirmed) "DONE!" else "MARK UNREAD",
                    fontSize = 12.sp,
                    color = if (isConfirmed) Color(0xFF4CAF50) else Color.LightGray,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        sections.forEach { section ->
            // Card de secțiune rafinat
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { onSectionSelect(section) },
                color = Color.Transparent, // Folosim gradientul din interior
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)) // Contur fin
            ) {
                Box(
                    modifier = Modifier.background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF1E1E1E), Color(0xFF141414))
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = section.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = section.color)
                            Text(text = section.description, fontSize = 14.sp, color = Color.Gray)
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = section.color.copy(alpha = 0.7f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun TeachingsListView(section: WisdomSection, onTeachingSelect: (Teaching) -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        section.teachings.forEach { teaching ->
            if (teaching.subtitle == "HEADER") {
                Text(
                    text = teaching.title,
                    color = Color(0xFFFF9800).copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
                )
            } else {
                val isRead = ProgressManager.isRead(context, teaching.title)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { onTeachingSelect(teaching) },
                    color = Color.Transparent,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.5.dp, if(isRead) Color.Transparent else Color.White.copy(alpha = 0.05f))
                ) {
                    Box(
                        modifier = Modifier.background(
                            brush = Brush.verticalGradient(
                                colors = if(isRead) listOf(Color(0xFF121212), Color(0xFF0F0F0F)) 
                                         else listOf(Color(0xFF1A1A1A), Color(0xFF161616))
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = teaching.subtitle,
                                        color = section.color.copy(alpha = if(isRead) 0.4f else 0.8f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    if (isRead) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("✓", color = section.color.copy(alpha = 0.3f), fontSize = 12.sp)
                                    }
                                }
                                Text(
                                    text = teaching.title,
                                    color = if (isRead) Color.Gray.copy(alpha = 0.6f) else Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = if(isRead) FontWeight.Normal else FontWeight.Medium
                                )
                            }
                            Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.White.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun TeachingDetailContent(teaching: Teaching, authorName: String) {
    val context = LocalContext.current

    LaunchedEffect(teaching.title) {
        ProgressManager.markAsRead(context, teaching.title)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = teaching.title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = "by $authorName", color = Color.Gray, fontSize = 14.sp, fontStyle = FontStyle.Italic)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = teaching.content,
            color = Color.LightGray.copy(alpha = 0.9f),
            fontSize = 18.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Light
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}