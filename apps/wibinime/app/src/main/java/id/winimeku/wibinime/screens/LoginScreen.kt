package id.winimeku.wibinime.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.winimeku.wibinime.network.ApiClient
import id.winimeku.wibinime.network.LoginRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("LOGIN WIBINIME", color = Color.White, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMsg != null) {
            Text(errorMsg!!, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                isLoading = true
                errorMsg = null
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = ApiClient.instance.login(LoginRequest(email, password))
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful && response.body()?.token != null) {
                                onLoginSuccess(response.body()!!.token!!)
                            } else {
                                errorMsg = response.body()?.error ?: "Login Gagal"
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) { errorMsg = e.message }
                    } finally {
                        withContext(Dispatchers.Main) { isLoading = false }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Loading..." else "MASUK")
        }
    }
}
