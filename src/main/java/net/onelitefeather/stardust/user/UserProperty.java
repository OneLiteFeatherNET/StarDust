package net.onelitefeather.stardust.user;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table
public class UserProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Long id;

    @Column
    private final String name;

    @Column
    private String value;

    @Column
    private final Byte type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public UserProperty(Long id, String name, String value, Byte type, User user) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.type = type;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Byte getType() {
        return type;
    }

    public User getUser() {
        return user;
    }

    public UserProperty withValue(String value) {
        this.value = value;
        return this;
    }

    public UserProperty withUser(User user) {
        this.user = user;
        return this;
    }

    public UserPropertyType propertyType() {
        return UserPropertyType.getByName(this.name.toUpperCase());
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue() {
        return switch (type) {
            case 1 -> (T) Boolean.valueOf(value);
            case 2 -> (T) Integer.valueOf(value);
            case 3 -> (T) Long.valueOf(value);
            case 4 -> (T) Double.valueOf(value);
            case 5 -> (T) String.valueOf(value);
            default -> (T) value;
        };
    }

    public static UserProperty of(UserPropertyType type) {
        return new UserProperty(
                null,
                type.getName(),
                type.getDefaultValue().toString(),
                type.getType(),
                null
        );
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProperty that)) return false;

        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(value, that.value) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(value);
        result = 31 * result + Objects.hashCode(type);
        return result;
    }

    @Override
    public String toString() {
        return "UserProperty{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", type=" + type +
                '}';
    }
}
