package com.photocoach.app.ui.consent

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.photocoach.app.R
import com.photocoach.app.ui.theme.CameraAccessLayout

@Composable
fun CameraConsentScreen(
    deniedOnce: Boolean,
    onAgree: () -> Unit,
    onDeny: () -> Unit,
) {
    CameraAccessLayout(
        title = stringResource(R.string.consent_title),
        body = stringResource(R.string.consent_body),
        icon = Icons.Rounded.CameraAlt,
        primaryLabel = stringResource(R.string.consent_agree),
        onPrimary = onAgree,
        secondaryLabel = stringResource(R.string.consent_deny),
        onSecondary = onDeny,
        notice = if (deniedOnce) stringResource(R.string.consent_denied_hint) else null,
    )
}
