package com.relaxmind.app.utils

object ValidationUtils {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")
    private val DATE_REGEX = Regex("^\\d{2}/\\d{2}/\\d{4}$") // dd/MM/yyyy

    /** Returns an error message or null if the value is valid. */
    fun validateName(value: String): String? =
        if (value.isBlank()) "El nombre es obligatorio." else null

    fun validateLastName(value: String): String? =
        if (value.isBlank()) "El apellido es obligatorio." else null

    fun validateBirthDate(value: String): String? = when {
        value.isBlank() -> "La fecha de nacimiento es obligatoria."
        !DATE_REGEX.matches(value) -> "Formato de fecha inválido (dd/MM/yyyy)."
        else -> null
    }

    fun validateEmail(value: String): String? = when {
        value.isBlank() -> "El correo electrónico es obligatorio."
        !EMAIL_REGEX.matches(value) -> "Correo electrónico no válido."
        else -> null
    }

    fun validatePassword(value: String): String? = when {
        value.isBlank() -> "La contraseña es obligatoria."
        value.length < 8 -> "La contraseña debe tener al menos 8 caracteres."
        !value.any { it.isDigit() } -> "La contraseña debe contener al menos un número."
        !value.any { it.isUpperCase() } -> "La contraseña debe contener al menos una letra mayúscula."
        else -> null
    }

    fun validateConfirmPassword(password: String, confirm: String): String? = when {
        confirm.isBlank() -> "Confirma tu contraseña."
        password != confirm -> "Las contraseñas no coinciden."
        else -> null
    }

    fun validateRole(value: String): String? =
        if (value != "patient" && value != "caregiver") "Selecciona un rol válido." else null

    fun validateOtpCode(code: String): String? = when {
        code.isBlank() -> "Ingresa el código de verificación."
        code.length != 6 -> "El código debe tener 6 dígitos."
        !code.all { it.isDigit() } -> "El código solo debe contener números."
        else -> null
    }
}
