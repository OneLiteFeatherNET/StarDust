package net.onelitefeather.stardust.api

import net.onelitefeather.stardust.api.user.User
import java.util.UUID
import java.util.function.Consumer

interface UserService<P> {

    fun getUsers(): List<User>

    fun getUser(uuid: UUID): User?

    fun getUser(name: String): User?

    fun registerUser(player: P, consumer: Consumer<User>)

    fun isUserCreated(uuid: UUID): Boolean

    fun updateUser(user: User)

    fun deleteUser(uuid: UUID, consumer: Consumer<Boolean>)

    fun startUserTask()

    fun stopUserTask()
}