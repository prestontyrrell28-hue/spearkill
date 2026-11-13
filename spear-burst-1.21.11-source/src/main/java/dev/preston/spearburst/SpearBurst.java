package dev.preston.spearburst;

import java.util.Locale;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

/** A physical one-tick burst, not a damage or invisibility guarantee. */
public class SpearBurst extends Module {
    private final SettingGroup general = settings.getDefaultGroup();
    private final Setting<Double> range = general.add(new DoubleSetting.Builder()
        .name("trigger-range").description("Maximum distance to the aimed target, in blocks.")
        .defaultValue(3.5).min(0.5).max(5).sliderRange(0.5, 5).build());
    private final Setting<Double> speed = general.add(new DoubleSetting.Builder()
        .name("burst-speed").description("Requested blocks per tick; capped to avoid passing the target.")
        .defaultValue(3).min(0.1).max(10).sliderRange(0.1, 10).build());
    private boolean spent;
    private boolean braking;
    private boolean previousSprint;
    private int lastUseTicks;
    private volatile boolean corrected;

    public SpearBurst() {
        super(SpearBurstAddon.CATEGORY, "spear-burst",
            "Charge normally, burst once near the aimed target, then brake. Release to rearm.");
    }

    @Override public void onActivate() { clear(); }
    @Override public void onDeactivate() { brake(); clear(); }

    private void clear() {
        spent = false;
        braking = false;
        lastUseTicks = 0;
        corrected = false;
    }

    private void brake() {
        if (braking && mc.player != null) {
            mc.player.setVelocity(Vec3d.ZERO);
            mc.player.setSprinting(previousSprint);
        }
        braking = false;
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        // Fallback braking if a post-tick event was missed.
        brake();
        if (mc.player == null || mc.world == null) { clear(); return; }
        if (corrected) { spent = true; corrected = false; }
        String item = Registries.ITEM.getId(mc.player.getActiveItem().getItem())
            .getPath().toLowerCase(Locale.ROOT);
        int useTicks = mc.player.getItemUseTime();
        if (!mc.player.isUsingItem() || !item.endsWith("_spear")) {
            spent = false;
            lastUseTicks = 0;
            return;
        }
        if (useTicks < lastUseTicks) spent = false;
        lastUseTicks = useTicks;
        if (spent || useTicks <= readyTicks(item) || mc.currentScreen != null
            || !mc.player.isAlive() || mc.player.hasVehicle()) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult hit)
            || !(hit.getEntity() instanceof LivingEntity target)
            || !target.isAlive() || target == mc.player) return;
        if (target instanceof PlayerEntity player
            && (player.isSpectator() || Friends.get().isFriend(player))) return;
        if (!mc.player.canSee(target) || mc.player.distanceTo(target) > range.get()) return;

        // Horizontal impulse: no flight, teleport, rotation snap, or distant chase.
        Vec3d offset = target.getEntityPos().subtract(mc.player.getEntityPos());
        Vec3d horizontal = new Vec3d(offset.x, 0, offset.z);
        double distance = horizontal.length();
        double clearance = (mc.player.getWidth() + target.getWidth()) * 0.5 + 0.1;
        double impulse = Math.min(speed.get(), Math.max(0, distance - clearance));
        if (impulse < 0.05) return;
        Vec3d velocity = horizontal.multiply(impulse / distance);
        // Conservative swept-box test rejects walls along the whole burst.
        if (!mc.world.isSpaceEmpty(mc.player,
            mc.player.getBoundingBox().stretch(velocity))) return;
        previousSprint = mc.player.isSprinting();
        mc.player.setSprinting(true);
        mc.player.setVelocity(velocity.x, mc.player.getVelocity().y, velocity.z);
        spent = true;
        braking = true;
    }

    @EventHandler
    private void onPostTick(TickEvent.Post event) { brake(); }

    @EventHandler
    private void onCorrection(PacketEvent.Receive event) {
        // Never cancel corrections. Handle state changes on the game thread.
        if (event.packet instanceof PlayerPositionLookS2CPacket) corrected = true;
    }

    private static int readyTicks(String item) {
        if (item.startsWith("netherite_")) return 7;
        if (item.startsWith("diamond_")) return 9;
        if (item.startsWith("iron_")) return 11;
        if (item.startsWith("copper_")) return 12;
        if (item.startsWith("stone_") || item.startsWith("golden_")) return 13;
        return 14;
    }
}
