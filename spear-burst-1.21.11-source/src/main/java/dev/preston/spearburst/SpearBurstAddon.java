package dev.preston.spearburst;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class SpearBurstAddon extends MeteorAddon {
    public static final Category CATEGORY = new Category("Spear Burst");
    @Override public void onInitialize() { Modules.get().add(new SpearBurst()); }
    @Override public void onRegisterCategories() { Modules.registerCategory(CATEGORY); }
    @Override public String getPackage() { return "dev.preston.spearburst"; }
}
