package net.onelitefeather.stardust.command

import jakarta.persistence.*

@Entity
@Table
data class CommandCooldown(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column
    val commandSender: String = "",

    @Column
    val command: String = "",

    @Column
    val executedAt: Long = -1
) {

    constructor() : this(null)

    fun isOver(): Boolean = System.currentTimeMillis() >= executedAt

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id , commandSender = $commandSender , command = $command , executedAt = $executedAt )"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CommandCooldown) return false

        if (id != other.id) return false
        if (commandSender != other.commandSender) return false
        if (command != other.command) return false
        if (executedAt != other.executedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + commandSender.hashCode()
        result = 31 * result + command.hashCode()
        result = 31 * result + executedAt.hashCode()
        return result
    }
}
