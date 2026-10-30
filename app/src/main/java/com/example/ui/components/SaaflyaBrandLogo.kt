package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

/**
 * Displays the user's exact uploaded "સાફલ્ય AI GPSC — સફળતાની શરૂઆત" logo image
 * inside a crisp white card with full aspect ratio preserved (no cropping).
 */
@Composable
fun SaaflyaFullLogoCard(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 320.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_saaflya_gpscai),
                contentDescription = "સાફલ્ય AI GPSC — સફળતાની શરૂઆત",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(848f / 576f)
            )
        }
    }
}

/**
 * Displays the user's exact uploaded "સાફલ્ય AI GPSC" logo in the TopAppBar header
 * and User Profile dialog with full aspect ratio preserved (no cropping).
 */
@Composable
fun SaaflyaFeatherEmblemBadge(
    size: Dp = 42.dp,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(size)
            .aspectRatio(848f / 576f),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_saaflya_gpscai),
                contentDescription = "સાફલ્ય AI GPSC Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
