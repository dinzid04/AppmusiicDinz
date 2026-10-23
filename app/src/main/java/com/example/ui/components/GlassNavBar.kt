@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.example.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.MusicItem
import com.example.data.model.ROW_ART_PX
import com.example.data.model.artworkAt
import com.example.ui.components.floatingtabbar.FloatingTabBar
import com.example.ui.components.floatingtabbar.FloatingTabBarDefaults
import com.example.ui.components.floatingtabbar.FloatingTabBarScrollConnection

@Composable
fun GlassNavBar(
    tabs: List<BottomTab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    scrollConnection: FloatingTabBarScrollConnection,
    song: MusicItem?,
    isPlaying: Boolean,
    isLoading: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pillShape = remember { RoundedCornerShape(percent = 50) }
    val contentColor = glassContentColor()
    val selectedColor = contentColor
    val unselectedColor = contentColor.copy(alpha = 0.65f)
    val haptic = LocalHapticFeedback.current

    val currentOnTabSelected by rememberUpdatedState(onTabSelected)
    val standaloneIndex = tabs.lastIndex

    val barTint = glassBarTint()
    val glassSurface: @Composable () -> Modifier = { Modifier.liquidGlass(shape = pillShape, surfaceTint = barTint) }

    FloatingTabBar(
        selectedTabKey = selectedIndex,
        scrollConnection = scrollConnection,
        modifier = modifier
            .windowInsetsPadding(floatingBarInsets)
            .padding(horizontal = BAR_GUTTER)
            .padding(bottom = 2.dp)
            .fillMaxWidth(),
        tabBarContentModifier = glassSurface,
        inlineAccessory = song?.let { current ->
            { accessoryModifier, _ ->
                GlassNowPlaying(
                    song = current,
                    isInline = true,
                    isPlaying = isPlaying,
                    isLoading = isLoading,
                    contentColor = contentColor,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onExpand = onExpand,
                    modifier = accessoryModifier.then(glassSurface()),
                )
            }
        },
        expandedAccessory = song?.let { current ->
            { accessoryModifier, _ ->
                GlassNowPlaying(
                    song = current,
                    isInline = false,
                    isPlaying = isPlaying,
                    isLoading = isLoading,
                    contentColor = contentColor,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onExpand = onExpand,
                    modifier = accessoryModifier.fillMaxWidth().then(glassSurface()),
                )
            }
        },
        colors = FloatingTabBarDefaults.colors(
            backgroundColor = Color.Transparent,
            accessoryBackgroundColor = Color.Transparent,
            indicatorColor = glassIndicatorColor(),
        ),
        elevations = FloatingTabBarDefaults.elevations(
            inlineElevation = 0.dp,
            expandedElevation = 0.dp,
        ),
        sizes = FloatingTabBarDefaults.sizes(
            tabBarContentPadding = PaddingValues(PILL_INSET),
            tabExpandedContentPadding = PaddingValues(vertical = TAB_VERTICAL_PADDING),
        ),
        contentKey = remember(selectedIndex, tabs, contentColor) {
            listOf(selectedIndex, tabs, contentColor)
        },
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = index == selectedIndex
            val tint = if (isSelected) selectedColor else unselectedColor
            val onClick = {
                if (!isSelected) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                currentOnTabSelected(index)
            }
            if (index == standaloneIndex) {
                standaloneTab(
                    key = index,
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = tint,
                            modifier = Modifier.size(25.dp),
                        )
                    },
                    onClick = onClick,
                )
            } else {
                tab(
                    key = index,
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = tint,
                            modifier = Modifier.size(25.dp),
                        )
                    },
                    title = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = tint,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = TAB_ICON_LABEL_GAP),
                        )
                    },
                    onClick = onClick,
                )
            }
        }
    }
}

@Composable
private fun GlassNowPlaying(
    song: MusicItem,
    isInline: Boolean,
    isPlaying: Boolean,
    isLoading: Boolean,
    contentColor: Color,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pressSource = remember { MutableInteractionSource() }
    val isPressed by pressSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 1.04f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "accessoryPressScale",
    )

    val artSize = if (isInline) 32.dp else 40.dp
    val artCorner = if (isInline) artSize / 2 else 8.dp
    val glyphSlot = if (isInline) 32.dp else 40.dp
    val glyphSize = if (isInline) 24.dp else 32.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .then(modifier)
            .clickable(
                interactionSource = pressSource,
                indication = null,
                onClick = onExpand,
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (isInline) 8.dp else 12.dp,
                    vertical = if (isInline) 4.dp else 8.dp,
                ),
        ) {
            AsyncImage(
                model = song.highResThumbnail.artworkAt(ROW_ART_PX),
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(artSize)
                    .clip(RoundedCornerShape(artCorner))
                    .background(Color(0xFF232329)),
            )
            Spacer(Modifier.width(if (isInline) 8.dp else 10.dp))
            if (isInline) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = song.cleanArtist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (isLoading) {
                Box(Modifier.size(glyphSlot), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = contentColor,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(if (isInline) 18.dp else 22.dp),
                    )
                }
            } else {
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(glyphSlot),
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = contentColor,
                        modifier = Modifier.size(glyphSize),
                    )
                }
            }
            if (!isInline) {
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(glyphSlot),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = contentColor,
                        modifier = Modifier.size(glyphSize),
                    )
                }
            }
        }
    }
}
