/*
 * kongamusic (2026)
 * © Samk
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.kongamusic.ui.component.floatingtabbar

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import moe.kongamusic.ui.screens.Screens

@Composable
fun FloatingTabBarNavigation(
    items: List<Screens>,
    currentRoute: String?,
    onItemClick: (Screens, Boolean) -> Unit,
    isInline: Boolean,
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
) {
    val isDarkScheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val backgroundColor = when {
        pureBlack -> Color.Black
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val selectedContentColor = when {
        pureBlack -> Color.White
        else -> MaterialTheme.colorScheme.primary
    }

    val unselectedContentColor = when {
        pureBlack -> Color.White.copy(alpha = 0.65f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val selectedTabKey =
        items
            .firstOrNull { screen -> isRouteSelected(currentRoute, screen.route, items) }
            ?.route

    val searchScreen = items.firstOrNull { it == Screens.Search }
    val tabScreens = remember(items) { items.filter { it != Screens.Search } }

    FloatingTabBar(
        isInline = isInline,
        selectedTabKey = selectedTabKey,
        modifier = modifier,
        colors =
            FloatingTabBarDefaults.colors(
                backgroundColor = backgroundColor,
                accessoryBackgroundColor = backgroundColor,
            ),
        contentKey =
            listOf(
                selectedTabKey,
                items,
                selectedContentColor,
                unselectedContentColor,
            ),
    ) {
        tabScreens.forEach { screen ->
            val isSelected = screen.route == selectedTabKey
            tab(
                key = screen.route,
                title = {
                    Text(
                        text = stringResource(screen.titleId),
                        color = if (isSelected) selectedContentColor else unselectedContentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                icon = {
                    Icon(
                        painter =
                            painterResource(if (isSelected) screen.iconIdActive else screen.iconIdInactive),
                        contentDescription = stringResource(screen.titleId),
                        tint = if (isSelected) selectedContentColor else unselectedContentColor,
                    )
                },
                onClick = { onItemClick(screen, isSelected) },
            )
        }

        searchScreen?.let { screen ->
            val isSelected = screen.route == selectedTabKey
            standaloneTab(
                key = screen.route,
                icon = {
                    Icon(
                        painter =
                            painterResource(if (isSelected) screen.iconIdActive else screen.iconIdInactive),
                        contentDescription = stringResource(screen.titleId),
                        tint = if (isSelected) selectedContentColor else unselectedContentColor,
                    )
                },
                onClick = { onItemClick(screen, isSelected) },
            )
        }
    }
}

private fun isRouteSelected(
    currentRoute: String?,
    route: String,
    navigationItems: List<Screens>,
): Boolean {
    if (currentRoute == null) return false
    if (currentRoute == route) return true
    if (route == navigationItems.firstOrNull()?.route) {
        return currentRoute.startsWith(route)
    }
    return false
}
