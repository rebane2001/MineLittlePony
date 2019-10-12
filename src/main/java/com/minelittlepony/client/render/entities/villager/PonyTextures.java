package com.minelittlepony.client.render.entities.villager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloadListener;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;

import com.minelittlepony.client.MineLittlePony;
import com.minelittlepony.util.resources.ITextureSupplier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Cached pool of villager textures.
 */
public class PonyTextures<T extends LivingEntity & VillagerDataContainer> implements ITextureSupplier<T>, SynchronousResourceReloadListener {

    private final ITextureSupplier<String> formatter;

    private final Identifier fallback;

    private final Map<String, Identifier> cache = new HashMap<>();

    private final Identifier egg;
    private final Identifier egg2;

    private final ReloadableResourceManager resourceManager;

    /**
     * Creates a new profession cache
     *
     * @param formatter Formatter used when creating new textures
     * @param keyMapper Mapper to convert integer ids into a string value for format insertion
     * @param fallback  The default if any generated textures fail to load. This is stored in place of failing textures.
     */
    public PonyTextures(ITextureSupplier<String> formatter) {
        this.resourceManager = (ReloadableResourceManager)MinecraftClient.getInstance().getResourceManager();
        this.formatter = formatter;
        this.fallback = formatter.supplyTexture("villager_pony");
        this.egg = formatter.supplyTexture("silly_pony");
        this.egg2 = formatter.supplyTexture("tiny_silly_pony");

        resourceManager.registerListener(this);
    }

    @Override
    public void apply(ResourceManager manager) {
        cache.clear();
    }

    @Override
    public Identifier supplyTexture(T entity) {
        if (isBestPony(entity)) {
            return entity.isBaby() ? egg2 : egg;
        }

        VillagerData t = entity.getVillagerData();

        return getTexture(t.getType(), t.getProfession());
    }

    private Identifier getTexture(final VillagerType type, final VillagerProfession profession) {

        if (profession == VillagerProfession.NONE) {
            return fallback;
        }

        String key = String.format("pony/%s/%s", type, profession);

        if (cache.containsKey(key)) {
            return cache.get(key); // People often complain that villagers cause lag,
                                   // so let's do better than Mojang and rather NOT go
                                   // through all the lambda generations if we can avoid it.
        }

        Identifier result = verifyTexture(formatter.supplyTexture(key)).orElseGet(() -> {
            if (type == VillagerType.PLAINS) {
                // if texture loading fails, use the fallback.
                return fallback;
            }

            return getTexture(VillagerType.PLAINS, profession);
        });

        cache.put(key, result);
        return result;
    }

    protected Optional<Identifier> verifyTexture(Identifier texture) {
        if (!resourceManager.containsResource(texture)) {
            MineLittlePony.logger.warn("Villager texture `" + texture + "` was not found.");
            return Optional.empty();
        }

        return Optional.of(texture);
    }

    public static boolean isBestPony(LivingEntity entity) {
        if (!entity.hasCustomName()) {
            return false;
        }
        String name = entity.getCustomName().getString();
        return "Derpy".equals(name) || (entity.isBaby() && "Dinky".equals(name));
    }

    public static boolean isCrownPony(LivingEntity entity) {
        return isBestPony(entity) && entity.getUuid().getLeastSignificantBits() % 20 == 0;
    }
}
