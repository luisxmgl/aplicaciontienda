package com.example.aplicaciontienda.ui.screens.home

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicaciontienda.R
import com.example.aplicaciontienda.ui.theme.AccentThread
import com.example.aplicaciontienda.ui.theme.NeutralMutedBrown

/** Equivalente a Home.jsx / activity_main.xml: entrada invitado/admin. */
@Composable
fun HomeScreen(
    onGuestEntry: () -> Unit,
    onAdminEntry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.home_tag),
            color = AccentThread,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.home_title_line1),
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
            // Long-press en el título abre el acceso administrador (mismo comportamiento que MainActivity.tvTitle2)
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(onLongPress = { onAdminEntry() })
            }
        )
        Text(
            text = stringResource(R.string.home_title_line2),
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = NeutralMutedBrown,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onGuestEntry,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentThread)
        ) {
            Text(stringResource(R.string.home_guest_button), fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onAdminEntry,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(stringResource(R.string.home_admin_button))
        }
    }
}
