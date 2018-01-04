package me.decyphr.FlyRaffler;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FlyRaffler extends JavaPlugin {
    FileConfiguration config = getConfig();
    List<Player> participatingPlayers = new ArrayList<Player>();

    @Override
    public void onEnable() {
        config.addDefault("prefix", "§7[§6RAFFLE§7] §f");
        config.options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (label.equalsIgnoreCase("raffle") || label.equalsIgnoreCase("raf")) {
                if (args.length >= 1) {
                    if (isNumeric(args[0])) {
                        participatingPlayers.remove(player);
                        player.sendMessage(config.getString("prefix") + "§7You have §cdisabled §7your raffle participation.");
                        if (participatingPlayers.isEmpty() == false) {
                            Player winningPlayer = winner();
                            int amount = 1;
                            int startamount = player.getItemInHand().getAmount();
                            if (player.getItemInHand().getAmount() >= Integer.parseInt(args[0])) {
                                amount = Integer.parseInt(args[0]);

                                String name =  player.getItemInHand().getItemMeta().hasDisplayName() ?  player.getItemInHand().getItemMeta().getDisplayName() :  player.getItemInHand().getType().toString().replace("_", " ").toLowerCase();
                                for (Player p1 : Bukkit.getServer().getOnlinePlayers()) {
                                    p1.sendMessage(config.getString("prefix") + "§a" + player.getName() + " §7has raffled off §6" + amount + "x §e" + name + "§7.");
                                    p1.sendMessage(config.getString("prefix") + "§7The winner is §b" + winningPlayer.getName() + "§7!");
                                }
                                ItemStack item = player.getItemInHand();
                                item.setAmount(amount);
                                if (winningPlayer.getInventory().firstEmpty() == -1) {
                                    winningPlayer.getWorld().dropItem(winningPlayer.getLocation(), item);
                                }
                                else {
                                    winningPlayer.getInventory().addItem(item);
                                }
                                player.getItemInHand().setAmount(startamount - amount);
                                winningPlayer.sendMessage(config.getString("prefix") + "§7Congrats §b" + winningPlayer.getName() + "§7, you won §6" + amount + "x §e" + name + "§7from the raffle!");
                                winningPlayer.sendMessage("§c§l(!) §7Please check your inventory or the ground for your prize!");
                            }
                        }
                        else {
                            player.sendMessage("§c(!)  Not enough players online to do a raffle!");
                        }
                        participatingPlayers.add(player);
                        player.sendMessage(config.getString("prefix") + "§7You have §aenabled §7your raffle participation.");
                    }
                    else {
                        if (args[0].equalsIgnoreCase("participate") || args[0].equalsIgnoreCase("p")) {
                            if (player.hasPermission("flyraffler.toggleparticipation")) {
                                if (args.length >= 2) {
                                    if (args[1].equalsIgnoreCase("on")) {
                                        if (participatingPlayers.contains(player) == false) {
                                            participatingPlayers.remove(player);
                                            player.sendMessage(config.getString("prefix") + "§7You have §aenabled §7your raffle participation.");
                                        }
                                        else {
                                            participatingPlayers.remove(player);
                                            player.sendMessage("§c(!) §7You are already participating in raffles.");
                                        }
                                    }
                                    else if (args[1].equalsIgnoreCase("off")) {
                                        if (participatingPlayers.contains(player)) {
                                            participatingPlayers.remove(player);
                                            player.sendMessage(config.getString("prefix") + "§c§l(!) §7You have §cdisabled §7your raffle participation.");
                                        }
                                        else {
                                            participatingPlayers.remove(player);
                                            player.sendMessage("§c(!) §7You are already not participating in raffles.");
                                        }
                                    }
                                    else {
                                        ToggleParticipation(player);
                                    }
                                }
                                else {
                                    ToggleParticipation(player);
                                }
                            }
                            else {
                                player.sendMessage("§cError! Invalid permissions.");
                            }
                        }
                        else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
                            if (player.hasPermission("flyraffler.admin.reload")) {
                                reloadConfig();
                                player.sendMessage("§aFlyRaffler 2.0 reloaded!");
                            }
                            else {
                                player.sendMessage("§cError! Invalid permissions.");
                            }
                        }
                        else {
                            player.sendMessage("§cError! Invalid sub-command!");
                        }
                    }
                }
                else {
                    //TODO: single raffle item code here :DONE
                    participatingPlayers.remove(player);
                    player.sendMessage(config.getString("prefix") + "§7You have §cdisabled §7your raffle participation.");
                    if (participatingPlayers.isEmpty() == false) {
                        Player winningPlayer = winner();
                        int amount = 1;
                        int startamount = player.getItemInHand().getAmount();
                        String name =  player.getItemInHand().getItemMeta().hasDisplayName() ?  player.getItemInHand().getItemMeta().getDisplayName() :  player.getItemInHand().getType().toString().replace("_", " ").toLowerCase();
                        for (Player p1 : Bukkit.getServer().getOnlinePlayers()) {
                            p1.sendMessage(config.getString("prefix") + "§a" + player.getName() + " §7has raffled off §6" + amount + "x §e" + name + "§7.");
                            p1.sendMessage(config.getString("prefix") + "§7The winner is §b" + winningPlayer.getName() + "§7!");
                        }
                        ItemStack item = player.getItemInHand();
                        item.setAmount(amount);
                        if (winningPlayer.getInventory().firstEmpty() == -1) {
                            winningPlayer.getWorld().dropItem(winningPlayer.getLocation(), item);
                        }
                        else {
                            winningPlayer.getInventory().addItem(item);
                        }
                        player.getItemInHand().setAmount(startamount - amount);
                        winningPlayer.sendMessage(config.getString("prefix") + "§7Congrats §b" + winningPlayer.getName() + "§7, you won §6" + amount + "x §e" + name + "§7from the raffle!");
                        winningPlayer.sendMessage("§c§l(!) §7Please check your inventory or the ground for your prize!");
                    }
                    else {
                        player.sendMessage("§c(!) Not enough players online to do a raffle!");
                    }
                    participatingPlayers.add(player);
                    player.sendMessage(config.getString("prefix") + "§7You have §aenabled §7your raffle participation.");
                }
            }
        }
        return false;
    }

    private Player winner() {
        Random rnd = new Random();
        int i = rnd.nextInt(participatingPlayers.size());
        return (Player) participatingPlayers.toArray()[i];
    }

    private boolean isNumeric(String s) {
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }

    public void ToggleParticipation(Player player) {
        if (participatingPlayers.contains(player)) {
            participatingPlayers.remove(player);
            player.sendMessage(config.getString("prefix") + "§7You have §cdisabled §7your raffle participation.");
        }
        else {
            participatingPlayers.add(player);
            player.sendMessage(config.getString("prefix") + "§7You have §aenabled §7your raffle participation.");
        }
    }
}
