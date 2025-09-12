package com.example.smartcropadvisory.viewmodels

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartcropadvisory.UserRole // Import your UserRole enum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModelFactory for AuthViewModel - This should be outside or a companion object if preferred
class AuthViewModelFactory(private val applicationContext: Context) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}") // More descriptive error
    }
}

class AuthViewModel(private val applicationContext: Context) : ViewModel() {

    private val prefs: SharedPreferences =
        applicationContext.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)

    private val _userRole = MutableStateFlow(UserRole.UNKNOWN)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    init {
        Log.d("AuthViewModel", "AuthViewModel Initialized. Loading user session...")
        loadUserSession()
    }

    private fun loadUserSession() {
        viewModelScope.launch {
            val storedRoleName = prefs.getString("USER_ROLE", null)
            val storedUserId = prefs.getString("USER_ID", null)
            val explicitlyLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false) // Check this flag

            if (explicitlyLoggedIn && storedRoleName != null && storedUserId != null) {
                try {
                    _userRole.value = UserRole.valueOf(storedRoleName) // Assumes storedRoleName matches enum names
                    _userId.value = storedUserId
                    _isUserLoggedIn.value = true
                    Log.d("AuthViewModel", "Session loaded: Role=${_userRole.value}, UID=$storedUserId, LoggedIn=true")
                } catch (e: IllegalArgumentException) {
                    Log.e("AuthViewModel", "Invalid role name '$storedRoleName' found in SharedPreferences. Logging out.", e)
                    // Treat as corrupted session, effectively log out
                    logoutUserInternal() // Call an internal version to avoid loop if logoutUser also calls loadUserSession
                }
            } else {
                _userRole.value = UserRole.UNKNOWN
                _isUserLoggedIn.value = false
                _userId.value = null // Ensure userId is also cleared
                Log.d("AuthViewModel", "No active session found or IS_LOGGED_IN is false.")
                // Clear prefs if only partial data exists or not explicitly logged in
                if (!explicitlyLoggedIn) {
                    with(prefs.edit()) {
                        remove("USER_ROLE")
                        remove("USER_ID")
                        // IS_LOGGED_IN is already false or removed
                        apply()
                    }
                }
            }
        }
    }

    // In AuthViewModel.kt -> loginUser function
    fun loginUser(email: String, pass: String, role: UserRole) {
        viewModelScope.launch {
            Log.d("AuthViewModel", "Attempting login for role: $role with email: $email")
            val simulatedUserId = "uid_${role.name.lowercase()}_${email.hashCode()}_${System.currentTimeMillis()}"

            _userRole.value = role // <<<< THIS IS WHERE IT'S SET
            _userId.value = simulatedUserId
            _isUserLoggedIn.value = true

            Log.d("AuthViewModel", "AuthViewModel state after login attempt: _userRole=${_userRole.value}, _isUserLoggedIn=${_isUserLoggedIn.value}")

            with(prefs.edit()) {
                putString("USER_ROLE", role.name)
                putString("USER_ID", simulatedUserId)
                putBoolean("IS_LOGGED_IN", true)
                apply()
            }
            Log.d("AuthViewModel", "Login successful, prefs updated: Role=${role}, UID=$simulatedUserId")
        }
    }

    fun registerUser(email: String, pass: String, name: String, role: UserRole) {
        viewModelScope.launch {
            // --- TODO: Implement actual backend registration ---
            Log.d("AuthViewModel", "Attempting registration for role: $role, Name: $name, Email: $email")
            // After successful registration with backend:
            Log.d("AuthViewModel", "Registration successful (simulated) for $name ($role). User should now login.")
            // You might want to automatically log them in by calling loginUser() or navigate to the login screen.
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            logoutUserInternal()
        }
    }

    private fun logoutUserInternal() { // To avoid potential issues if called from loadUserSession
        // TODO: Clear any backend session tokens (e.g., Firebase signOut)
        _userRole.value = UserRole.UNKNOWN
        _userId.value = null // Clear the userId
        _isUserLoggedIn.value = false // This state change should trigger navigation

        with(prefs.edit()) {
            remove("USER_ROLE")
            remove("USER_ID")
            putBoolean("IS_LOGGED_IN", false) // Explicitly set to false or remove
            // remove("IS_LOGGED_IN") // Alternative: just remove it
            apply()
        }
        Log.d("AuthViewModel", "User logged out. isUserLoggedIn set to false.")
    }
}
