package com.photocoach.app.ui.viewfinder

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NoPhotography
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.photocoach.app.R
import com.photocoach.app.ui.theme.CameraAccessLayout

@Composable
fun PermissionDeniedScreen(
    canRequestAgain: Boolean,
    onRetry: () -> Unit,
    onSettings: () -> Unit,
) {
    CameraAccessLayout(
        title = stringResource(R.string.permission_denied_title),
        body = stringResource(
            if (canRequestAgain) R.string.permission_denied_body else R.string.permission_denied_settings_body,
        ),
        icon = Icons.Rounded.NoPhotography,
        primaryLabel = stringResource(
            if (canRequestAgain) R.string.permission_retry else R.string.permission_settings,
        ),
        onPrimary = if (canRequestAgain) onRetry else onSettings,
        secondaryLabel = if (canRequestAgain) stringResource(R.string.permission_settings) else null,
        onSecondary = if (canRequestAgain) onSettings else null,
    )
}
