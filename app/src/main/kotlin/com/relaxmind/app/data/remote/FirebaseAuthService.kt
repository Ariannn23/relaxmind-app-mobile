package com.relaxmind.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class FirebaseAuthService {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    suspend fun register(
        email: String,
        password: String
    ): Result<FirebaseUser> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user ?: error("Firebase did not return a registered user.")
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<FirebaseUser> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        result.user ?: error("Firebase did not return an authenticated user.")
    }

    suspend fun loginWithGoogleCredential(idToken: String): Result<FirebaseUser> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        result.user ?: error("Firebase did not return an authenticated user.")
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun sendVerificationEmail(): Result<Unit> = runCatching {
        val user = auth.currentUser ?: error("No authenticated user is available.")
        user.sendEmailVerification().await()
    }

    suspend fun resetPassword(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun isLoggedIn(): Boolean = auth.currentUser != null

    suspend fun reauthenticate(password: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: error("No authenticated user is available.")
        val email = user.email ?: error("User email not available.")
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
        user.reauthenticate(credential).await()
    }
}
