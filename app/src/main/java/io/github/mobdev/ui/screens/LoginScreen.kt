package io.github.mobdev.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.github.mobdev.R
import io.github.mobdev.ui.ChatViewModel

@Composable
fun LoginScreen(viewModel: ChatViewModel, onLoginSuccess: () -> Unit) {
    val name by viewModel.loginName.collectAsState()
    val password by viewModel.loginPassword.collectAsState()
    val isLoggingIn by viewModel.isLoggingIn.collectAsState()
    val showErrorDialog by viewModel.showErrorDialog.collectAsState()

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissErrorDialog() },
            title = { Text(stringResource(R.string.error_title)) },
            text = { Text(stringResource(R.string.invalid_credentials)) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissErrorDialog() }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        OutlinedTextField(
            value = name, onValueChange = { viewModel.updateLoginName(it) },
            label = { Text(stringResource(R.string.login_hint)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password, onValueChange = { viewModel.updateLoginPassword(it) },
            label = { Text(stringResource(R.string.password_hint)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (isLoggingIn) CircularProgressIndicator()
        else {
            Button(
                onClick = {
                    if (name.isNotBlank() && password.isNotBlank()) {
                        viewModel.login(onSuccess = onLoginSuccess)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) { Text(stringResource(R.string.btn_login)) }
        }
    }
}