package io.github.proify.lyricon.mgaprovider.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 分组小标题，如「歌词」「使用说明」 */
@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        color = MgColors.TextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(start = 26.dp, top = 20.dp, bottom = 8.dp),
    )
}

/** Miuix 风格的大圆角白色卡片 */
@Composable
fun GroupCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        shape = RoundedCornerShape(24.dp),
        color = MgColors.Card,
        shadowElevation = 0.dp,
    ) {
        Column(Modifier.padding(vertical = 6.dp), content = content)
    }
}

@Composable
fun RowDivider() {
    HorizontalDivider(
        color = MgColors.Divider,
        thickness = 1.dp,
        modifier = Modifier.padding(start = 18.dp, end = 18.dp),
    )
}

/** 开关行：标题 + 描述 + 橙色开关 */
@Composable
fun SwitchRow(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = MgColors.TextPrimary, fontSize = 17.sp)
            if (subtitle != null) {
                Text(
                    subtitle,
                    color = MgColors.TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MgColors.Orange,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFD9D9DE),
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}

/** 说明条目：序号圆点 + 文本 */
@Composable
fun InstructionRow(index: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(MgColors.OrangeSoft),
            contentAlignment = Alignment.Center,
        ) {
            Text(index, color = MgColors.Orange, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.width(12.dp))
        Text(text, color = MgColors.TextPrimary, fontSize = 15.sp)
    }
}

/** 信息行：左标题右值 */
@Composable
fun InfoRow(title: String, value: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = MgColors.TextPrimary, fontSize = 16.sp)
        Spacer(Modifier.weight(1f))
        Text(
            value,
            color = MgColors.TextSecondary,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** 底部导航（两页） */
@Composable
fun MgBottomBar(
    page: Int,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        shadowElevation = 8.dp,
    ) {
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
            BottomItem(
                label = "首页",
                icon = Icons.Filled.Home,
                selected = page == 0,
                modifier = Modifier.weight(1f),
            ) { onSelect(0) }
            BottomItem(
                label = "关于",
                icon = Icons.Filled.Info,
                selected = page == 1,
                modifier = Modifier.weight(1f),
            ) { onSelect(1) }
        }
    }
}

@Composable
private fun BottomItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) MgColors.Orange else MgColors.TextSecondary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            color = if (selected) MgColors.Orange else MgColors.TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
