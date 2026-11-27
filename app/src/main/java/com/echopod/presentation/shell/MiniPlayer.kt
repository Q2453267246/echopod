package com.echopod.presentation.shell

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echopod.R
import com.echopod.core.designsystem.theme.EchoPodTheme
import com.echopod.domain.playback.model.NowPlaying

/** 迷你播放器卡片阴影高度。 */
private val MiniPlayerElevation = 6.dp

/** 底部进度线粗细。 */
private val ProgressHeight = 2.dp

/**
 * 悬浮迷你播放器（无状态）。
 *
 * 设计稿：PNG/01-手机/01-首页.png（Compact）、PNG/02-折叠屏/02-首页-竖向.png（Medium）。
 * 摆放位置（对齐、外边距、系统 inset）由调用方通过 [modifier] 决定，组件本身只负责卡片内部。
 *
 * @param nowPlaying 正在播放的单集。
 * @param style 尺寸规格，见 [MiniPlayerStyle]。
 * @param onClick 点击封面 / 标题区域，进入全屏播放器。
 * @param onTogglePlayback 播放 / 暂停。
 * @param onSeekBack 快退 15 秒（仅 Medium 显示）。
 * @param onSeekForward 快进 30 秒（仅 Medium 显示）。
 */
@Composable
fun MiniPlayer(
    nowPlaying: NowPlaying,
    style: MiniPlayerStyle,
    onClick: () -> Unit,
    onTogglePlayback: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = EchoPodTheme.colors
    val shape = RoundedCornerShape(style.cornerRadius)
    val remaining =
        if (style.showRemaining) stringResource(R.string.mini_player_remaining, nowPlaying.remainingMinutes) else null
    val subtitle = miniPlayerSubtitle(nowPlaying.podcastTitle, nowPlaying.speed, remaining)

    Box(
        modifier
            .fillMaxWidth()
            .height(style.height)
            .shadow(MiniPlayerElevation, shape)   // shadow 默认 clip = true，后续背景与内容都裁成圆角
            .background(colors.card),
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(start = style.paddingStart, end = style.paddingEnd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(style.spacing),
        ) {
            // 封面 + 文字：整块可点击 → 全屏播放器
            Row(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(onClickLabel = stringResource(R.string.action_open_player), onClick = onClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(style.spacing),
            ) {
                CoverPlaceholder(nowPlaying.podcastTitle, style.coverSize)
                Column(Modifier.weight(1f)) {
                    Text(
                        text = nowPlaying.episodeTitle,
                        style = MaterialTheme.typography.titleSmall,   // 14 / 600
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,    // 12 / 400
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (style.showSkipButtons) {
                SkipButton(
                    iconRes = R.drawable.ic_replay,
                    seconds = 15,
                    contentDescription = stringResource(R.string.action_seek_back),
                    onClick = onSeekBack,
                )
            }
            PlayPauseButton(
                isPlaying = nowPlaying.isPlaying,
                filled = style.filledPlayButton,
                onClick = onTogglePlayback,
            )
            if (style.showSkipButtons) {
                SkipButton(
                    iconRes = R.drawable.ic_forward,
                    seconds = 30,
                    contentDescription = stringResource(R.string.action_seek_forward),
                    onClick = onSeekForward,
                )
            }
        }

        MiniPlayerProgress(
            progress = nowPlaying.progress,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = style.progressInset),
        )
    }
}

/** 封面占位：品牌容器底色 + 播客名首字。接入真实封面后替换为图片。 */
@Composable
private fun CoverPlaceholder(podcastTitle: String, size: Dp) {
    val colors = EchoPodTheme.colors
    Box(
        Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.brandContainer),
    ) {
        Text(
            text = podcastTitle.take(1),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 5.dp, bottom = 3.dp),
            color = colors.onBrandContainer,
            fontSize = 20.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * 播放 / 暂停按钮，48dp。
 * - Compact：透明底，图标 24dp textPrimary；
 * - Medium（[filled]）：墨色圆形，图标 22dp onInkButton。
 */
@Composable
private fun PlayPauseButton(isPlaying: Boolean, filled: Boolean, onClick: () -> Unit) {
    val colors = EchoPodTheme.colors
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (filled) colors.inkButton else Color.Transparent,
            contentColor = if (filled) colors.onInkButton else colors.textPrimary,
        ),
    ) {
        Icon(
            painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
            contentDescription = stringResource(if (isPlaying) R.string.action_pause else R.string.action_play),
            modifier = Modifier.size(if (filled) 22.dp else 24.dp),
        )
    }
}

/** 快退 / 快进按钮：26dp 圆弧箭头 + 居中的秒数（秒数不参与无障碍朗读）。 */
@Composable
private fun SkipButton(
    @DrawableRes iconRes: Int,
    seconds: Int,
    contentDescription: String,
    onClick: () -> Unit,
) {
    // 数字是图标的一部分，用 dp 换算字号，不随系统字体缩放撑破图标
    val labelSize = with(LocalDensity.current) { 7.5.dp.toSp() }
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        colors = IconButtonDefaults.iconButtonColors(contentColor = EchoPodTheme.colors.textPrimary),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(26.dp),
            )
            Text(
                text = seconds.toString(),
                modifier = Modifier
                    .offset(y = 1.dp)                 // 设计稿数字基线略低于圆心
                    .clearAndSetSemantics {},
                fontSize = labelSize,
                lineHeight = labelSize,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/** 贴底 2dp 进度线：轨道 outline，已播部分 brand。 */
@Composable
private fun MiniPlayerProgress(progress: Float, modifier: Modifier = Modifier) {
    val colors = EchoPodTheme.colors
    val value = progress.coerceIn(0f, 1f)
    Box(
        modifier
            .fillMaxWidth()
            .height(ProgressHeight)
            .progressSemantics(value)
            .background(colors.outline),
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(value)
                .background(colors.brand),
        )
    }
}

// ---- 预览：与 01-首页.png / 14-首页-深色.png / 02-首页-竖向.png 对比 ----

private val PreviewNowPlaying = NowPlaying(
    episodeTitle = "在深夜写代码的人，都在想些什么",
    podcastTitle = "夜航电台",
    speed = 1.2f,
    progress = 0.62f,
    remainingMinutes = 23,
    isPlaying = true,
)

@Preview(name = "MiniPlayer · Compact · Light", widthDp = 390)
@Composable
private fun MiniPlayerCompactLightPreview() {
    EchoPodTheme(darkTheme = false) {
        MiniPlayer(PreviewNowPlaying, MiniPlayerStyle.Compact, {}, {}, {}, {}, Modifier.padding(8.dp))
    }
}

@Preview(name = "MiniPlayer · Compact · Dark", widthDp = 390)
@Composable
private fun MiniPlayerCompactDarkPreview() {
    EchoPodTheme(darkTheme = true) {
        MiniPlayer(PreviewNowPlaying, MiniPlayerStyle.Compact, {}, {}, {}, {}, Modifier.padding(8.dp))
    }
}

@Preview(name = "MiniPlayer · Medium · Light", widthDp = 616)
@Composable
private fun MiniPlayerMediumLightPreview() {
    EchoPodTheme(darkTheme = false) {
        MiniPlayer(PreviewNowPlaying, MiniPlayerStyle.Medium, {}, {}, {}, {}, Modifier.padding(24.dp))
    }
}