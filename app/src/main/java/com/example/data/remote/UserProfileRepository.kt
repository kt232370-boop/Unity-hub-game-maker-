package com.example.data.remote

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserProfileRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch; refusing cross-user read." }
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) null
                    else snapshot.toObject(
                        UserProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun syncGoogleUserProfile(
        preferredEmail: String?,
        preferredUsername: String?,
        preferredFullName: String?,
        emailCodeVerified: Boolean
    ): Result<Unit> {
        val currentUser = auth.currentUser
        val docPath = "users/${currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val user = auth.currentUser ?: error("Missing authenticated Firebase user")
            val docRef = db.collection("users").document(uid)
            val existingSnapshot = docRef.get().await()

            val resolvedEmail = (preferredEmail?.takeIf { it.isNotBlank() }
                ?: user.email
                ?: "developer@unity.com").trim().take(254)
            val fallbackName = user.displayName?.takeIf { it.isNotBlank() }
                ?: resolvedEmail.substringBefore("@").ifBlank { "Unity Creator" }
            val resolvedFullName = (preferredFullName?.takeIf { it.isNotBlank() }
                ?: fallbackName).trim().take(80)
            val rawHandle = (preferredUsername?.takeIf { it.isNotBlank() }
                ?: resolvedFullName.lowercase().replace(Regex("[^a-z0-9._-]"), ""))
                .trim()
            val resolvedUsername = rawHandle.ifBlank { "unitydev" }.let {
                if (it.length < 2) "${it}dev" else it.take(40)
            }
            val resolvedPhoto = user.photoUrl?.toString()?.take(512)
            val verified = emailCodeVerified || user.isEmailVerified

            if (existingSnapshot.exists()) {
                val updates = mutableMapOf<String, Any?>(
                    "email" to resolvedEmail,
                    "emailVerified" to verified,
                    "photoUrl" to resolvedPhoto,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (!preferredUsername.isNullOrBlank()) {
                    updates["username"] = resolvedUsername
                }
                if (!preferredFullName.isNullOrBlank()) {
                    updates["fullName"] = resolvedFullName
                }
                @Suppress("UNCHECKED_CAST")
                val cleanUpdates = updates.filterValues { it != null } as Map<String, Any>
                docRef.update(cleanUpdates).await()
            } else {
                val createPayload = mapOf<String, Any?>(
                    "userId" to uid,
                    "email" to resolvedEmail,
                    "username" to resolvedUsername,
                    "fullName" to resolvedFullName,
                    "emailVerified" to verified,
                    "authProvider" to "google",
                    "photoUrl" to resolvedPhoto,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                @Suppress("UNCHECKED_CAST")
                val cleanPayload = createPayload.filterValues { it != null } as Map<String, Any>
                docRef.set(cleanPayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }
}
