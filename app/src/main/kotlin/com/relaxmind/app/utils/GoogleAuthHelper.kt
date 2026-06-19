package com.relaxmind.app.utils

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.relaxmind.app.BuildConfig
import java.security.MessageDigest
import java.util.UUID

object GoogleAuthHelper {

    suspend fun getGoogleIdToken(context: Context): Result<String> = runCatching {
        val credentialManager = CredentialManager.create(context)
        
        // Use a secure random nonce
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        if (webClientId == "MOCK_WEB_CLIENT_ID") {
            error("Google Web Client ID no configurado. Falta GOOGLE_WEB_CLIENT_ID en local.properties")
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            
            val credential = result.credential
            if (credential is androidx.credentials.CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                googleIdTokenCredential.idToken
            } else {
                error("Credencial devuelta no es válida o no es un ID Token de Google.")
            }
        } catch (e: GetCredentialException) {
            error("Error al obtener la credencial de Google: ${e.message}")
        }
    }
}
