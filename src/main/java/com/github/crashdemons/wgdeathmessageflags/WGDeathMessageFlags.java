/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.crashdemons.wgdeathmessageflags;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author crash
 */
public class WGDeathMessageFlags extends JavaPlugin implements Listener {
    WorldGuardPlugin wgp = null;
    WorldGuard wg = null;
    
    
    
    public static final StateFlag FLAG_DEATH_MESSAGE = new StateFlag("death-messages", true);
    
    
    
    public WorldGuard getWorldGuard(){ return wg; }
    public WorldGuardPlugin getWorldGuardPlugin(){ return wgp; }
    
    private WorldGuardPlugin findWorldGuardPlugin() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null; // Maybe you want throw an exception instead
        }

        return (WorldGuardPlugin) plugin;
    }
    
    
    private boolean wgInit(){
        wgp = findWorldGuardPlugin();
        wg = WorldGuard.getInstance();
        if(wgp==null || wg==null){
            return false; 
        }
        
        FlagRegistry registry = wg.getFlagRegistry();
        try {
            // register our flag with the registry
            registry.register(FLAG_DEATH_MESSAGE);
            return true;
        } catch (FlagConflictException e) {
            // some other plugin registered a flag by the same name already.
            // you may want to re-register with a different name, but this
            // could cause issues with saved flags in region files. it's better
            // to print a message to let the server admin know of the conflict
            getLogger().severe("Could not register WG flags due to a conflict with another plugin");
            return false;
        }
    }
    
    private boolean pluginInit(){
        return true;
    }
    
    @Override
    public void onLoad(){
        if(!wgInit()) return;
    }
    
    @Override
    public void onEnable(){
        getLogger().info("Enabling...");
        if(!pluginInit()) return;
        this.getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Enabled.");
    }
    
    @Override
    public void onDisable(){
        getLogger().info("Disabling...");
        getLogger().info("Disabled."); 
   }
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event){
        Player player = event.getEntity();
        Location loc = player.getLocation();
        
        LocalPlayer wgPlayer = getWorldGuardPlugin().wrapPlayer(player);
        
        com.sk89q.worldedit.util.Location wgLoc = BukkitAdapter.adapt(loc);
        RegionQuery query = getWorldGuard().getPlatform().getRegionContainer().createQuery();
        StateFlag.State state = query.queryState(wgLoc, wgPlayer, FLAG_DEATH_MESSAGE);
        if(state==StateFlag.State.DENY){
           event.setDeathMessage(null);
        }
    }
}
