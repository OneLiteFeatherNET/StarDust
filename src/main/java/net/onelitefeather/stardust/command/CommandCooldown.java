package net.onelitefeather.stardust.command;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table
public class CommandCooldown {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String commandSender = "";

    @Column
    private String command = "";

    @Column
    private long executedAt = -1;

    public CommandCooldown() {}

    public CommandCooldown(Long id, UUID commandSender, String command, long executedAt) {
        this.id = id;
        this.commandSender = commandSender.toString();
        this.command = command;
        this.executedAt = executedAt;
    }

    public Long getId() {
        return id;
    }

    public String getCommandSender() {
        return commandSender;
    }

    public String getCommand() {
        return command;
    }

    public long getExecutedAt() {
        return executedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCommandSender(String commandSender) {
        this.commandSender = commandSender;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setExecutedAt(long executedAt) {
        this.executedAt = executedAt;
    }

    public boolean isOver() {
        return System.currentTimeMillis() >= executedAt;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "(id = " + id +
                " , commandSender = " + commandSender +
                " , command = " + command +
                " , executedAt = " + executedAt + " )";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommandCooldown)) return false;

        CommandCooldown that = (CommandCooldown) o;

        if (executedAt != that.executedAt) return false;
        if (!Objects.equals(id, that.id)) return false;
        if (!commandSender.equals(that.commandSender)) return false;
        return command.equals(that.command);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + commandSender.hashCode();
        result = 31 * result + command.hashCode();
        result = 31 * result + Long.hashCode(executedAt);
        return result;
    }
}
