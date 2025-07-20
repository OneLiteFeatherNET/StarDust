package net.onelitefeather.stardust.command.commands;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.kyori.adventure.text.Component;
import net.onelitefeather.stardust.StardustPlugin;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public final class VPNCheckCommand {

    private final StardustPlugin plugin;
    private final String baseUrl = "https://v2.api.iphub.info/ip/%s";
 
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson().newBuilder().create();

    public VPNCheckCommand(StardustPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("vpncheck <player>")
    @Permission("psittaciforms.command.vpncheck")
    public void vpnCheck(Player player, @Argument("player") Player target) {
        if (target.getAddress() != null) {
            var address = target.getAddress();


            var request = HttpRequest.newBuilder().GET()
                    .uri(URI.create(baseUrl.formatted(address.getAddress().getHostAddress())))
                    .header("X-Key", this.plugin.getConfig().getString("iphub.key"))
                    .build();
            try {
                player.sendMessage(Component.text(address.getAddress().getHostAddress()));
                var response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                player.sendMessage(Component.text(response.body()));
                if (response.statusCode() == 200) {
                    var json = this.gson.fromJson(response.body(), JsonElement.class).getAsJsonObject();
                    var block = json.get("block").getAsInt();
                    var isp = json.get("isp").getAsString();
                    var ip = json.get("ip").getAsString();
                    var asn = json.get("asn").getAsInt();
                    var countryName = json.get("countryName").getAsString();
                    var countryCode = json.get("countryCode").getAsString();

                    player.sendMessage(Component.translatable("commands.vpncheck.block.%s".formatted(block))
                            .arguments(
                                    plugin.getPrefix(),
                                    Component.text(ip),
                                    Component.text(isp),
                                    Component.text(asn),
                                    Component.text(countryName),
                                    Component.text(countryCode),
                                    Component.text(target.getName())));

                }
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
                this.plugin.getLogger().throwing(VPNCheckCommand.class.getSimpleName(),"vpnCheck", e);
            }
        }
    }
}
