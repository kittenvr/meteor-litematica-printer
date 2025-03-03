package com.kkllffaa.meteor_litematica_printer;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class Breaker extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRendering = settings.createGroup("Rendering");

    private final Setting<Integer> breakingRange = sgGeneral.add(new IntSetting.Builder()
        .name("breaking-range")
        .description("The block breaking range.")
        .defaultValue(5)
        .min(1).sliderMin(1)
        .max(6).sliderMax(6)
        .build()
    );

    private final Setting<Integer> breakingDelay = sgGeneral.add(new IntSetting.Builder()
        .name("breaking-delay")
        .description("Delay between breaking blocks in ticks.")
        .defaultValue(2)
        .min(0).sliderMin(0)
        .max(100).sliderMax(40)
        .build()
    );

    private final Setting<Integer> bpt = sgGeneral.add(new IntSetting.Builder()
        .name("blocks/tick")
        .description("How many blocks to break per tick.")
        .defaultValue(1)
        .min(1).sliderMin(1)
        .max(100).sliderMax(100)
        .build()
    );

    private final Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder()
        .name("swing")
        .description("Swing hand when breaking.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> renderBlocks = sgRendering.add(new BoolSetting.Builder()
        .name("render-broken-blocks")
        .description("Renders block breakings.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> fadeTime = sgRendering.add(new IntSetting.Builder()
        .name("fade-time")
        .description("Time for the rendering to fade, in ticks.")
        .defaultValue(3)
        .min(1).sliderMin(1)
        .max(1000).sliderMax(20)
        .visible(renderBlocks::get)
        .build()
    );

    private final Setting<SettingColor> colour = sgRendering.add(new ColorSetting.Builder()
        .name("colour")
        .description("The cubes colour.")
        .defaultValue(new SettingColor(255, 0, 0))
        .visible(renderBlocks::get)
        .build()
    );

    private int timer;
    private final List<BlockPos> toBreak = new ArrayList<>();
    private final List<Pair<Integer, BlockPos>> broken_fade = new ArrayList<>();

    public Breaker() {
        super(Addon.CATEGORY, "litematica-breaker", "Automatically breaks blocks based on litematica verifier.");
    }

    @Override
    public void onActivate() {
        onDeactivate();
    }

    @Override
    public void onDeactivate() {
        broken_fade.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) {
            broken_fade.clear();
            return;
        }

        broken_fade.forEach(s -> s.setLeft(s.getLeft() - 1));
        broken_fade.removeIf(s -> s.getLeft() <= 0);

        if (timer >= breakingDelay.get()) {
            // Logic to find blocks to break based on litematica verifier
            // Add blocks to toBreak list

            int broken = 0;
            for (BlockPos pos : toBreak) {
                BlockState state = mc.world.getBlockState(pos);

                if (breakBlock(state, pos)) {
                    timer = 0;
                    broken++;
                    if (renderBlocks.get()) {
                        broken_fade.add(new Pair<>(fadeTime.get(), new BlockPos(pos)));
                    }
                    if (broken >= bpt.get()) {
                        return;
                    }
                }
            }
        } else timer++;
    }

    public boolean breakBlock(BlockState state, BlockPos pos) {
        if (mc.player == null || mc.world == null) return false;

        // Logic to break the block at the given position
        // Use mc.interactionManager to break the block

        return true;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        broken_fade.forEach(s -> {
            Color a = new Color(colour.get().r, colour.get().g, colour.get().b, (int) (((float)s.getLeft() / (float) fadeTime.get()) * colour.get().a));
            event.renderer.box(s.getRight(), a, null, ShapeMode.Sides, 0);
        });
    }
}
