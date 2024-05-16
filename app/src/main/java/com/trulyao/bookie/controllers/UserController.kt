package com.trulyao.bookie.controllers

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import com.trulyao.bookie.daos.UserDao
import com.trulyao.bookie.entities.Role
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.lib.AppException
import com.trulyao.bookie.lib.emailRegex
import com.trulyao.bookie.lib.nameRegex
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period
import java.time.ZoneId
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
)

data class EditableUserData(
    var firstName: String,
    var lastName: String,
    var email: String,
    var dateOfBirth: Date?,
)


class UserController private constructor(
    private val dao: UserDao,
    private val dispatcher: CoroutineDispatcher,
) {
    companion object {
        @Volatile
        private var instance: UserController? = null
        fun getInstance(
            dao: UserDao,
            dispatcher: CoroutineDispatcher = Dispatchers.IO,
        ): UserController {
            return if (this.instance != null) {
                this.instance!!
            } else {
                synchronized(this) {
                    instance ?: UserController(dao, dispatcher).also { repo -> instance = repo }
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
            dao.findByEmail(email.trim()) ?: throw AppException("Account not found")
        }

        if (comparePassword(password = password, hashedPassword = user.password).not()) {
            throw AppException("Invalid credentials provided")
        }

        return user
    }

    private fun validateEditableUserData(data: EditableUserData): EditableUserData {
        data.firstName = data.firstName.trim().lowercase()
        data.lastName = data.lastName.trim().lowercase()
        data.email = data.email.trim().lowercase()

        val dobAsLocalDate = data.dateOfBirth
            ?.toInstant()
            ?.atZone(ZoneId.systemDefault())
            ?.toLocalDate()

        if (!data.firstName.matches(nameRegex)) throw AppException("First name must be at least 3 characters and only contain alphabets")
        if (!data.lastName.matches(nameRegex)) throw AppException("Last name must be at least 3 characters and only contain alphabets")
        if (!data.email.matches(emailRegex)) throw AppException("Invalid email address provided, must only contain alphanumeric characters, ., _ and - and end with @bookie.ac.uk")

        // Date must not be in the future and must be at least 12 years old
        if (dobAsLocalDate?.isAfter(LocalDate.now()) == true) throw AppException("Date of birth cannot be in the future")
        val age = Period.between(dobAsLocalDate, LocalDate.now()).years < 12
        if (age) throw AppException("Date of birth must be equivalent to at least 12 years old")

        return data
    }

    suspend fun signUp(data: CreateUserData, role: Role = Role.Student): Int {
        val userData = this.validateEditableUserData(
            EditableUserData(
                firstName = data.firstName,
                lastName = data.lastName,
                email = data.email,
                dateOfBirth = data.dateOfBirth
            )
        )

        data.password = data.password.trim()
        if (data.password.isEmpty()) throw AppException("Password is required")

        val existingUser = withContext(dispatcher) {
            dao.findByEmail(userData.email)
        }

        if (existingUser != null && existingUser.id!! > 0) throw AppException("An account with this email already exists")

        val insertedUserID = withContext(dispatcher) {
            dao.createUser(
                User(
                    firstName = userData.firstName,
                    lastName = userData.lastName,
                    email = userData.email,
                    password = hashPassword(data.password),
                    netHash = generateNetworkHash(userData.firstName, userData.email),
                    role = role,
                    dateOfBirth = userData.dateOfBirth ?: Date(),
                    createdAt = System.currentTimeMillis(),
                    modifiedAt = System.currentTimeMillis(),
                )
            )
        }

        return insertedUserID.toInt()
    }

    suspend fun updateProfile(userId: Int, data: EditableUserData) {
        val existingUser = withContext(dispatcher) {
            dao.findByID(userId)
        } ?: throw AppException("Could not find an account with that user ID")

        val userData = this.validateEditableUserData(data)

        // Update editable fields
        existingUser.firstName = userData.firstName
        existingUser.lastName = userData.lastName
        existingUser.email = userData.email
        if (userData.dateOfBirth != null) existingUser.dateOfBirth = userData.dateOfBirth!!
        existingUser.modifiedAt = System.currentTimeMillis()

        withContext(dispatcher) { dao.updateUser(existingUser) }
    }

    suspend fun changePassword(
        userId: Int,
        oldPassword: String,
        newPassword: String,
        confirmPassword: String,
    ) {
        if (oldPassword.isEmpty() && newPassword.isEmpty() && confirmPassword.isEmpty()) {
            throw AppException("All fields are required!")
        }

        if (newPassword != confirmPassword) throw AppException("New password is not the same as the password confirmation")
        val user = withContext(dispatcher) {
            // We could just pass in the whole user context instead of this extra read but we are using a local in-process database anyway
            // and it is safer to get the data again to be sure we have got the right user and the ID, the extra call has nearly zero boundary-crossing latency, it's fine
            dao.findByID(userId)
        }
            ?: throw AppException("Unexpected user ID provided, this user does not exist, please contact an administrator or try again")

        val isCorrectPassword = comparePassword(oldPassword, user.password)
        if (isCorrectPassword.not()) throw AppException("Incorrect current password provided, please try again")

        withContext(dispatcher) {
            user.id?.let {
                dao.updatePassword(it, hashPassword(newPassword))
            }
        }
    }

    suspend fun adminUpdatePassword(userId: Int, newPassword: String) {
        if (newPassword.isEmpty()) throw AppException("New password is required")

        withContext(dispatcher) {
            dao.updatePassword(userId, hashPassword(newPassword))
        }
    }

    suspend fun deleteUser(userId: Int) {
        withContext(dispatcher) {
            dao.deleteByID(userId)
        }
    }

    fun createDefaultAdmin() {
        dao.createUser(
            User(
                firstName = "julian",
                lastName = "blake",
                email = "jb@bookie.ac.uk",
                password = this.hashPassword("Admin123"),
                role = Role.SuperAdmin,
                netHash = generateNetworkHash("Julian", "jb@bookie.ac.uk"), // TO BE USED FOR FUTURE P2P NETWORKING
                dateOfBirth = Date.from(
                    LocalDate.of(1990, 8, 21)
                        .atStartOfDay()
                        .toInstant(ZoneOffset.UTC)
                ),
                createdAt = System.currentTimeMillis(),
                modifiedAt = System.currentTimeMillis(),
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

        return hashStr.replace(Regex("[^a-zA-Z0-9_]"), "").substring(0, 16)
    }
}

fun mockUser(role: Role, userId: Int = 1_000) = User(
    id = userId,
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