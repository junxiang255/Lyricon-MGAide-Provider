package io.github.proify.lyricon.mgaprovider.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.proify.lyricon.mgaprovider.BuildConfig

@Composable
fun HomeScreen(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 110.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "词慕 · 毛怪助手歌词",
            color = MgColors.TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 26.dp),
        )
        Text(
            text = "把毛怪助手正在播放的歌词交给词慕，在状态栏逐字滚动显示",
            color = MgColors.TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 26.dp, vertical = 8.dp),
        )

        SectionTitle("歌词")
        GroupCard {
            SwitchRow(
                title = "状态栏歌词",
                subtitle = "通过词慕在状态栏显示毛怪助手的歌词",
                checked = enabled,
                onCheckedChange = onToggle,
            )
        }

        SectionTitle("使用说明")
        GroupCard {
            InstructionRow("1", "在 LSPosed 中启用本模块")
            RowDivider()
            InstructionRow("2", "作用域勾选「毛怪助手」")
            RowDivider()
            InstructionRow("3", "强制停止并重新打开毛怪助手")
            RowDivider()
            InstructionRow("4", "在词慕里把歌词来源选为「毛怪助手」")
        }

        SectionTitle("状态")
        GroupCard {
            InfoRow("作用域", "com.sennagi.mgaide")
            RowDivider()
            InfoRow("模块版本", BuildConfig.VERSION_NAME)
            RowDivider()
            InfoRow("歌词来源", "MusicViewModel · 逐字")
        }
        Spacer(Modifier.height(24.dp))
    }
}
