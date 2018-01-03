package me.decyphr.FlyRaffler;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class FlyRaffler extends JavaPlugin {
    FileConfiguration config = getConfig();
    @Override
    public void onEnable() {
        config.addDefault("prefix", "§8[§aFlyRaffler§8] §f");
        config.options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (label.equalsIgnoreCase("raffle")) {
                Player winningPlayer = winner();
                while (winningPlayer == player) {
                    winningPlayer = winner();
                }
                for (Player p1 : Bukkit.getServer().getOnlinePlayers()) {
                    if (player.getItemInHand().getItemMeta().hasDisplayName()) {
                        p1.sendMessage(config.getString("prefix") + "§a" + player.getName() + " §7is raffling off §e" + player.getItemInHand().getItemMeta().getDisplayName() + "§7.");
                    }
                    else {
                        p1.sendMessage(config.getString("prefix") + "§a" + player.getName() + " §7is raffling off §e" + player.getItemInHand().getType().toString() + "§7.");
                    }
                    p1.sendMessage(config.getString("prefix") + "§7The winner is §b" + winningPlayer.getName() + "§7!");
                }
                if (winningPlayer.getInventory().firstEmpty() == -1) {
                    winningPlayer.getWorld().dropItem(winningPlayer.getLocation(), player.getItemInHand());
                }
                else {
                    winningPlayer.getInventory().addItem(player.getItemInHand());
                }
                player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
                winningPlayer.sendMessage(config.getString("prefix") + "§7You have won the raffle started by §a" + player.getName() + "§7!");
                winningPlayer.sendMessage("§7Check your inventory or the ground for the prize!");
            }
        }

        return false;
    }

    private Player winner() {
        Random rnd = new Random();
        int i = rnd.nextInt(Bukkit.getServer().getOnlinePlayers().size());
        return (Player) Bukkit.getServer().getOnlinePlayers().toArray()[i];
    }
}
