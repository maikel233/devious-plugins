package net.runelite.client.plugins.aoewarnings;

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@PluginDescriptor(
	name = "AoE warnings"
)
public class AoeWarningPlugin extends Plugin
{
	private static final int VERZIK_REGION = 12611;
	private static final int GROTESQUE_GUARDIANS_REGION = 6727;
	@Getter(AccessLevel.PACKAGE)
	private final Set<CrystalBomb> bombs = new HashSet<>();
	@Getter(AccessLevel.PACKAGE)
	private final Set<ProjectileContainer> projectiles = new HashSet<>();
	@com.google.inject.Inject
	public AoeWarningConfig config;
	@Inject
	private AoeWarningOverlay coreOverlay;
	@Inject
	private BombOverlay bombOverlay;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private ConfigManager configManager;
	@Inject
	private Client client;
	@Getter(AccessLevel.PACKAGE)
	private final List<WorldPoint> lightningTrail = new ArrayList<>();
	@Getter(AccessLevel.PACKAGE)
	private final List<GameObject> acidTrail = new ArrayList<>();
	@Getter(AccessLevel.PACKAGE)
	private final List<GameObject> crystalSpike = new ArrayList<>();
	@Getter(AccessLevel.PACKAGE)
	private final List<GameObject> wintertodtSnowFall = new ArrayList<>();

