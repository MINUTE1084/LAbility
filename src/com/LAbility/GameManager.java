package com.LAbility;

import com.LAbility.LuaUtility.AbilityList;
import com.LAbility.LuaUtility.PlayerList;
import joptsimple.util.KeyValuePair;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.util.ArrayList;
import java.util.Random;

public class GameManager {
    public boolean isGameStarted = false;
    public PlayerList<LAPlayer> players = new PlayerList<LAPlayer>();

    public ArrayList<Integer> shuffledAbilityIndex = new ArrayList<Integer>();
    public int currentAbilityIndex = 0;

    public int abilityAmount = 1;
    public boolean overlapAbility = false;
    public boolean raffleAbility = true;

    public void ResetAll(){
        isGameStarted = false;
        currentAbilityIndex = 0;
        shuffledAbilityIndex = new ArrayList<Integer>();
        StopAllPassive();
        StopAllActiveTimer();
        players = new PlayerList<LAPlayer>();
        LAbilityMain.instance.assignAllPlayer();
    }

    public void RunEvent(Ability ability, Event event) {
        if (isGameStarted){
            for (LAPlayer player : players){
                for (Ability ab : player.ability){
                    if (ab.abilityID.equals(ability.abilityID) && ab.eventFunc.contains(event)){
                        ab.UseEventFunc(event);
                        return;
                    }
                }
            }
        }
    }

    public void RunAllPassive() {
        if (isGameStarted) {
            for (LAPlayer player : players) {
                for (Ability ability : player.ability) {
                    RunPassive(player, ability);
                }
            }
        }
    }

    public void StopAllPassive(){
        for (LAPlayer player : players) {
            for (Ability ability : player.getAbility()){
                StopPassive(player, ability);
            }
        }
    }

    public void StopAllActiveTimer(){
        for (LAPlayer player : players) {
            for (Ability ability : player.getAbility()) {
                StopActiveTimer(player, ability);
            }
        }
    }

    public void RunPassive(LAPlayer player, Ability targetAbility) {
        int abilityIndex = player.ability.indexOf(targetAbility.abilityID);
        if (abilityIndex < 0) return;
        for (Ability.PassiveFunc pf : player.ability.get(abilityIndex).passiveFunc) {
            if (pf.delay > 0) {
                pf.scheduler = LAbilityMain.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(LAbilityMain.plugin, new Runnable() {
                    public void run() {
                        pf.function.call(CoerceJavaToLua.coerce(player.player));
                    }
                }, 0, pf.delay);
            }
            else pf.function.call(CoerceJavaToLua.coerce(player.player));
        }
    }

    public void StopPassive(LAPlayer player, Ability targetAbility){
        int abilityIndex = player.ability.indexOf(targetAbility.abilityID);
        if (abilityIndex < 0) return;

        for (LuaFunction func : player.ability.get(abilityIndex).resetFunc) {
            func.call(CoerceJavaToLua.coerce(player));
        }
        for (Ability.PassiveFunc pf : player.ability.get(abilityIndex).passiveFunc) {
            if (pf.delay > 0) {
                Bukkit.getScheduler().cancelTask(pf.scheduler);
            }
        }
    }

    public void StopActiveTimer(LAPlayer player, Ability targetAbility){
        int abilityIndex = player.ability.indexOf(targetAbility.abilityID);
        if (abilityIndex < 0) return;
        for (Ability.ActiveFunc af : player.ability.get(abilityIndex).eventFunc) {
            Bukkit.getScheduler().cancelTask(af.cooldown.currentSchedule);
        }
    }

    public void AbilityShuffle(boolean resetShuffleIndex) {
        Random random = new Random();
        int size = 0;

        if (resetShuffleIndex) {
            size = LAbilityMain.instance.abilities.size();
            for (int i = 0; i < size; i++) {
                if (!LAbilityMain.instance.abilities.get(i).abilityID.contains("HIDDEN")) shuffledAbilityIndex.add(i);
            }
        }
        else {
            size = shuffledAbilityIndex.size();
        }

        if (size < 2) return;
        for (int i = 0 ; i < 1000; i++) {
            int randomIndex = random.nextInt(size - 1);
            final int temp = shuffledAbilityIndex.get(0);
            shuffledAbilityIndex.set(0, shuffledAbilityIndex.get(randomIndex));
            shuffledAbilityIndex.set(randomIndex, temp);
        }
    }

    public void AssignAbility() {
        if (shuffledAbilityIndex.size() < 1) AbilityShuffle(true);
        for (LAPlayer player : players){
            for (int i = 0; i < abilityAmount; i++){
                AssignAbility(player);
            }
        }
    }

    public void AssignAbility(LAPlayer player) {
        if (overlapAbility) {
            Random random = new Random();
            Ability temp = LAbilityMain.instance.abilities.get(random.nextInt(LAbilityMain.instance.abilities.size() - 1));
            while (player.ability.contains(temp.abilityID)) temp = LAbilityMain.instance.abilities.get(random.nextInt(LAbilityMain.instance.abilities.size() - 1));
            player.ability.add(new Ability(temp));
        }
        else player.ability.add(new Ability(LAbilityMain.instance.abilities.get(shuffledAbilityIndex.get(0))));
        shuffledAbilityIndex.remove(0);

        player.player.sendMessage("\2472[\247aLAbility\2472] \247a" + "????????? ????????? ?????????????????????.");
        player.player.sendMessage("\2472[\247aLAbility\2472] \247a" + "/la check " + (player.ability.size() - 1) + "??? ????????? ??????????????????.");
    }

    public void ResignAbility(LAPlayer player, Ability ability) {
        if (player.ability.contains(ability.abilityID)) {
            player.ability.remove(ability);
            if (!ability.abilityID.contains("HIDDEN")) shuffledAbilityIndex.add(LAbilityMain.instance.abilities.indexOf(ability));
            AbilityShuffle(false);
        }
    }

    public void ResignAbility(LAPlayer player) {
        ArrayList<Ability> tempArray = player.ability;
        if (tempArray.size() > 0) {
            for (Ability a : tempArray) {
                if (!a.abilityID.contains("HIDDEN")) shuffledAbilityIndex.add(LAbilityMain.instance.abilities.indexOf(a.abilityID));
            }
            AbilityShuffle(false);

            for (Ability a : player.ability){
                LAbilityMain.instance.gameManager.StopPassive(player, a);
                LAbilityMain.instance.gameManager.StopActiveTimer(player, a);
            }
            player.ability.clear();
        }
    }

    public void ResignAbility() {
        for (LAPlayer player : players){
            ResignAbility(player);
        }
    }

    public boolean IsAllAsigned(){
        for (LAPlayer player : players){
            if (player.isAssign == false) return false;
        }
        return true;
    }
}
