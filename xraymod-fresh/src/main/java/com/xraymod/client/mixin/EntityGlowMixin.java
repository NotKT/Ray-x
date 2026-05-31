package com.xraymod.client.mixin;

import com.xraymod.client.XRayState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Mixin(Entity.class)
public abstract class EntityGlowMixin {

    private static final String TEAM_HOSTILE  = "kps_hostile";
    private static final String TEAM_PASSIVE  = "kps_passive";
    private static final String TEAM_NEUTRAL  = "kps_neutral";
    private static final String TEAM_VEHICLE  = "kps_vehicle";
    private static final String TEAM_PLAYER   = "kps_player";

    private static final Set<String> HOSTILE = new HashSet<>(Arrays.asList(
        "minecraft:blaze", "minecraft:bogged", "minecraft:breeze",
        "minecraft:cave_spider", "minecraft:creeper", "minecraft:drowned",
        "minecraft:elder_guardian", "minecraft:ender_dragon", "minecraft:endermite",
        "minecraft:evoker", "minecraft:ghast", "minecraft:guardian",
        "minecraft:hoglin", "minecraft:husk", "minecraft:illusioner",
        "minecraft:magma_cube", "minecraft:phantom", "minecraft:piglin_brute",
        "minecraft:pillager", "minecraft:ravager", "minecraft:shulker",
        "minecraft:silverfish", "minecraft:skeleton", "minecraft:slime",
        "minecraft:spider", "minecraft:stray", "minecraft:vex",
        "minecraft:vindicator", "minecraft:warden", "minecraft:witch",
        "minecraft:wither", "minecraft:wither_skeleton", "minecraft:zoglin",
        "minecraft:zombie", "minecraft:zombie_villager", "minecraft:zombified_piglin",
        
    ));

    private static final Set<String> PASSIVE = new HashSet<>(Arrays.asList(
        "minecraft:allay", "minecraft:armadillo", "minecraft:axolotl",
        "minecraft:bat", "minecraft:camel", "minecraft:cat",
        "minecraft:chicken", "minecraft:cod", "minecraft:cow",
        "minecraft:donkey", "minecraft:frog", "minecraft:glow_squid",
        "minecraft:horse", "minecraft:mooshroom", "minecraft:mule",
        "minecraft:ocelot", "minecraft:panda", "minecraft:parrot",
        "minecraft:pig", "minecraft:pufferfish", "minecraft:rabbit",
        "minecraft:salmon", "minecraft:sheep", "minecraft:skeleton_horse",
        "minecraft:sniffer", "minecraft:snow_golem", "minecraft:squid",
        "minecraft:strider", "minecraft:tadpole", "minecraft:tropical_fish",
        "minecraft:turtle", "minecraft:villager", "minecraft:wandering_trader",
        "minecraft:zombie_horse"
    ));

    private static final Set<String> NEUTRAL = new HashSet<>(Arrays.asList(
        "minecraft:bee", "minecraft:dolphin","minecraft:enderman", "minecraft:fox",
        "minecraft:goat", "minecraft:iron_golem", "minecraft:llama",
        "minecraft:piglin", "minecraft:polar_bear", "minecraft:trader_llama",
        "minecraft:wolf"
    ));

    private static final Set<String> ITEMS_VEHICLES = new HashSet<>(Arrays.asList(
        "minecraft:armor_stand", "minecraft:arrow", "minecraft:chest_boat",
        "minecraft:chest_minecart", "minecraft:command_block_minecart",
        "minecraft:dragon_fireball", "minecraft:egg", "minecraft:end_crystal",
        "minecraft:ender_pearl", "minecraft:experience_bottle",
        "minecraft:experience_orb", "minecraft:eye_of_ender",
        "minecraft:falling_block", "minecraft:fireball", "minecraft:firework_rocket",
        "minecraft:fishing_bobber", "minecraft:furnace_minecart",
        "minecraft:hopper_minecart", "minecraft:item", "minecraft:item_display",
        "minecraft:item_frame", "minecraft:glow_item_frame", "minecraft:leash_knot",
        "minecraft:lightning_bolt", "minecraft:llama_spit", "minecraft:minecart",
        "minecraft:painting", "minecraft:potion", "minecraft:shulker_bullet",
        "minecraft:small_fireball", "minecraft:snowball", "minecraft:spawner_minecart",
        "minecraft:spectral_arrow", "minecraft:tnt", "minecraft:tnt_minecart",
        "minecraft:trident", "minecraft:wind_charge", "minecraft:breeze_wind_charge",
        "minecraft:wither_skull", "minecraft:block_display", "minecraft:text_display",
        "minecraft:interaction", "minecraft:marker", "minecraft:ominous_item_spawner"
    ));

    @Inject(method = "isGlowing", at = @At("RETURN"), cancellable = true)
    private void xray_forceGlow(CallbackInfoReturnable<Boolean> cir) {
        if (!XRayState.entityGlowActive) return;

        Entity self = (Entity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null) return;
        if (self == client.player) return;

        // Range check
        int rangeBlocks = XRayState.config != null
            ? XRayState.config.getEntityGlowRange() * 16 : 96;
        if (client.player.squaredDistanceTo(self) > rangeBlocks * rangeBlocks) return;

        // Whitelist check
        String entityId = Registries.ENTITY_TYPE.getId(self.getType()).toString();
        if (XRayState.config == null || !XRayState.config.shouldGlow(entityId)) return;

        // Assign color team
        if (client.world != null) {
            Scoreboard scoreboard = client.world.getScoreboard();
            ensureTeams(scoreboard);
            String teamName = getTeamName(entityId);
            Team team = scoreboard.getTeam(teamName);
            if (team != null && self.getScoreboardTeam() == null) {
                scoreboard.addScoreHolderToTeam(self.getNameForScoreboard(), team);
            }
        }

        cir.setReturnValue(true);
    }

    private String getTeamName(String entityId) {
        if (entityId.equals("minecraft:player")) return TEAM_PLAYER;
        if (HOSTILE.contains(entityId)) return TEAM_HOSTILE;
        if (PASSIVE.contains(entityId)) return TEAM_PASSIVE;
        if (NEUTRAL.contains(entityId)) return TEAM_NEUTRAL;
        if (ITEMS_VEHICLES.contains(entityId)) return TEAM_VEHICLE;
        return TEAM_NEUTRAL; // default
    }

    private void ensureTeams(Scoreboard scoreboard) {
        createTeamIfAbsent(scoreboard, TEAM_HOSTILE, Formatting.RED);
        createTeamIfAbsent(scoreboard, TEAM_PASSIVE, Formatting.GREEN);
        createTeamIfAbsent(scoreboard, TEAM_NEUTRAL, Formatting.YELLOW);
        createTeamIfAbsent(scoreboard, TEAM_VEHICLE, Formatting.AQUA);
        createTeamIfAbsent(scoreboard, TEAM_PLAYER, Formatting.WHITE);
    }

    private void createTeamIfAbsent(Scoreboard scoreboard, String name, Formatting color) {
        if (scoreboard.getTeam(name) == null) {
            Team team = scoreboard.addTeam(name);
            team.setColor(color);
        }
    }
}
