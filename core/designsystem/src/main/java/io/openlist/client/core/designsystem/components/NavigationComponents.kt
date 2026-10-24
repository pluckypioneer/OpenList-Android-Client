package io.openlist.client.core.designsystem.components

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class AppNavigationItem(
    val label: String,
    val icon: ImageVector,
    val selected: Boolean,
    val enabled: Boolean = true,
    val badgeCount: Int = 0,
    val onClick: () -> Unit,
)

@Composable
fun AppNavigationBar(items: List<AppNavigationItem>, modifier: Modifier = Modifier) {
    NavigationBar(modifier = modifier, containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.selected,
                onClick = item.onClick,
                enabled = item.enabled,
                icon = {
                    BadgedBox(badge = {
                        if (item.badgeCount > 0) Badge { Text(if (item.badgeCount > 99) "99+" else item.badgeCount.toString()) }
                    }) { Icon(item.icon, contentDescription = null) }
                },
                label = { Text(item.label) },
            )
        }
    }
}