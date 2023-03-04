package net.runelite.client.plugins.xCrafter;

import javax.inject.Inject;
import lombok.Getter;
import net.runelite.client.plugins.xRunecrafting.xRunecraftingConfig;

@Getter
public class xCrafterUtils {

    static xCrafterConfig config;

    @Inject
    public xCrafterUtils(xCrafterConfig config) {
        this.config = config;
    }

    static public int BowStringId = 1777;
    static public int FlaxId = 1779;

    public static boolean ConfigIsOther() {
        if (config.xcraftersettings().getName() == "LumbridgeFlax") {
            return false;
        }
        return true;
    }
    public static boolean ConfigIsFlaxSelected() {
        if (config.xcraftersettings().getName() == "LumbridgeFlax") {
            return true;
        }
        return false;
    }
}
