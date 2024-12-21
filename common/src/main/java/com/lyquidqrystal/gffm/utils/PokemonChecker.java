package com.lyquidqrystal.gffm.utils;

import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.lyquidqrystal.gffm.GainFriendshipFromMelodies;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PokemonChecker {
    public static boolean checkName(String name, Pokemon pokemon) {
        if (name.equalsIgnoreCase("any")) {
            return true;
        }
        return pokemon.getSpecies().getName().equalsIgnoreCase(name);
    }

    public static boolean checkNature(String natureName, Pokemon pokemon) {
        if (natureName.equalsIgnoreCase("any")) {
            return true;
        }
        return pokemon.getNature().getDisplayName().equalsIgnoreCase("cobblemon.nature." + natureName);
    }

    public static boolean checkAbility(String abilityName, Pokemon pokemon) {
        if (abilityName.equalsIgnoreCase("any")) {
            return true;
        }
        return pokemon.getAbility().getName().equalsIgnoreCase(abilityName);
    }

    public static boolean checkMove(String moveName, Pokemon pokemon) {
        if (moveName.equalsIgnoreCase("any")) {
            return true;
        }
        for (Move move : pokemon.getMoveSet().getMoves()) {
            if (move.getName().equalsIgnoreCase(moveName)) {
                return true;
            }
        }

        return false;
    }

    public static boolean checkFriendship(int requiredFriendship, Pokemon pokemon) {
        if (requiredFriendship == -1) {
            return true;
        }
        return requiredFriendship <= pokemon.getFriendship();
    }

    public static boolean checkGender(String gender, Pokemon pokemon) {
        if (gender.equalsIgnoreCase("any")) {
            return true;
        }
        return pokemon.getGender().name().equalsIgnoreCase(gender);
    }

    public static boolean checkRegionalForm(String formName, Pokemon pokemon) {
        if (formName.equalsIgnoreCase("any")) {
            return true;
        }
        return pokemon.getForm().getName().equalsIgnoreCase(formName);
    }
    public static String match(String rule, Pokemon pokemon) {
        Pattern pattern = Pattern.compile("([^|]+)\\|([^|]+)\\|([^|]+)\\|([^|]+)\\|([^|]+)\\|([^|]+)\\|([^|]+)\\|([^|]+)");
        Matcher matcher = pattern.matcher(rule.toLowerCase());
        String result = "";
        if (pokemon == null) {
            return result;
        }
        if (matcher.find()) {
            int friendshipValue = 0;

            String tmp = matcher.group(8);
            if (tmp.equalsIgnoreCase("any")) {
                friendshipValue = -1;
            }else{
                try{
                    friendshipValue=Integer.parseInt(tmp);
                }
                catch (NumberFormatException e){
                    GainFriendshipFromMelodies.LOGGER.info("Failed to convert the friendship value");
                }
            }

            boolean canUse = checkName(matcher.group(2), pokemon)
                    && checkRegionalForm(matcher.group(3), pokemon)
                    && checkGender(matcher.group(4), pokemon)
                    && checkNature(matcher.group(5), pokemon)
                    && checkAbility(matcher.group(6), pokemon)
                    && checkMove(matcher.group(7), pokemon)
                    && checkFriendship(friendshipValue, pokemon);
            if (canUse) {
                result = matcher.group(1);
                //GainFriendshipFromMelodies.LOGGER.info("Available:"+result);
            }
        }
        return result;
    }
}
