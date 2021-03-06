package com.LAbility.LuaUtility.Wrapper;

import com.LAbility.Ability;
import com.LAbility.LAPlayer;
import com.LAbility.LAbilityMain;
import com.LAbility.LuaUtility.PlayerList;
import com.LAbility.ScheduleManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class GameWrapper extends LuaTable {
    public Material targetItem = Material.IRON_INGOT;
    public boolean overrideItem = false;

    public GameWrapper(LAbilityMain plugin) {
        set("getPlayers", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                PlayerList<LAPlayer> survivePlayer = new PlayerList<LAPlayer>();
                for (LAPlayer lap : plugin.gameManager.players){
                    if (lap.isSurvive) survivePlayer.add(lap);
                }
                return CoerceJavaToLua.coerce(survivePlayer);
            }
        });

        set("getAllPlayers", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return CoerceJavaToLua.coerce(plugin.gameManager.players);
            }
        });

        set("getPlayer", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg1) {
                Player player = (Player) arg1.checkuserdata(Player.class);
                if (plugin.gameManager.players.indexOf(player) >= 0)
                    return CoerceJavaToLua.coerce(plugin.gameManager.players.get(plugin.gameManager.players.indexOf(player)));
                else return CoerceJavaToLua.coerce(false);
            }
        });

        set("checkCooldown", new VarArgFunction() {
            @Override
            public LuaValue invoke(Varargs vargs) {
                Player player = (Player) vargs.checkuserdata(1, Player.class);
                Ability ability = (Ability) vargs.checkuserdata(2, Ability.class);
                int funcID = vargs.checkint(3);

                for (LAPlayer players : LAbilityMain.instance.gameManager.players) {
                    if (players.getPlayer().equals(player)) {
                        for (Ability abilities : players.getAbility()) {
                            if (abilities.equals(ability)) {
                                return CoerceJavaToLua.coerce(abilities.CheckCooldown(player, funcID));
                            }
                        }
                    }
                }
                return CoerceJavaToLua.coerce(false);
            }
        });

        set("changeAbility", new VarArgFunction() {
            @Override
            public LuaValue invoke(Varargs vargs) {
                Player player = (Player) vargs.checkuserdata(1, Player.class);
                Ability ability = (Ability) vargs.checkuserdata(2, Ability.class);
                String abilityID = vargs.checkjstring(3);
                boolean deleteAll = vargs.checkboolean(4);

                Ability newAbility;
                int aindex = LAbilityMain.instance.abilities.indexOf(abilityID);
                if (aindex >= 0) newAbility = LAbilityMain.instance.abilities.get(aindex);
                else return CoerceJavaToLua.coerce(false);

                for (LAPlayer players : LAbilityMain.instance.gameManager.players) {
                    if (players.getPlayer().equals(player)) {
                        players.getPlayer().sendMessage("\2476[\247eLAbility\2476] \247e????????? ????????? ?????????????????????.");
                        players.getPlayer().sendMessage("\2476[\247eLAbility\2476] \247e/la check??? ????????? ??? ?????? ????????????.");

                        if (deleteAll) {
                            for (Ability a : players.getAbility()){
                                LAbilityMain.instance.gameManager.StopPassive(players, a);
                                LAbilityMain.instance.gameManager.StopActiveTimer(players, a);
                            }
                            players.getAbility().clear();
                        }
                        else {
                            int abilityIndex = players.getAbility().indexOf(ability.abilityID);
                            LAbilityMain.instance.gameManager.StopPassive(players, players.getAbility().get(abilityIndex));
                            LAbilityMain.instance.gameManager.StopActiveTimer(players, players.getAbility().get(abilityIndex));
                            players.getAbility().remove(abilityIndex);
                        }

                        players.getAbility().add(newAbility);
                        int newAbilityIndex = players.getAbility().indexOf(newAbility.abilityID);
                        LAbilityMain.instance.gameManager.RunPassive(players, players.getAbility().get(newAbilityIndex));
                    }
                }

                return CoerceJavaToLua.coerce(false);
            }
        });

        set("removeAbility", new VarArgFunction() {
            @Override
            public LuaValue invoke(Varargs vargs) {
                Player player = (Player) vargs.checkuserdata(1, Player.class);
                Ability ability = (Ability) vargs.checkuserdata(2, Ability.class);
                boolean deleteAll = vargs.checkboolean(3);

                for (LAPlayer players : LAbilityMain.instance.gameManager.players) {
                    if (players.getPlayer().equals(player)) {
                        players.getPlayer().sendMessage("\2474[\247cLAbility\2474] \247c????????? ????????? ???????????????.");
                        players.getPlayer().sendMessage("\2474[\247cLAbility\2474] \247c/la check??? ????????? ??? ?????? ????????????.");

                        if (deleteAll) {
                            for (Ability a : players.getAbility()){
                                LAbilityMain.instance.gameManager.StopPassive(players, a);
                                LAbilityMain.instance.gameManager.StopActiveTimer(players, a);
                            }
                            players.getAbility().clear();
                        }
                        else {
                            int abilityIndex = players.getAbility().indexOf(ability.abilityID);
                            LAbilityMain.instance.gameManager.StopPassive(players, players.getAbility().get(abilityIndex));
                            LAbilityMain.instance.gameManager.StopActiveTimer(players, players.getAbility().get(abilityIndex));
                            players.getAbility().remove(abilityIndex);
                        }
                    }
                }

                return CoerceJavaToLua.coerce(false);
            }
        });

        set("addAbility", new VarArgFunction() {
            @Override
            public LuaValue invoke(Varargs vargs) {
                Player player = (Player) vargs.checkuserdata(1, Player.class);
                String abilityID = vargs.checkjstring(2);

                Ability newAbility;
                int aindex = LAbilityMain.instance.abilities.indexOf(abilityID);
                if (aindex >= 0) newAbility = LAbilityMain.instance.abilities.get(aindex);
                else return CoerceJavaToLua.coerce(false);

                for (LAPlayer players : LAbilityMain.instance.gameManager.players) {
                    if (players.getPlayer().equals(player)) {
                        players.getPlayer().sendMessage("\2476[\247eLAbility\2476] \247e????????? ????????? ?????????????????????.");
                        players.getPlayer().sendMessage("\2476[\247eLAbility\2476] \247e/la check??? ????????? ??? ?????? ????????????.");

                        players.getAbility().add(newAbility);
                        int newAbilityIndex = players.getAbility().indexOf(newAbility.abilityID);
                        LAbilityMain.instance.gameManager.RunPassive(players, players.getAbility().get(newAbilityIndex));
                    }
                }

                return CoerceJavaToLua.coerce(false);
            }
        });

        set("hasAbility", new VarArgFunction() {
            @Override
            public LuaValue invoke(Varargs vargs) {
                Player player = (Player) vargs.checkuserdata(1, Player.class);
                Ability ability = (Ability) vargs.checkuserdata(2, Ability.class);

                for (LAPlayer players : LAbilityMain.instance.gameManager.players) {
                    if (players.getPlayer().equals(player)) {
                        return CoerceJavaToLua.coerce(players.hasAbility(ability));
                    }
                }
                return CoerceJavaToLua.coerce(false);
            }
        });

        set("getPlayerAbility", new VarArgFunction() {
            @Override
            public LuaValue invoke(Varargs vargs) {
                Player player = (Player) vargs.checkuserdata(1, Player.class);

                for (LAPlayer players : LAbilityMain.instance.gameManager.players) {
                    if (players.getPlayer().equals(player)) {
                        return CoerceJavaToLua.coerce(players.getAbility());
                    }
                }
                return CoerceJavaToLua.coerce(NIL);
            }
        });


        set("sendMessage", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue arg1, LuaValue arg2) {
                Player player = (Player) arg1.checkuserdata(Player.class);
                String message = arg2.checkjstring();
                player.sendMessage(message);
                return CoerceJavaToLua.coerce(true);
            }
        });

        set("broadcastMessage", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                String message = arg.checkjstring();
                Bukkit.broadcastMessage(message);
                return CoerceJavaToLua.coerce(true);
            }
        });

        set("isAbilityItem", new VarArgFunction() {
            @Override
            public LuaValue invoke(Varargs vargs) {
                ItemStack item = (ItemStack) vargs.checkuserdata(1, ItemStack.class);
                String targetItem = vargs.checkjstring(2);

                if (overrideItem) return CoerceJavaToLua.coerce(item.getType().equals(targetItem));
                else return CoerceJavaToLua.coerce(item.getType().toString().contains(targetItem));
            }
        });

        set("eliminatePlayer", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                Player player = (Player) arg.checkuserdata(Player.class);
                int playerIndex = plugin.gameManager.players.indexOf(player);
                if (playerIndex >= 0){
                    plugin.gameManager.players.get(playerIndex).isSurvive = false;
                    for (Ability a : plugin.gameManager.players.get(playerIndex).getAbility()){
                        LAbilityMain.instance.gameManager.StopPassive(plugin.gameManager.players.get(playerIndex), a);
                        LAbilityMain.instance.gameManager.StopActiveTimer(plugin.gameManager.players.get(playerIndex), a);
                    }
                    player.setGameMode(GameMode.SPECTATOR);
                }
                return NIL;
            }
        });

        set("endGame", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                ScheduleManager.ClearTimer();
                LAbilityMain.instance.gameManager.ResetAll();
                Bukkit.getScheduler().cancelTasks(LAbilityMain.plugin);
                return NIL;
            }
        });
    }
}
/*


 */
