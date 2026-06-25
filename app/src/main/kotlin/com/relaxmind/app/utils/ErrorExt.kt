package com.relaxmind.app.utils

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

fun Throwable?.toUserFriendlyMessage(
    fallback: String = "Ocurrió un error inesperado. Inténtalo de nuevo."
): String {
    val exception = this
    val msg = exception?.message ?: exception?.localizedMessage ?: ""
    
    // Map Firebase auth specific exceptions
    if (exception is FirebaseAuthInvalidCredentialsException) {
        return "Correo o contraseña incorrectos."
    }
    if (exception is FirebaseAuthInvalidUserException) {
        return "Usuario no encontrado. Revisa tu correo."
    }
    if (exception is FirebaseAuthUserCollisionException) {
        return "Ya existe una cuenta registrada con este correo."
    }
    if (exception is FirebaseAuthWeakPasswordException) {
        return "La contraseña es demasiado débil. Intenta con una más larga."
    }
    if (exception is FirebaseNetworkException) {
        return "Error de red. Revisa tu conexión a internet e inténtalo de nuevo."
    }
    
    // Check specific strings from Google Auth or other internal errors
    return when {
        msg.contains("16") && (msg.contains("Credential") || msg.contains("GetCredential")) -> 
            "El inicio de sesión con Google fue cancelado o no se pudo completar."
        msg.contains("16") && msg.contains("CANCELED") -> 
            "El inicio de sesión con Google fue cancelado."
        msg.contains("CONFIGURATION_NOT_FOUND") -> 
            "El servicio de autenticación no está disponible por ahora."
        msg.contains("email address is badly formatted") -> 
            "El correo electrónico no tiene un formato válido."
        msg.contains("A network error") -> 
            "Error de red. Revisa tu conexión a internet e inténtalo de nuevo."
        msg.contains("network connection") -> 
            "Error de red. Revisa tu conexión a internet."
        else -> fallback
    }
}
