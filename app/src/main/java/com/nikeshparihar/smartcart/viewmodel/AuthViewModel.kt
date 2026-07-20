package com.nikeshparihar.smartcart.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nikeshparihar.smartcart.data.remote.SupabaseApi
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSignUp: Boolean = false,
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val supabase = SupabaseApi.client

    var authState by mutableStateOf<AuthUiState>(AuthUiState())
        private set

    var currentUser by mutableStateOf<UserInfo?>(null)
        private set

    init {
        viewModelScope.launch {
            try {
                supabase.auth.sessionStatus.collect { status ->
                    when (status) {
                        is SessionStatus.Authenticated -> currentUser = status.session.user
                        is SessionStatus.NotAuthenticated -> currentUser = null
                        else -> { /* Ignore Loading or other states */ }
                    }
                }
            } catch (_: Exception) { }
        }
    }

    fun onEmailChange(email: String) {
        authState = authState.copy(email = email, errorMessage = null)
    }

    fun onPasswordChange(password: String) {
        authState = authState.copy(password = password, errorMessage = null)
    }

    fun toggleSignUp() {
        authState = authState.copy(isSignUp = !authState.isSignUp, errorMessage = null)
    }

    fun onEmailAuth() {
        val email = authState.email.trim()
        val password = authState.password
        if (email.isBlank() || password.length < 6) {
            authState = authState.copy(errorMessage = "Email required, password must be 6+ characters.")
            return
        }

        authState = authState.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                if (authState.isSignUp) {
                    supabase.auth.signUpWith(Email) {
                        this.email = email
                        this.password = password
                    }
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        com.nikeshparihar.smartcart.data.local.ShoppingDatabase.getDatabase(getApplication()).clearAllTables()
                    }
                    currentUser = supabase.auth.currentUserOrNull()
                    authState = authState.copy(
                        isLoading = false,
                        errorMessage = if (currentUser == null) "Check your email for a confirmation link!" else null
                    )
                } else {
                    supabase.auth.signInWith(Email) {
                        this.email = email
                        this.password = password
                    }
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        com.nikeshparihar.smartcart.data.local.ShoppingDatabase.getDatabase(getApplication()).clearAllTables()
                    }
                    currentUser = supabase.auth.currentUserOrNull()
                    authState = authState.copy(isLoading = false)
                }
            } catch (e: Exception) {
                // Supabase exceptions tend to dump the entire HTTP request headers and URL. We'll strip that out for a clean UI message.
                val cleanMessage = e.message?.substringBefore("URL:")?.trim() ?: "Authentication failed."
                authState = authState.copy(
                    isLoading = false,
                    errorMessage = cleanMessage
                )
            }
        }
    }

    fun onGoogleSignIn(activityContext: android.app.Activity) {
        authState = authState.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(activityContext)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(SupabaseApi.GOOGLE_WEB_CLIENT_ID)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(activityContext, request)
                val credential = result.credential

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                supabase.auth.signInWith(IDToken) {
                    this.idToken = idToken
                    this.provider = Google
                }

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.nikeshparihar.smartcart.data.local.ShoppingDatabase.getDatabase(getApplication()).clearAllTables()
                }

                currentUser = supabase.auth.currentUserOrNull()
                authState = authState.copy(isLoading = false)
            } catch (e: Exception) {
                authState = authState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Google Sign-In failed."
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Wipe local database for security between accounts
                com.nikeshparihar.smartcart.data.local.ShoppingDatabase.getDatabase(getApplication()).clearAllTables()
                supabase.auth.signOut()
                currentUser = null
            } catch (_: Exception) { }
        }
    }
}
