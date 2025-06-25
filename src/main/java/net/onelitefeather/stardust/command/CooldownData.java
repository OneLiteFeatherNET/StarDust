package net.onelitefeather.stardust.command;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CooldownData {
    private final String commandName;
    private final TimeUnit timeUnit;
    private final long time;

    public CooldownData(String commandName, TimeUnit timeUnit, long time) {
        this.commandName = commandName;
        this.timeUnit = timeUnit;
        this.time = time;
    }

    public String getCommandName() {
        return commandName;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public long getTime() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CooldownData that)) return false;
        return time == that.time &&
                commandName.equals(that.commandName) &&
                timeUnit == that.timeUnit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(commandName, timeUnit, time);
    }
}