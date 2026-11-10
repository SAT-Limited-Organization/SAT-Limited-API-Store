package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.AuthManager
import com.example.ui.theme.SatDarkBackground
import com.example.ui.theme.SatDarkSurface
import com.example.ui.theme.SatDarkSurfaceVariant
import com.example.ui.theme.SatPrimary
import com.example.ui.theme.SatSecondary
import com.example.ui.theme.SatTertiary
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(
    viewModel: MainViewModel,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val selectedTab by viewModel.selectedTab.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_sat),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SAT Limited API store",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    },
                    actions = {
                        // User tier badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SatDarkSurfaceVariant,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = userProfile?.tier?.uppercase() ?: "PRO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SatSecondary
                                )
                            }
                        }

                        // Sign out icon
                        IconButton(
                            onClick = {
                                AuthManager.signOut(
                                    credentialManager = credentialManager,
                                    onSignOutComplete = onSignOut,
                                    scope = coroutineScope
                                )
                            },
                            modifier = Modifier.testTag("sign_out_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Sign Out",
                                tint = Color(0xFFFF8A80)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = SatDarkSurface
                    )
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = SatDarkSurface,
                        contentColor = SatSecondary
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == AppNavTab.CATALOG,
                            onClick = { viewModel.setTab(AppNavTab.CATALOG) },
                            icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Store") },
                            label = { Text("Store", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SatSecondary,
                                selectedTextColor = SatSecondary,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = SatDarkSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_catalog")
                        )

                        NavigationBarItem(
                            selected = selectedTab == AppNavTab.PLAYGROUND,
                            onClick = { viewModel.setTab(AppNavTab.PLAYGROUND) },
                            icon = { Icon(Icons.Default.PlayCircle, contentDescription = "Tester") },
                            label = { Text("Playground", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SatSecondary,
                                selectedTextColor = SatSecondary,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = SatDarkSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_playground")
                        )

                        NavigationBarItem(
                            selected = selectedTab == AppNavTab.BUILDER,
                            onClick = { viewModel.setTab(AppNavTab.BUILDER) },
                            icon = { Icon(Icons.Default.Build, contentDescription = "Custom Builder") },
                            label = { Text("Builder", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SatSecondary,
                                selectedTextColor = SatSecondary,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = SatDarkSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_builder")
                        )

                        NavigationBarItem(
                            selected = selectedTab == AppNavTab.KEYS_AND_STATS,
                            onClick = { viewModel.setTab(AppNavTab.KEYS_AND_STATS) },
                            icon = { Icon(Icons.Default.Key, contentDescription = "Keys & Stats") },
                            label = { Text("Keys & Logs", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SatSecondary,
                                selectedTextColor = SatSecondary,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = SatDarkSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_keys")
                        )

                        NavigationBarItem(
                            selected = selectedTab == AppNavTab.SAVED,
                            onClick = { viewModel.setTab(AppNavTab.SAVED) },
                            icon = { Icon(Icons.Default.Bookmark, contentDescription = "Saved") },
                            label = { Text("Library", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SatSecondary,
                                selectedTextColor = SatSecondary,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = SatDarkSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_saved")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = SatDarkSurface,
                        contentColor = SatSecondary,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        NavigationRailItem(
                            selected = selectedTab == AppNavTab.CATALOG,
                            onClick = { viewModel.setTab(AppNavTab.CATALOG) },
                            icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Store") },
                            label = { Text("Store") },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = SatSecondary,
                                indicatorColor = SatDarkSurfaceVariant
                            )
                        )
                        NavigationRailItem(
                            selected = selectedTab == AppNavTab.PLAYGROUND,
                            onClick = { viewModel.setTab(AppNavTab.PLAYGROUND) },
                            icon = { Icon(Icons.Default.PlayCircle, contentDescription = "Tester") },
                            label = { Text("Playground") },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = SatSecondary,
                                indicatorColor = SatDarkSurfaceVariant
                            )
                        )
                        NavigationRailItem(
                            selected = selectedTab == AppNavTab.BUILDER,
                            onClick = { viewModel.setTab(AppNavTab.BUILDER) },
                            icon = { Icon(Icons.Default.Build, contentDescription = "Custom Builder") },
                            label = { Text("Builder") },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = SatSecondary,
                                indicatorColor = SatDarkSurfaceVariant
                            )
                        )
                        NavigationRailItem(
                            selected = selectedTab == AppNavTab.KEYS_AND_STATS,
                            onClick = { viewModel.setTab(AppNavTab.KEYS_AND_STATS) },
                            icon = { Icon(Icons.Default.Key, contentDescription = "Keys & Stats") },
                            label = { Text("Keys & Logs") },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = SatSecondary,
                                indicatorColor = SatDarkSurfaceVariant
                            )
                        )
                        NavigationRailItem(
                            selected = selectedTab == AppNavTab.SAVED,
                            onClick = { viewModel.setTab(AppNavTab.SAVED) },
                            icon = { Icon(Icons.Default.Bookmark, contentDescription = "Saved") },
                            label = { Text("Library") },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = SatSecondary,
                                indicatorColor = SatDarkSurfaceVariant
                            )
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (selectedTab) {
                        AppNavTab.CATALOG -> CatalogScreen(viewModel = viewModel)
                        AppNavTab.PLAYGROUND -> PlaygroundScreen(viewModel = viewModel)
                        AppNavTab.BUILDER -> CustomBuilderScreen(viewModel = viewModel)
                        AppNavTab.KEYS_AND_STATS -> KeysAndLogsScreen(viewModel = viewModel)
                        AppNavTab.SAVED -> SavedLibraryScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
