package com.photocoach.app.ui.consent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.photocoach.app.R

@Composable
fun CameraConsentScreen(
    deniedOnce: Boolean,
    onAgree: () -> Unit,
    onDeny: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.consent_title), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.consent_body), style = MaterialTheme.typography.bodyLarge)
        if (deniedOnce) {
            Text(stringResource(R.string.consent_denied_hint), color = MaterialTheme.colorScheme.primary)
        }
        Button(onClick = onAgree) { Text(stringResource(R.string.consent_agree)) }
        TextButton(onClick = onDeny) { Text(stringResource(R.string.consent_deny)) }
    }
}
