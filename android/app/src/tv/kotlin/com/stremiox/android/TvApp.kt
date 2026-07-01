package com.stremiox.android

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stremiox.android.data.CatalogRepository
import com.stremiox.android.data.PreviewCatalogRepository
import com.stremiox.android.model.Catalog
import com.stremiox.android.model.MetaItem
import com.stremiox.android.model.Playable
import com.stremiox.android.player.PlayerScreen
import com.stremiox.android.ui.UiState
import com.stremiox.android.ui.screens.DetailScreen
import com.stremiox.android.ui.theme.StremioXTheme
import com.stremiox.android.ui.viewmodel.*

// ── Navigation items ─────────────────────────────────────────────────────────

private enum class TvTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home),
    DISCOVER("Discover", Icons.Filled.Explore),
    LIBRARY("Library", Icons.Filled.VideoLibrary),
    SEARCH("Search", Icons.Filled.Search),
    SETTINGS("Settings", Icons.Filled.Settings),
}

// ── Root composable ───────────────────────────────────────────────────────────

/**
 * Android TV shell.
 *
 * Layout: a permanent left navigation rail (D-pad Up/Down to move between tabs) with the content
 * area to the right. Cards are 10-foot-sized (196 dp wide). Focus rings appear on every
 * interactive element so a remote-control cursor is always visible.
 */
@Composable
fun TvApp(repo: CatalogRepository = PreviewCatalogRepository()) {
    StremioXTheme {
        var tab by remember { mutableStateOf(TvTab.HOME) }
        var detail by remember { mutableStateOf<MetaItem?>(null) }
        var playing by remember { mutableStateOf<Playable?>(null) }

        val playable = playing
        if (playable != null) {
            PlayerScreen(playable = playable, onBack = { playing = null })
            return@StremioXTheme
        }

        val current = detail
        if (current != null) {
            val detailVm: DetailViewModel = viewModel(
                key = "detail-${current.id}",
                factory = StremioXViewModelFactory(
                    repo = repo,
                    detailArgs = StremioXViewModelFactory.DetailArgs(current.type, current.id),
                ),
            )
            DetailScreen(
                viewModel = detailVm,
                title = current.name,
                onBack = { detail = null },
                onPlay = { playing = it },
            )
            return@StremioXTheme
        }

        val factory = StremioXViewModelFactory(repo)

        Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            // ── Left rail ────────────────────────────────────────────────────
            TvNavRail(
                currentTab = tab,
                onTabSelected = { tab = it },
            )

            // ── Content area ─────────────────────────────────────────────────
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                when (tab) {
                    TvTab.HOME ->
                        TvHomeScreen(viewModel(factory = factory), onItem = { detail = it })
                    TvTab.DISCOVER ->
                        TvDiscoverScreen(viewModel(factory = factory), onItem = { detail = it })
                    TvTab.LIBRARY ->
                        TvLibraryScreen(viewModel(factory = factory), onItem = { detail = it })
                    TvTab.SEARCH ->
                        TvSearchScreen(viewModel(factory = factory), onItem = { detail = it })
                    TvTab.SETTINGS ->
                        TvSettingsScreen()
                }
            }
        }
    }
}

// ── Left navigation rail ──────────────────────────────────────────────────────

@Composable
private fun TvNavRail(currentTab: TvTab, onTabSelected: (TvTab) -> Unit) {
    Column(
        modifier = Modifier
            .width(200.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 32.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Wordmark
        Text(
            text = "VortX",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 12.dp, bottom = 24.dp),
        )

        TvTab.entries.forEach { t ->
            TvNavItem(
                tab = t,
                selected = t == currentTab,
                onClick = { onTabSelected(t) },
            )
        }
    }
}

@Composable
private fun TvNavItem(tab: TvTab, selected: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val highlight = selected || focused

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else if (focused) MaterialTheme.colorScheme.surfaceVariant
                else Color.Transparent
            )
            .border(
                width = if (focused && !selected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick)
            .focusable()
            .onFocusChanged { focused = it.isFocused }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = null,
            tint = if (highlight) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = tab.label,
            style = MaterialTheme.typography.titleMedium,
            color = if (highlight) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── TV Home ───────────────────────────────────────────────────────────────────

@Composable
private fun TvHomeScreen(vm: HomeViewModel, onItem: (MetaItem) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    when (val s = state) {
        is UiState.Loading -> TvLoadingScreen()
        is UiState.Error   -> TvErrorScreen(s.message, onRetry = vm::load)
        is UiState.Success -> TvCatalogContent(s.data, onItem)
    }
}

@Composable
private fun TvDiscoverScreen(vm: DiscoverViewModel, onItem: (MetaItem) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    when (val s = state) {
        is UiState.Loading -> TvLoadingScreen()
        is UiState.Error   -> TvErrorScreen(s.message, onRetry = vm::load)
        is UiState.Success -> TvCatalogContent(s.data, onItem)
    }
}

@Composable
private fun TvLibraryScreen(vm: LibraryViewModel, onItem: (MetaItem) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    when (val s = state) {
        is UiState.Loading -> TvLoadingScreen()
        is UiState.Error   -> TvErrorScreen(s.message, onRetry = vm::load)
        is UiState.Success -> TvCatalogContent(s.data, onItem)
    }
}

@Composable
private fun TvSearchScreen(vm: SearchViewModel, onItem: (MetaItem) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Text(
            "Search",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp),
        )
        when (val s = state) {
            is UiState.Loading -> TvLoadingScreen()
            is UiState.Error   -> TvErrorScreen(s.message, onRetry = vm::load)
            is UiState.Success -> TvCatalogContent(s.data, onItem)
        }
    }
}

