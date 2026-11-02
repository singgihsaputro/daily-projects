package com.dailyprojects.shimmer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** A rounded bar standing in for one line of text. */
@Composable
fun SkeletonLine(
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
    spec: ShimmerSpec = ShimmerSpec.Light,
) {
    Box(modifier.height(height).skeleton(true, RoundedCornerShape(height / 2), spec))
}

/** A circle standing in for an avatar. */
@Composable
fun SkeletonCircle(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    spec: ShimmerSpec = ShimmerSpec.Light,
) {
    Box(modifier.size(size).skeleton(true, CircleShape, spec))
}

/** An avatar plus a title line and two summary lines: the usual list-row placeholder. */
@Composable
fun SkeletonListItem(
    modifier: Modifier = Modifier,
    spec: ShimmerSpec = ShimmerSpec.Light,
) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkeletonCircle(spec = spec)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonLine(Modifier.fillMaxWidth(0.6f), height = 16.dp, spec = spec)
            SkeletonLine(Modifier.fillMaxWidth(), spec = spec)
            SkeletonLine(Modifier.fillMaxWidth(0.8f), spec = spec)
        }
    }
}

/** [count] list-row placeholders stacked vertically. */
@Composable
fun SkeletonList(
    count: Int,
    modifier: Modifier = Modifier,
    spec: ShimmerSpec = ShimmerSpec.Light,
) {
    Column(modifier) {
        repeat(count) { SkeletonListItem(spec = spec) }
    }
}
