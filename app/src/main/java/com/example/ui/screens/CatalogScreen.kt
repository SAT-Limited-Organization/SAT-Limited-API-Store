package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.catalog.SatApiCatalog
import com.example.data.model.StoreApiDefinition
import com.example.ui.theme.SatDarkBackground
import com.example.ui.theme.SatDarkSurface
import com.example.ui.theme.SatDarkSurfaceVariant
import com.example.ui.theme.SatPrimary
import com.example.ui.theme.SatSecondary
import com.example.ui.theme.SatSuccess
import com.example.ui.theme.SatTertiary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun CatalogScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categoryFilter by viewModel.categoryFilter.collectAsState()
    val savedResources by viewModel.savedResources.collectAsState()

    var activeDialogApi by remember { mutableStateOf<StoreApiDefinition?>(null) }

    val filteredApis = remember(searchQuery, categoryFilter) {
        SatApiCatalog.storeApis.filter { api ->
            val matchesCategory = categoryFilter == "all" || api.category == categoryFilter
            val matchesSearch = searchQuery.isBlank() ||
                api.title.contains(searchQuery, ignoreCase = true) ||
                api.summary.contains(searchQuery, ignoreCase = true) ||
                api.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SatDarkBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Search TextField
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search APIs: Exams, Textbooks, Movies, YouTube, WhatsApp, AI...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = SatSecondary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("catalog_search_input"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SatDarkSurface,
                unfocusedContainerColor = SatDarkSurface,
                focusedBorderColor = SatSecondary,
                unfocusedBorderColor = SatDarkSurfaceVariant,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        val categories = listOf(
            "all" to "All APIs",
            "exams" to "Exam Papers",
            "textbooks" to "Textbooks",
            "movies" to "Movie Box",
            "youtube" to "YouTube",
            "whatsapp" to "WhatsApp Baileys",
            "ai" to "Gemini AI"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { (catId, catLabel) ->
                val isSelected = categoryFilter == catId
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setCategoryFilter(catId) },
                    label = { Text(catLabel, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SatPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = SatDarkSurfaceVariant,
                        labelColor = Color(0xFFC7C2E5)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("filter_chip_$catId")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // API List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filteredApis, key = { it.id }) { api ->
                ApiCatalogCard(
                    api = api,
                    onTestClick = { viewModel.selectStoreApiForPlayground(api) },
                    onExploreClick = { activeDialogApi = api }
                )
            }
        }
    }

    // Modal to browse actual data of the selected API
    if (activeDialogApi != null) {
        ExploreApiDataDialog(
            api = activeDialogApi!!,
            savedUrls = savedResources.map { it.resourceUrl },
            onBookmark = { type, title, sub, url ->
                viewModel.bookmarkResource(type, title, sub, url)
            },
            onTestApi = {
                viewModel.selectStoreApiForPlayground(activeDialogApi!!)
                activeDialogApi = null
            },
            onDismiss = { activeDialogApi = null }
        )
    }
}

@Composable
fun ApiCatalogCard(
    api: StoreApiDefinition,
    onTestClick: () -> Unit,
    onExploreClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("api_card_${api.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SatDarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIconBadge(category = api.category)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = api.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = api.badge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SatSecondary
                        )
                    }
                }

                // Method badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (api.method) {
                        "GET" -> Color(0xFF1B5E20)
                        "POST" -> Color(0xFF0D47A1)
                        else -> Color(0xFF4A148C)
                    }
                ) {
                    Text(
                        text = api.method,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = api.summary,
                fontSize = 13.sp,
                color = Color(0xFFC7C2E5),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Endpoint code bar
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0A0717),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = api.defaultEndpoint,
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = SatSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onExploreClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = SatSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Explore Data", fontSize = 12.sp)
                }

                Button(
                    onClick = onTestClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SatPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test in Tester", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun CategoryIconBadge(category: String) {
    val (icon, bg) = when (category) {
        "exams" -> Icons.Default.School to Color(0xFF3F51B5)
        "textbooks" -> Icons.AutoMirrored.Filled.MenuBook to Color(0xFF00897B)
        "movies" -> Icons.Default.Movie to Color(0xFFE53935)
        "youtube" -> Icons.Default.PlayCircle to Color(0xFFD32F2F)
        "whatsapp" -> Icons.Default.QuestionAnswer to Color(0xFF2E7D32)
        "ai" -> Icons.Default.AutoAwesome to Color(0xFF7C4DFF)
        else -> Icons.Default.School to SatPrimary
    }

    Box(
        modifier = Modifier
            .size(42.dp)
            .background(bg.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = bg,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun ExploreApiDataDialog(
    api: StoreApiDefinition,
    savedUrls: List<String>,
    onBookmark: (type: String, title: String, subtitle: String, url: String) -> Unit,
    onTestApi: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryIconBadge(category = api.category)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(api.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Live API Database Records", fontSize = 11.sp, color = SatSecondary)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (api.category) {
                    "exams" -> {
                        items(SatApiCatalog.sampleExamPapers) { paper ->
                            val isSaved = savedUrls.contains(paper.downloadUrl)
                            ResourceItemCard(
                                title = "${paper.examBoard} ${paper.subject}",
                                subtitle = "${paper.level} • ${paper.season} ${paper.year} • ${paper.paperNumber}",
                                tag = "PDF Paper",
                                isSaved = isSaved,
                                onSave = {
                                    onBookmark("paper", "${paper.examBoard} ${paper.subject}", "${paper.year} ${paper.season}", paper.downloadUrl)
                                }
                            )
                        }
                    }
                    "textbooks" -> {
                        items(SatApiCatalog.sampleTextbooks) { book ->
                            val isSaved = savedUrls.contains(book.readUrl)
                            ResourceItemCard(
                                title = book.title,
                                subtitle = "${book.subject} • ${book.gradeLevel} • ${book.totalChapters} Chapters • ISBN: ${book.isbn}",
                                tag = "Digital Book",
                                isSaved = isSaved,
                                onSave = {
                                    onBookmark("textbook", book.title, book.authorOrPublisher, book.readUrl)
                                }
                            )
                        }
                    }
                    "movies" -> {
                        items(SatApiCatalog.sampleMovies) { movie ->
                            val isSaved = savedUrls.contains(movie.downloadUrl)
                            ResourceItemCard(
                                title = "${movie.title} (${movie.year})",
                                subtitle = "Rating: ⭐ ${movie.rating} • ${movie.quality} • ${movie.genre}\n${movie.synopsis}",
                                tag = "HD Stream",
                                isSaved = isSaved,
                                onSave = {
                                    onBookmark("movie", movie.title, "${movie.year} • ⭐ ${movie.rating}", movie.downloadUrl)
                                }
                            )
                        }
                    }
                    "youtube" -> {
                        items(SatApiCatalog.sampleYouTubeVideos) { video ->
                            val isSaved = savedUrls.contains(video.video1080pUrl)
                            ResourceItemCard(
                                title = video.title,
                                subtitle = "Channel: ${video.channel} • Duration: ${video.duration} • ${video.views}\nFormats: 1080p MP4 | 720p MP4 | 320k MP3",
                                tag = "Video/Audio",
                                isSaved = isSaved,
                                onSave = {
                                    onBookmark("video", video.title, video.channel, video.video1080pUrl)
                                }
                            )
                        }
                    }
                    "whatsapp" -> {
                        item {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0C1F))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Baileys Socket Integration Guide", fontWeight = FontWeight.Bold, color = SatSecondary, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "SAT Limited API store provides ready-to-use webhook dispatchers that connect directly to your @whiskeysockets/baileys bots. You can forward incoming WhatsApp messages to this endpoint to trigger automatic AI exam answers, textbooks, and media delivery.",
                                        fontSize = 12.sp,
                                        color = Color(0xFFC7C2E5)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "npm install @whiskeysockets/baileys qrcode-terminal",
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = SatTertiary
                                    )
                                }
                            }
                        }
                    }
                    "ai" -> {
                        item {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0C1F))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Gemini 3.8 Flash AI Model", fontWeight = FontWeight.Bold, color = SatSecondary, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Integrated with Firebase AI Logic. This API solves complex examination questions step-by-step, summarizes curriculum chapters, and drafts responses for your WhatsApp Baileys bots.",
                                        fontSize = 12.sp,
                                        color = Color(0xFFC7C2E5)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onTestApi,
                colors = ButtonDefaults.buttonColors(containerColor = SatPrimary)
            ) {
                Text("Open in Playground")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.Gray)
            }
        },
        containerColor = SatDarkSurface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun ResourceItemCard(
    title: String,
    subtitle: String,
    tag: String,
    isSaved: Boolean,
    onSave: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1A38)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = SatPrimary.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = tag,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SatSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, fontSize = 11.sp, color = Color(0xFFC7C2E5), lineHeight = 16.sp)
            }

            IconButton(onClick = onSave) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Save",
                    tint = if (isSaved) SatTertiary else Color.LightGray
                )
            }
        }
    }
}
