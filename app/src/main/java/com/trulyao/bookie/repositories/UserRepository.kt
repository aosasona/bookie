package com.trulyao.bookie.repositories

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import com.trulyao.bookie.daos.UserDao
import com.trulyao.bookie.entities.Role
import com.trulyao.bookie.entities.User
import com.trulyao.bookie.lib.AppException
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.Base64
import java.util.Date
import java.util.UUID

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

class UserRepository(dao: UserDao) {
    private val dao: UserDao;

    init {
        this.dao = dao
    }

    fun signIn(email: String, password: String): Int {
        if (!email.trim().matches(emailRegex)) {
            throw AppException("Invalid email address provided, must only contain alphanumeric characters, ., _ and - and end with @bookie.ac.uk")
        }
        if (password.trim().isEmpty()) throw AppException("Password is required")

        val user = dao.findByEmail(email.trim()) ?: throw AppException("Account not found")

        if (comparePassword(rawPasswordString = password, hash = user.password).not()) {
            throw AppException("Invalid credentials provided")
        }

        return user.id!!
    }

    fun signUp(data: CreateUserData, role: Role = Role.Student): Int {
        data.firstName = data.firstName.trim().lowercase()
        data.lastName = data.lastName.trim().lowercase()
        data.email = data.email.trim().lowercase()
        data.password = data.password.trim()

        if (!data.firstName.matches(nameRegex)) throw AppException("First name must be at least 3 characters and only contain alphabets")
        if (!data.lastName.matches(nameRegex)) throw AppException("Last name must be at least 3 characters and only contain alphabets")
        if (!data.email.matches(emailRegex)) throw AppException("Invalid email address provided, must only contain alphanumeric characters, ., _ and - and end with @bookie.ac.uk")
        if (data.password.isEmpty()) throw AppException("Password is required")
        if (data.password !== data.confirmPassword) throw AppException("Passwords are not the same!")

        val existingUser = dao.findByEmail(data.email)
        if (existingUser != null && existingUser.id!! > 0) throw AppException("An account with this username already exists")

        val insertedUserID = dao.createUser(
            User(
                firstName = data.firstName,
                lastName = data.lastName,
                email = data.email,
                password = data.password,
                netHash = generateNetworkHash(data.firstName, data.email),
                role = role,
                dateOfBirth = data.dateOfBirth
            )
        )

        return insertedUserID.toInt()
    }

    fun createDefaultAdmin() {
        dao.createUser(
            User(
                firstName = "Julian",
                lastName = "Blake",
                email = "jb@bookie.ac.uk",
                password = this.hashPassword("admin123"),
                role = Role.Admin,
                netHash = generateNetworkHash("Julian", "jb@bookie.ac.uk"),
                dateOfBirth = Date.from(
                    LocalDate.of(1990, 8, 21).atStartOfDay().toInstant(ZoneOffset.UTC)
                )
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

    private fun comparePassword(rawPasswordString: String, hash: String): Boolean {
        val argon2 = Argon2Kt()
        return argon2.verify(
            mode = Argon2Mode.ARGON2_I,
            encoded = hash,
            password = rawPasswordString.toByteArray()
        )
    }

    // Generate a unique network name from a combination of a couple of unique identifiers
    private fun generateNetworkHash(firstName: String, email: String): String {
        val timestamp = LocalTime.now().second
        val emailWithoutSuffix = email.replace(Regex("[^a-zA-Z0-9_]"), "")
        val hashStr = Base64
            .getEncoder()
            .encodeToString("${timestamp}_${firstName}_${emailWithoutSuffix}".toByteArray())

        val uuid = UUID.fromString(hashStr).toString()

        return uuid.replace("-", "").substring(0, 16);
    }
}