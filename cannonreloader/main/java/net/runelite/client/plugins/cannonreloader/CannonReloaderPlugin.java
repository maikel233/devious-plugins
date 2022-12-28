package net.runelite.client.plugins.cannonreloader;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.runelite.api.ObjectID.*;
import static net.runelite.api.ProjectileID.CANNONBALL;
import static net.runelite.api.ProjectileID.GRANITE_CANNONBALL;

@PluginDescriptor(
        name = "Cannon Reloader",
        description = "Automatically reload your cannon",
        tags = {"combat", "notifications", "ranged"},
        enabledByDefault = false
)
public class CannonReloaderPlugin extends Plugin {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("([0-9]+)");
    private static final int MAX_CBALLS = 30;
    private static final int MAX_DISTANCE = 2500;
    private int nextReloadCount = 10;
    private boolean skipProjectileCheckThisTick;

    @Inject
    private CannonReloaderConfig config;

    @Provides
    public CannonReloaderConfig getConfig(final ConfigManager configManager) {
        return configManager.getConfig(CannonReloaderConfig.class);
    }

    private int cballsLeft;

    private Random r = new Random();

    private boolean cannonPlaced;

    private WorldPoint cannonPosition;

    private GameObject cannon;

    private int tickDelay;

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Override
    protected void startUp()
    {
        nextReloadCount = r.nextInt(config.maxReloadAmount() - config.minReloadAmount()) + config.minReloadAmount();
    }

    @Override
    protected void shutDown()
    {
        cannonPlaced = false;
        cannonPosition = null;
        cballsLeft = 0;
        skipProjectileCheckThisTick = false;
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        GameObject gameObject = event.getGameObject();

        Player localPlayer = client.getLocalPlayer();

        if (gameObject.getId() == CANNON_BASE && !cannonPlaced) {
            if (localPlayer != null && localPlayer.getWorldLocation().distanceTo(gameObject.getWorldLocation()) <= 2
                    && localPlayer.getAnimation() == AnimationID.BURYING_BONES) {
                cannonPosition = gameObject.getWorldLocation();
                cannon = gameObject;
            }
        }

        //Object ID = 14916
        if (gameObject.getId() == BROKEN_MULTICANNON_14916 && cannonPlaced) {
            if (cannonPosition.equals(gameObject.getWorldLocation())) {
                clientThread.invoke(() ->
                        client.invokeMenuAction(
                                "Repair",
                                "<col=ffff>Broken multicannon",
                                gameObject.getId(),
                                MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                                cannon.getSceneMinLocation().getX(),
                                cannon.getSceneMinLocation().getY()
                        )
                );
                tickDelay = 3;
            }
        }
    }

    @Subscribe
    private void onProjectileMoved(ProjectileMoved event) {
        Projectile projectile = event.getProjectile();

        if ((projectile.getId() == CANNONBALL || projectile.getId() == GRANITE_CANNONBALL) && cannonPosition != null) {
            WorldPoint projectileLoc = WorldPoint.fromLocal(client, projectile.getX1(), projectile.getY1(), client.getPlane());

            //Check to see if projectile x,y is 0 else it will continuously decrease while ball is flying.
            if (projectileLoc.equals(cannonPosition) && projectile.getX() == 0 && projectile.getY() == 0) {
                // When there's a chat message about cannon reloaded/unloaded/out of ammo,
                // the message event runs before the projectile event. However they run
                // in the opposite order on the server. So if both fires in the same tick,
                // we don't want to update the cannonball counter if it was set to a specific
                // amount.
                if (!skipProjectileCheckThisTick) {
                    cballsLeft--;
                }
            }
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.SPAM && event.getType() != ChatMessageType.GAMEMESSAGE) {
            return;
        }

        if (event.getMessage().equals("You add the furnace.")) {
            cannonPlaced = true;
            cballsLeft = 0;
        }

        if (event.getMessage().contains("You pick up the cannon")) {
            cannonPlaced = false;
            cballsLeft = 0;
        }

        if (event.getMessage().startsWith("You load the cannon with")) {
            nextReloadCount = r.nextInt(config.maxReloadAmount() - config.minReloadAmount()) + config.minReloadAmount();

            Matcher m = NUMBER_PATTERN.matcher(event.getMessage());
            if (m.find()) {
                // The cannon will usually refill to MAX_CBALLS, but if the
                // player didn't have enough cannonballs in their inventory,
                // it could fill up less than that. Filling the cannon to
                // cballsLeft + amt is not always accurate though because our
                // counter doesn't decrease if the player has been too far away
                // from the cannon due to the projectiels not being in memory,
                // so our counter can be higher than it is supposed to be.
                int amt = Integer.parseInt(m.group());
                if (cballsLeft + amt >= MAX_CBALLS) {
                    skipProjectileCheckThisTick = true;
                    cballsLeft = MAX_CBALLS;
                } else {
                    cballsLeft += amt;
                }
            } else if (event.getMessage().equals("You load the cannon with one cannonball.")) {
                if (cballsLeft + 1 >= MAX_CBALLS) {
                    skipProjectileCheckThisTick = true;
                    cballsLeft = MAX_CBALLS;
                } else {
                    cballsLeft++;
                }
            }
        }

        if (event.getMessage().contains("Your cannon is out of ammo!")) {
            skipProjectileCheckThisTick = true;

            // If the player was out of range of the cannon, some cannonballs
            // may have been used without the client knowing, so having this
            // extra check is a good idea.
            cballsLeft = 0;
        }

        if (event.getMessage().startsWith("You unload your cannon and receive Cannonball")
                || event.getMessage().startsWith("You unload your cannon and receive Granite cannonball")) {
            skipProjectileCheckThisTick = true;

            cballsLeft = 0;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        skipProjectileCheckThisTick = false;

        if (tickDelay > 0) {
            tickDelay--;
            return;
        }

        if (!cannonPlaced || cannonPosition == null || cballsLeft > nextReloadCount) {
            return;
        }

        clientThread.invoke(() ->
                client.invokeMenuAction(
                        "Fire",
                        "<col=ffff>Dwarf multicannon",
                        DWARF_MULTICANNON,
                        MenuAction.GAME_OBJECT_FIRST_OPTION.getId(),
                        cannon.getSceneMinLocation().getX(),
                        cannon.getSceneMinLocation().getY()
                )
        );

        tickDelay = 3;

        nextReloadCount = r.nextInt(config.maxReloadAmount() - config.minReloadAmount()) + config.minReloadAmount();
    }
}