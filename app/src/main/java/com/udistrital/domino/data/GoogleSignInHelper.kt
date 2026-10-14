package com.udistrital.domino.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * Obtiene el idToken de Google con Credential Manager (reemplazo moderno del
 * GoogleSignInClient deprecado). Luego AuthFirebaseRepository lo cambia por una
 * sesión de Firebase.
 *
 * Requisitos en Firebase Console:
 *  1. Authentication > Sign-in method > Google: Habilitar.
 *  2. Configuración del proyecto > app Android > agregar SHA-1 (./gradlew signingReport).
 *  3. Volver a descargar google-services.json (debe traer "oauth_client" con client_type 3).
 *
 * El client id se busca por nombre para que el proyecto COMPILE aunque el json
 * todavía no lo traiga; en ese caso se devuelve un mensaje claro.
 */
object GoogleSignInHelper {

    sealed class Resultado {
        data class Token(val idToken: String) : Resultado()
        data class Error(val mensaje: String) : Resultado()
        data object Cancelado : Resultado()
    }

    suspend fun obtenerIdToken(activityContext: Context): Resultado {
        val idRecurso = activityContext.resources.getIdentifier(
            "default_web_client_id", "string", activityContext.packageName
        )
        if (idRecurso == 0) {
            return Resultado.Error("Google no está configurado: agrega la SHA-1 y descarga de nuevo google-services.json")
        }

        val opcion = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // muestra todas las cuentas del dispositivo
            .setServerClientId(activityContext.getString(idRecurso))
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(opcion).build()

        return try {
            val credencial = CredentialManager.create(activityContext)
                .getCredential(activityContext, request).credential
            if (credencial is CustomCredential &&
                credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                Resultado.Token(GoogleIdTokenCredential.createFrom(credencial.data).idToken)
            } else {
                Resultado.Error("Credencial no reconocida")
            }
        } catch (e: GetCredentialCancellationException) {
            Resultado.Cancelado
        } catch (e: GetCredentialException) {
            Resultado.Error(e.message ?: "No se pudo iniciar sesión con Google")
        }
    }
}
