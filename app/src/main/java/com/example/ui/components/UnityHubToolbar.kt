package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.HubBadgeDark
import com.example.ui.theme.HubBadgeText
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.ToolbarBorder
import com.example.ui.theme.ToolbarWhite
import com.example.ui.viewmodel.HubTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnityHubToolbar(
    activeTab: HubTab,
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onQuickAddProjectClick: () -> Unit,
    onQuickWebsiteClick: () -> Unit,
    onAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ToolbarWhite,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("unity_hub_white_toolbar")
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ToolbarWhite,
                    navigationIconContentColor = InkPrimary,
                    titleContentColor = InkPrimary,
                    actionIconContentColor = InkPrimary
                ),
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("toolbar_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back),
                                tint = InkPrimary
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onQuickWebsiteClick,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("toolbar_website_shortcut")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = stringResource(R.string.tab_website),
                                tint = InkPrimary
                            )
                        }
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.testTag("unity_hub_center_logo")
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.unity_logo),
                            contentDescription = stringResource(R.string.cd_unity_hub_logo),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.height(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(HubBadgeDark)
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "HUB",
                                color = HubBadgeText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.1.sp
                                )
                            )
                        }
                    }
                },
                actions = {
                    if (activeTab == HubTab.PROJECTS) {
                        IconButton(
                            onClick = onQuickAddProjectClick,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("toolbar_add_project_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = stringResource(R.string.cd_add_project),
                                tint = InkPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onAccountClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("toolbar_account_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = stringResource(R.string.cd_account),
                            tint = InkPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
            HorizontalDivider(thickness = 1.dp, color = ToolbarBorder)
        }
    }
}
