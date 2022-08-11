package net.onelitefeather.stardust.position;

import net.onelitefeather.stardust.FeatherEssentials;
import net.onelitefeather.stardust.util.Constants;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class SpawnManager {

    private final File file;
    private final List<SpawnPoint> spawnPoints;

    public SpawnManager(FeatherEssentials featherEssentials) {

        this.file = new File(featherEssentials.getDataFolder(), "spawns.json");

        if (!this.file.exists()) {
            try {
                Files.createFile(this.file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.spawnPoints = getStoredSpawnPoints();
    }

    @Nullable
    public SpawnPoint getSpawnPoint(World world) {
        return this.getSpawnPoints().stream().filter(spawnPoint -> spawnPoint.getWrappedLocation().getWorldName().equalsIgnoreCase(world.getName())).findFirst().orElse(null);
    }

    @Nullable
    public SpawnPoint getSpawnPoint(String name) {
        return this.spawnPoints.stream().filter(spawnPoint -> spawnPoint.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<SpawnPoint> getSpawnPoints() {
        return this.spawnPoints;
    }

    public boolean setSpawnPoint(String name, WrappedLocation wrappedLocation, @Nullable String permission, boolean defaultSpawn) {

        SpawnPoint spawnPoint = new SpawnPoint(name, wrappedLocation, permission, defaultSpawn);

        if (!exists(name)) {
            this.spawnPoints.add(spawnPoint);
        } else {
            spawnPoint = getSpawnPoint(name);
            if (spawnPoint != null) {
                spawnPoint.setName(name);
                spawnPoint.setPermission(permission);
                spawnPoint.setWrappedLocation(wrappedLocation);
                spawnPoint.setDefaultSpawn(defaultSpawn);
            }
        }

        try (FileWriter fileWriter = new FileWriter(this.file)) {
            fileWriter.write(Constants.GSON.toJson(this.spawnPoints));
            fileWriter.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean removeSpawnPoint(String name) {
        if (exists(name)) {

            SpawnPoint spawnPoint = getSpawnPoint(name);
            if (spawnPoint != null) {
                this.spawnPoints.remove(spawnPoint);
                try (FileWriter fileWriter = new FileWriter(this.file)) {
                    fileWriter.write(Constants.GSON.toJson(this.spawnPoints));
                    fileWriter.flush();
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    public boolean exists(@NotNull String name) {
        return getStoredSpawnPoints().stream().anyMatch(spawnPoint -> spawnPoint.getName().equalsIgnoreCase(name));
    }

    public List<SpawnPoint> getStoredSpawnPoints() {

        List<SpawnPoint> points = new ArrayList<>();
        try (FileReader fileReader = new FileReader(this.file)) {

            SpawnPoint[] storedSpawnPoint = Constants.GSON.fromJson(fileReader, SpawnPoint[].class);
            if (storedSpawnPoint != null && storedSpawnPoint.length > 0) {
                points.addAll(List.of(storedSpawnPoint));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return points;
    }


}
