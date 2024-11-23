package net.onelitefeather.stardust.user

import jakarta.persistence.*

@Entity
@Table
data class UserProperty(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

    @Column
    val name: String = "",

    @Column
    val value: String = "",

    @Column
    val type: Byte = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User? = null) {

    constructor() : this(null)

    fun <T : Any> getValue(): T? {
        val result = when (this.type.toInt()) {
            0 -> this.value
            1 -> this.value.toIntOrNull()
            2 -> this.value.toBooleanStrictOrNull()
            3 -> this.value.toDoubleOrNull()
            4 -> this.value.toFloatOrNull()
            5 -> this.value.toShortOrNull()
            6 -> this.value.toByteOrNull()
            else -> null
        }
        return result as T?
    }

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id , key = $name , value = $value )"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserProperty) return false

        if (id != other.id) return false
        if (name != other.name) return false
        if (value != other.value) return false
        if (type != other.type) return false
        if (user != other.user) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + type
        result = 31 * result + (user?.hashCode() ?: 0)
        return result
    }
}