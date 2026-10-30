package io.github.proify.lyricon.mgaprovider.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.proify.lyricon.mgaprovider.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AboutScreen(onOpenUrl: (String) -> Unit) {
    val context = LocalContext.current
    var projects by remember { mutableStateOf(GithubRepository.cached(context)) }
    var refreshing by remember { mutableStateOf(false) }

    // GitHub API 每天最多拉取一次，其余时间用缓存
    LaunchedEffect(Unit) {
        if (!GithubRepository.shouldRefresh(context)) return@LaunchedEffect
        refreshing = true
        val fresh = withContext(Dispatchers.IO) { GithubRepository.refresh(context) }
        if (!fresh.isNullOrEmpty()) projects = fresh
        refreshing = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 110.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "关于",
            color = MgColors.TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 26.dp),
        )
        Spacer(Modifier.height(16.dp))

        // 头像 + 名字
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .clickable { onOpenUrl(GithubRepository.PROFILE_URL) },
            shape = RoundedCornerShape(24.dp),
            color = MgColors.Card,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.avatar),
                    contentDescription = "头像",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = GithubRepository.NICKNAME,
                        color = MgColors.TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "@" + GithubRepository.LOGIN,
                        color = MgColors.TextSecondary,
                        fontSize = 13.sp,
                    )
                    Text(
                        text = GithubRepository.BIO,
                        color = MgColors.TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        SectionTitle(if (refreshing) "我的 GitHub 项目 · 更新中" else "我的 GitHub 项目")
        GroupCard {
            if (projects.isEmpty()) {
                InfoRow("暂无缓存", "点击打开 GitHub") {
                    onOpenUrl(GithubRepository.PROFILE_URL)
                }
            } else {
                projects.forEachIndexed { index, project ->
                    ProjectRow(project) { onOpenUrl(project.url) }
                    if (index != projects.lastIndex) RowDivider()
                }
            }
        }

        SectionTitle("链接")
        GroupCard {
            InfoRow("GitHub 主页", "github.com/" + GithubRepository.LOGIN) {
                onOpenUrl(GithubRepository.PROFILE_URL)
            }
            RowDivider()
            InfoRow("词慕项目", "github.com/tomakino/lyricon") {
                onOpenUrl("https://github.com/tomakino/lyricon")
            }
            RowDivider()
            InfoRow("参考实现", "github.com/tomakino/LyricProvider") {
                onOpenUrl("https://github.com/tomakino/LyricProvider")
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ProjectRow(project: GithubProject, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MgColors.OrangeSoft),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = project.name.take(1).uppercase(),
                color = MgColors.Orange,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = project.name,
                    color = MgColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (project.isFork) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "fork",
                        color = MgColors.TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x11000000))
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                    )
                }
            }
            Text(
                text = project.description,
                color = MgColors.TextSecondary,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
            Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = project.language,
                    color = MgColors.TextSecondary,
                    fontSize = 11.sp,
                )
                Text(
                    text = "  ★ " + project.stars,
                    color = MgColors.TextSecondary,
                    fontSize = 11.sp,
                )
            }
        }
    }
}
