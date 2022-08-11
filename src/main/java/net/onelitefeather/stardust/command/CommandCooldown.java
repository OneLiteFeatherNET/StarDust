package net.onelitefeather.stardust.command;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table
public class CommandCooldown {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String uniqueId;

    @Column
    private String command;

    @Column
    private long executedAt;

    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId.toString();
    }

    public UUID getUuid() {
        return UUID.fromString(this.uniqueId);
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void setExecutedAt(long executedAt) {
        this.executedAt = executedAt;
    }

    public long getExecutedAt() {
        return executedAt;
    }

    public boolean canExecute() {
        return System.currentTimeMillis() > this.executedAt;
    }

    @Override
    public String toString() {
        return "CommandCooldown{" +
                "uniqueId=" + uniqueId +
                ", command='" + command + '\'' +
                ", executedAt=" + executedAt +
                '}';
    }
}