	@Provides
	AoeWarningConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AoeWarningConfig.class);
	}




	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(coreOverlay);
		overlayManager.add(bombOverlay);
		reset();
	}


	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(coreOverlay);
		overlayManager.remove(bombOverlay);
		reset();
	}

	@Subscribe
	private void onProjectileSpawned(ProjectileSpawned event) {
		final Projectile projectile = event.getProjectile();

		if (AoeProjectileInfo.getById(projectile.getId()) == null) {
			return;
		}

		final int id = projectile.getId();
		final int lifetime = config.delay() + (projectile.getRemainingCycles() * 20);
		int ticksRemaining = projectile.getRemainingCycles() / 30;
		if (!isTickTimersEnabledForProjectileID(id)) {
			ticksRemaining = 0;
		}
		final int tickCycle = client.getTickCount() + ticksRemaining;
		if (isConfigEnabledForProjectileId(id)) {
			projectiles.add(new ProjectileContainer(projectile, Instant.now(), lifetime, tickCycle));

		}
	}

	@Subscribe
	private void onProjectileMoved(ProjectileMoved event) {
		if (projectiles.isEmpty()) {
			return;
		}

		final Projectile projectile = event.getProjectile();

		projectiles.forEach(proj ->
		{
			if (proj.getProjectile() == projectile) {
				proj.setTargetPoint(event.getPosition());
			}
		});
	}

	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned event) {
		final GameObject gameObject = event.getGameObject();

		switch (gameObject.getId()) {
			case ObjectID.CRYSTAL_BOMB:
				bombs.add(new CrystalBomb(gameObject, client.getTickCount()));

				break;
			case ObjectID.ACID_POOL:
				acidTrail.add(gameObject);
				break;
			case ObjectID.SMALL_CRYSTALS:
				crystalSpike.add(gameObject);
				break;
			case NullObjectID.NULL_26690:
				if (config.isWintertodtEnabled()) {
					wintertodtSnowFall.add(gameObject);
				}
				break;
		}
	}

	@Subscribe
	private void onGameObjectDespawned(GameObjectDespawned event) {
		final GameObject gameObject = event.getGameObject();

		switch (gameObject.getId()) {
			case ObjectID.CRYSTAL_BOMB:
				bombs.removeIf(o -> o.getGameObject() == gameObject);
				break;
			case ObjectID.ACID_POOL:
				acidTrail.remove(gameObject);
				break;
			case ObjectID.SMALL_CRYSTALS:
				crystalSpike.remove(gameObject);
				break;
			case NullObjectID.NULL_26690:
				wintertodtSnowFall.remove(gameObject);
				break;
		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event) {
		if (event.getGameState() == GameState.LOGGED_IN) {
			return;
		}
		reset();
	}

	@Subscribe
	private void onGameTick(GameTick event) {
		lightningTrail.clear();

		if (config.LightningTrail()) {
			client.getGraphicsObjects().forEach(o ->
			{
				if (o.getId() == GraphicID.OLM_LIGHTNING) {
					lightningTrail.add(WorldPoint.fromLocal(client, o.getLocation()));
				}
			});
		}

		bombs.forEach(CrystalBomb::bombClockUpdate);
	}

	private boolean isTickTimersEnabledForProjectileID(int projectileId) {
		AoeProjectileInfo projectileInfo = AoeProjectileInfo.getById(projectileId);

		if (projectileInfo == null) {
			return false;
		}

		switch (projectileInfo) {
			case VASA_RANGED_AOE:
			case VORKATH_POISON_POOL:
			case VORKATH_SPAWN:
			case VORKATH_TICK_FIRE:
			case OLM_BURNING:
			case OLM_FALLING_CRYSTAL_TRAIL:
			case OLM_ACID_TRAIL:
			case OLM_FIRE_LINE:
				return false;
		}

		return true;
	}

	private boolean isConfigEnabledForProjectileId(int projectileId) {
		AoeProjectileInfo projectileInfo = AoeProjectileInfo.getById(projectileId);
		if (projectileInfo == null) {
			return false;
		}

		switch (projectileInfo) {
			case LIZARDMAN_SHAMAN_AOE:
				return config.isShamansEnabled();
			case CRAZY_ARCHAEOLOGIST_AOE:
				return config.isArchaeologistEnabled();
			case ICE_DEMON_RANGED_AOE:
			case ICE_DEMON_ICE_BARRAGE_AOE:
				return config.isIceDemonEnabled();
			case VASA_AWAKEN_AOE:
			case VASA_RANGED_AOE:
				return config.isVasaEnabled();
			case TEKTON_METEOR_AOE:
				return config.isTektonEnabled();
			case VORKATH_BOMB:
			case VORKATH_POISON_POOL:
			case VORKATH_SPAWN:
			case VORKATH_TICK_FIRE:
				return  config.vorkathModes().contains(AoeWarningConfig.VorkathMode.of(projectileInfo));
			case VETION_LIGHTNING:
				return config.isVetionEnabled();
			case CHAOS_FANATIC:
				return config.isChaosFanaticEnabled();
			case GALVEK_BOMB:
			case GALVEK_MINE:
				return config.isGalvekEnabled();
			case DAWN_FREEZE:
			case DUSK_CEILING:
				if (regionCheck(GROTESQUE_GUARDIANS_REGION)) {
					return config.isGargBossEnabled();
				}
			case VERZIK_P1_ROCKS:
				if (regionCheck(VERZIK_REGION)) {
					return config.isVerzikEnabled();
				}
			case OLM_FALLING_CRYSTAL:
			case OLM_BURNING:
			case OLM_FALLING_CRYSTAL_TRAIL:
			case OLM_ACID_TRAIL:
			case OLM_FIRE_LINE:
				return config.isOlmEnabled();
			case CORPOREAL_BEAST:
			case CORPOREAL_BEAST_DARK_CORE:
				return config.isCorpEnabled();
			case XARPUS_POISON_AOE:
				return config.isXarpusEnabled();
			case ADDY_DRAG_POISON:
				return config.addyDrags();
			case DRAKE_BREATH:
				return config.isDrakeEnabled();
			case CERB_FIRE:
				return config.isCerbFireEnabled();
			case DEMONIC_GORILLA_BOULDER:
				return config.isDemonicGorillaEnabled();
			case VERZIK_PURPLE_SPAWN:
				return config.isVerzikEnabled();
		}

		return false;
	}

	private void reset() {
		lightningTrail.clear();
		acidTrail.clear();
		crystalSpike.clear();
		wintertodtSnowFall.clear();
		bombs.clear();
		projectiles.clear();
	}

	private boolean regionCheck(int region) {
		return ArrayUtils.contains(client.getMapRegions(), region);
	}
}