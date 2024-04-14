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

class UserRepository(dao: UserDao) {
    private val dao: UserDao;

    init {
        this.dao = dao
    }

    fun signIn(email: String, password: String): Int {
        val user = dao.findByEmail(email) ?: throw AppException("Account not found")

        if (comparePassword(rawPasswordString = password, hash = user.password).not()) {
            throw AppException("Invalid credentials provided")
        }

        return user.id!!
    }

    fun signUp(user: User): Int {
        val existingUser = dao.findByEmail(user.email)
        if (existingUser != null) {
            throw AppException("An account with this username already exists")
        }

        return 1;
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