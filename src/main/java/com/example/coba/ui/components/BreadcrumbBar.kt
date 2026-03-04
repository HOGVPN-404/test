package com.example.coba.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coba.ui.screens.home.BreadcrumbNode

@Composable
fun BreadcrumbBar(
    nodes: List<BreadcrumbNode>,
    onNodeClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        nodes.forEachIndexed { index, node ->
            Text(
                text = node.label,
                color = if (index == nodes.lastIndex) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                },
                fontWeight = if (index == nodes.lastIndex) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.clickable { onNodeClick(index) }
            )
            if (index != nodes.lastIndex) {
                Text(
                    text = ">",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f)
                )
            }
        }
    }
}
