package com.trulyao.bookie.controllers

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import com.trulyao.bookie.daos.UserDao
import com.trulyao.bookie.entities.Role
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.lib.AppException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.Base64
import java.util.Date
import kotlin.concurrent.Volatile

data class CreateUserData(
    var firstName: String,
    var lastName: String,
    var email: String,
    var dateOfBirth: Date,
    var password: String,
    var confirmPassword: String,
)

val nameRegex = Regex("^[a-zA-Z]{3,}$")
val emailRegex = Regex("^[a-zA-Z0-9._-]+@bookie\\.ac\\.uk$")

class UserController private constructor(
    private val dao: UserDao,
    private val dispatcher: CoroutineDispatcher,
) {
    companion object {
        @Volatile
        private var instance: UserController? = null
        fun getInstance(
            dao: UserDao,
            dispatcher: CoroutineDispatcher,
        ): UserController {
            return if (this.instance != null) {
                this.instance!!
            } else {
                synchronized(this) {
                    instance ?: UserController(
                        dao,
                        dispatcher
                    ).also { repo -> instance = repo }
                }
            }
        }
    }

    suspend fun signIn(email: String, password: String): User {
        if (email.isEmpty()) throw AppException("No email address provided")
        if (password.isEmpty()) throw AppException("No password provided")

        if (!email.trim().matches(emailRegex)) {
            throw AppException("Invalid email address provided, must only contain alphanumeric characters, ., _ and - and end with @bookie.ac.uk")
        }

        if (password.trim().isEmpty()) throw AppException("Password is required")

        val user = withContext(dispatcher) {
            dao.findByEmail(email.trim()) ?: throw AppException("Account not found");
        }

        if (comparePassword(password = password, hashedPassword = user.password).not()) {
            throw AppException("Invalid credentials provided")
        }

        return user
    }

    suspend fun signUp(data: CreateUserData, role: Role = Role.Student): Int {
        data.firstName = data.firstName.trim().lowercase()
        data.lastName = data.lastName.trim().lowercase()
        data.email = data.email.trim().lowercase()
        data.password = data.password.trim()

        if (!data.firstName.matches(nameRegex)) throw AppException("First name must be at least 3 characters and only contain alphabets")
        if (!data.lastName.matches(nameRegex)) throw AppException("Last name must be at least 3 characters and only contain alphabets")
        if (!data.email.matches(emailRegex)) throw AppException("Invalid email address provided, must only contain alphanumeric characters, ., _ and - and end with @bookie.ac.uk")
        if (data.password.isEmpty()) throw AppException("Password is required")
        if (data.password != data.confirmPassword) throw AppException("Passwords are not the same!")

        val existingUser = withContext(dispatcher) {
            dao.findByEmail(data.email)
        };

        if (existingUser != null && existingUser.id!! > 0) throw AppException("An account with this email already exists")

        val insertedUserID = withContext(dispatcher) {
            dao.createUser(
                User(
                    firstName = data.firstName,
                    lastName = data.lastName,
                    email = data.email,
                    password = hashPassword(data.password),
                    netHash = generateNetworkHash(data.firstName, data.email),
                    role = role,
                    dateOfBirth = data.dateOfBirth,
                    createdAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                    modifiedAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                )
            )
        }

        return insertedUserID.toInt()
    }

    suspend fun changePassword(
        oldPassword: String,
        newPassword: String,
        newPasswordConfirmation: String,
    ) {

    }

    fun createDefaultAdmin() {
        dao.createUser(
            User(
                firstName = "julian",
                lastName = "blake",
                email = "jb@bookie.ac.uk",
                password = this.hashPassword("Admin123"),
                role = Role.Admin,
                netHash = generateNetworkHash("Julian", "jb@bookie.ac.uk"),
                dateOfBirth = Date.from(
                    LocalDate.of(1990, 8, 21)
                        .atStartOfDay()
                        .toInstant(ZoneOffset.UTC)
                ),
                createdAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                modifiedAt = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
            ),
        )
    }

    private fun hashPassword(password: String): String {
        val argon2 = Argon2Kt()
        val hashedPassword = argon2.hash(
            password = password.toByteArray(),
            mode = Argon2Mode.ARGON2_I,
            salt = "definitelyasecurepasswordhash".toByteArray(),
            tCostInIterations = 5,
            mCostInKibibyte = 65536
        )

        return hashedPassword.encodedOutputAsString()
    }

    private fun comparePassword(password: String, hashedPassword: String): Boolean {
        val argon2 = Argon2Kt()
        return argon2.verify(
            mode = Argon2Mode.ARGON2_I,
            encoded = hashedPassword,
            password = password.toByteArray()
        )
    }

    // Generate a unique network name from a combination of a couple of unique identifiers
    private fun generateNetworkHash(firstName: String, email: String): String {
        val timestamp = LocalTime.now().second
        val emailWithoutSuffix = email.replace(Regex("[^a-zA-Z0-9_]"), "")
        val hashStr = Base64
            .getEncoder()
            .encodeToString("${timestamp}_${firstName}_${emailWithoutSuffix}".toByteArray())

        return hashStr.replace(Regex("[^a-zA-Z0-9_]"), "").substring(0, 16);
    }
}

fun mockUser(role: Role) = User(
    id = 1_000_000_000,
    firstName = "John",
    lastName = "Doe",
    email = "test@bookie.ac.uk",
    password = "",
    dateOfBirth = Date(651196800000),
    role = role,
    netHash = "testUser",
    createdAt = 1713573878,
    modifiedAt = 1713573878,
)