@Composable
private fun TvSettingsScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            "Add-ons, accounts, and playback options will appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Shared TV catalog layout ──────────────────────────────────────────────────

@Composable
private fun TvCatalogContent(catalogs: List<Catalog>, onItem: (MetaItem) -> Unit) {
    val hero = catalogs.firstOrNull()?.items?.firstOrNull()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        if (hero != null) {
            item { TvHeroHeader(hero) }
        }
        items(catalogs, key = { it.id }) { catalog ->
            val eyebrow = if (catalog.id == "continue") "Pick up where you left off" else null
            TvPosterRail(catalog = catalog, onItem = onItem, eyebrow = eyebrow)
        }
    }
}

// ── TV Hero ───────────────────────────────────────────────────────────────────

@Composable
private fun TvHeroHeader(item: MetaItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.background,
                    )
                )
            ),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 48.dp, bottom = 40.dp),
        ) {
            Text(
                text = item.type.label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp,
            )
            Text(
                text = item.name,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 48.sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp),
            )
            item.year?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

// ── TV Poster Rail ────────────────────────────────────────────────────────────

@Composable
private fun TvPosterRail(catalog: Catalog, onItem: (MetaItem) -> Unit, eyebrow: String? = null) {
    Column {
        // Rail header
        Column(modifier = Modifier.padding(start = 48.dp, bottom = 16.dp)) {
            if (eyebrow != null) {
                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp,
                )
            }
            Text(
                text = catalog.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 48.dp)) {
            items(catalog.items, key = { it.id }) { item ->
                TvPosterCard(
                    item = item,
                    onClick = { onItem(item) },
                    modifier = Modifier.width(196.dp).padding(end = 20.dp),
                )
            }
        }
    }
}

// ── TV Poster Card ────────────────────────────────────────────────────────────

@Composable
private fun TvPosterCard(item: MetaItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var focused by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = if (focused) 3.dp else 0.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(14.dp),
                )
                .background(tvPosterBrush(item.id))
                .clickable(onClick = onClick)
                .focusable()
                .onFocusChanged { focused = it.isFocused },
        ) {
            // Gradient scrim at the bottom so text is readable
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    ),
            )
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.BottomStart).padding(14.dp),
            )
            // Focus ring glow overlay
            if (focused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ),
                )
            }
        }
        Text(
            text = listOfNotNull(item.year, item.type.label).joinToString(" · "),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp),
        )
    }
}

// ── Loading / Error states ────────────────────────────────────────────────────

@Composable
private fun TvLoadingScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 32.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        items(3) {
            Column {
                Box(
                    modifier = Modifier
                        .padding(start = 48.dp, bottom = 16.dp)
                        .size(width = 200.dp, height = 20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                LazyRow(contentPadding = PaddingValues(horizontal = 48.dp)) {
                    items(6) {
                        Box(
                            modifier = Modifier
                                .width(196.dp)
                                .padding(end = 20.dp)
                                .aspectRatio(2f / 3f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TvErrorScreen(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        var focused by remember { mutableStateOf(false) }
        val fr = remember { FocusRequester() }
        Button(
            onClick = onRetry,
            modifier = Modifier
                .focusRequester(fr)
                .focusable()
                .onFocusChanged { focused = it.isFocused }
                .border(
                    width = if (focused) 2.dp else 0.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(50),
                ),
        ) {
            Text("Retry")
        }
        LaunchedEffect(Unit) { fr.requestFocus() }
    }
}

// ── Gradient helper ───────────────────────────────────────────────────────────

private fun tvPosterBrush(seed: String): Brush {
    val h = seed.hashCode()
    val hue = ((h ushr 8) % 80) - 20
    val top = tvHsl(260f + hue, 0.42f, 0.34f)
    val bottom = tvHsl(260f + hue, 0.50f, 0.16f)
    return Brush.verticalGradient(listOf(top, bottom))
}

private fun tvHsl(hDeg: Float, s: Float, l: Float): Color {
    val h = ((hDeg % 360f) + 360f) % 360f
    val c = (1f - kotlin.math.abs(2 * l - 1f)) * s
    val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
    val m = l - c / 2f
    val (r, g, b) = when {
        h < 60f  -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else     -> Triple(c, 0f, x)
    }
    return Color(r + m, g + m, b + m)
}
