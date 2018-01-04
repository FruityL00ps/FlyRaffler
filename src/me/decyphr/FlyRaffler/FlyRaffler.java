package me.decyphr.FlyRaffler;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class FlyRaffler extends JavaPlugin {
    Logger log = Bukkit.getLogger();
    FileConfiguration config = getConfig();
    List<Player> participatingPlayers = new ArrayList<Player>();
    
    public FlyRaffler (mainclass instance) {
        plugin = instance;
    }
    
    ArrayList<int> timeList = new ArrayList<int>();
    
    @Override
    public void onEnable() {
        config.addDefault("prefix", "§8[§aFlyRaffler§8] §f");
        
        config.addDefault("time", 30);
        
        ArrayList<int> list = new ArrayList<int>();
        list.add(20);
        list.add(10);
        list.add(3);
        list.add(2);
        list.add(1);
        config.addDefault("time.countdown", list);
        
        timeList = config.getIntegerList("time.countdown");
        
        saveConfig();
        config.options().copyDefaults(true);
        saveConfig();
        log.info("FlyRaffler config loaded.");
        for (Player p1 : Bukkit.getServer().getOnlinePlayers()) {
            addPlayerParticipation(p1);
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        addPlayerParticipation(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removePlayerParticipation(event.getPlayer());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (label.equalsIgnoreCase("raffle")) {
                int amount = 1;
                if (args.length() >= 1) {
                    if (isNumeric(args[0])) {
                        amount = Integer.parseInt(args[0]);
                        ItemStack originalItemStack = player.getItemInHand();
                        ItemStack raffleItemStack = player.getItemInHand();
                        raffleItemStack.setAmount(amount);
                        String itemName = raffleItemStack.getItemMeta().hasDisplayName() ? raffleItemStack.getItemMeta().getDisplayname() : raffleItemStack.getType().replace("_", " ").toLowerCase();
                        
                        for (Player p1 : Bukkit.getServer().getOnlinePlayers()) {
                            p1.sendMessage(config.getString("prefix") + "§a" + player.getName() + " §7is raffling off §6" + amount.toString() + "x §e" + itemName + "§7.");
                            p1.sendMessage(config.getString("prefix") + "§7The winner will be picked in §b" + config.getInteger("time") + "§7!");
                            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - amount);
                            RaffleThread thread = new RaffleThread(config.getInteger("time"), p1, raffleItemStack, originalItemStack, player, config, winner());
                            thread.start();
                        }
                    }
                    else {
                        if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
                            if (player.hasPermission("flyraffler.admin.reload")) {
                                reloadConfig();
                            }
                            else {
                                player.sendMessage("§c§lAlert! §cYou do not have permission to reload FlyRaffler.");
                            }
                        }
                        else if (args[0].equalsIgnoreCase("participate") || args[0].equalsIgnoreCase("p")) {
                            if (player.hasPermission("flyraffler.participate.toggle")) {
                                if (args.length() >= 2) {
                                    if (args[1].equalsIgnoreCase("on")) {
                                        addPlayerParticipation(player);
                                    }
                                    else if (args[1].equalsIgnoreCase("off")) {
                                        removePlayerParticipation(player);
                                    }
                                    else {
                                        player.sendMessage("§cSyntax error! Usage: §4/raffle participate [on/off]");
                                    }
                                }
                                else {
                                    togglePlayerParticipation(player);
                                }
                            }
                            else {
                                player.sendMessage("§c§lAlert! §cYou do not have permission to toggle your participation in raffles.");
                            }
                        }
                        else {
                            player.sendMessage("§cInvalid arguments! Usages:");
                            player.sendMessage("§6/raffle [number] §f: §7Start a raffle for the item in your hand.");
                            player.sendMessage("§6/raffle participate [on/off] §f: §7Toggle your participation in raffles.");
                            player.sendMessage("§6/raffle reload §f: §7Reload the FlyRaffler plugin.");
                            
                        }
                    }
                }
                else {
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
        }

        return false;
    }

    private Player winner() {
        Random rnd = new Random();
        int i = rnd.nextInt(Bukkit.getServer().getOnlinePlayers().size());
        return (Player) Bukkit.getServer().getOnlinePlayers().toArray()[i];
    }
    
    public boolean isNumeric(String s) {  
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");  
    }
    
    public void togglePlayerParticipation(Player player) {
        if (participatingPlayers.contains(player)) {
            participatingPlayers.remove(new Player(player));
            player.sendMessage(config.getString("prefix") + "§7You have §cdisabled §7raffle participation!");
            log.info("[DEBUG] Player " + player.getName() + "has been removed from the participating players list.");
        }
        else {
            participatingPlayers.add(player);
            player.sendMessage(config.getString("prefix") + "§7You have §aenabled §7raffle participation!");
            log.info("[DEBUG] Player " + player.getName() + "has been added to the participating players list.");
        }
    }
    
    public void addPlayerParticipation(Player player) {
        if (participatingPlayers.contains(player) == false) {
            participatingPlayers.add(player);
            player.sendMessage(config.getString("prefix") + "§7You have §aenabled §7raffle participation!");
        }
        else {
            player.sendMessage(config.getString("prefix") + "§7You are already on the raffle list!");
        }
    }
    
    public void removePlayerParticipation(Player player) {
        if (participatingPlayers.contains(player)) {
            participatingPlayers.remove(new Player(player));
            player.sendMessage(config.getString("prefix") + "§7You have §cdisabled §7raffle participation!");
        }
        else {
            player.sendMessage(config.getString("prefix") + "§7You are already not on the raffle list!");
        }
    }
    
    public class RaffleThread implements Runnable {
        public int timee;
        public Player player;
        public ItemStack winnersItemStack;
        public ItemStack rafflersItemStack;
        public Player raffler;
        public FileConfiguration config;
        public Player winningPlayer;

        public RaffleThread (int timee, Player player, ItemStack winnersItemStack, ItemStack rafflersItemStack, Player raffler, FileConfiguration config, Player winningPlayer) {
            this.timee = timee;
            this.player = player;
            this.winnersItemStack = winnersItemStack;
            this.rafflersItemStack = rafflersItemStack;
            this.raffler = raffler;
            this.config = config;
            this.winningPlayer = winningPlayer;
        }

        public void run() {
            currInterval = timee;
            timeOn = -1;
            currentCountdown = timee;
            for (int i = 0; i <= currInterval; i++) {
                if (currentCountdown == 0) {
                    done();
                }
                else {
                    if (i == currInterval) {
                        timeOn++;
                        currInterval = timeList[timeOn];
                    }
                    Thread.sleep(1000);
                    currentCountdown--;
                }
            }
        }
        
        public void done() {
            if (winningPlayer.getInventory().firstEmpty() == -1) {
                winningPlayer.getWorld().dropItem(winningPlayer.getLocation(), raffleItemStack);
            }
            else {
                winningPlayer.getInventory().addItem(raffleItemStack);
            }
            winningPlayer.sendMessage(config.getString("prefix") + "§7You have won the raffle started by §a" + raffler.getName() + "§7!");
            winningPlayer.sendMessage("§7Check your inventory or the ground for the prize!");
        }
    }
}
