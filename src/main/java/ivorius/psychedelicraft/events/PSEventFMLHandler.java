/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.events;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.blocks.PSBlocks;
import ivorius.psychedelicraft.client.rendering.DrugEffectInterpreter;
import ivorius.psychedelicraft.client.rendering.SmoothCameraHelper;
import ivorius.psychedelicraft.client.rendering.shaders.PSRenderStates;
import ivorius.psychedelicraft.config.PSConfig;
import ivorius.psychedelicraft.crafting.RecipeActionRegistry;
import ivorius.psychedelicraft.entities.EntityRealityRift;
import ivorius.psychedelicraft.entities.drugs.DrugProperties;
import ivorius.psychedelicraft.gui.UpdatableContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;

/**
 * Created by lukas on 18.02.14.
 */
public class PSEventFMLHandler
{
    public void register()
    {
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent event)
    {
        if ((event.type == TickEvent.Type.CLIENT || event.type == TickEvent.Type.SERVER) && event.phase == TickEvent.Phase.END)
        {
            for (FMLInterModComms.IMCMessage message : FMLInterModComms.fetchRuntimeMessages(Psychedelicraft.instance))
            {
                Psychedelicraft.communicationHandler.onIMCMessage(message, event.type == TickEvent.Type.SERVER, true);
            }
        }

        if (event.type == TickEvent.Type.CLIENT && event.phase == TickEvent.Phase.START)
        {
            PSRenderStates.update();
        }

        if (event.type == TickEvent.Type.RENDER && event.phase == TickEvent.Phase.START)
        {
            PSBlocks.psycheLeaves.setGraphicsLevel(Minecraft.getMinecraft().gameSettings.fancyGraphics);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            DrugProperties drugProperties = DrugProperties.getDrugProperties(event.player);

            if (drugProperties != null)
            {
                drugProperties.updateDrugEffects(event.player);

                if (!event.player.getEntityWorld().isRemote && PSConfig.randomTicksUntilRiftSpawn > 0)
                {
                    if (event.player.getRNG().nextInt(PSConfig.randomTicksUntilRiftSpawn) == 0)
                    {
                        spawnRiftAtPlayer(event.player);
                    }
                }

                Container container = event.player.openContainer;
                if (container instanceof UpdatableContainer)
                    ((UpdatableContainer) container).updateAsCustomContainer();
            }
        }
    }

    public static void spawnRiftAtPlayer(EntityPlayer player)
    {
        EntityRealityRift rift = new EntityRealityRift(player.getEntityWorld());

        double xP = (player.getRNG().nextDouble() - 0.5) * 100.0;
        double yP = (player.getRNG().nextDouble() - 0.5) * 100.0;
        double zP = (player.getRNG().nextDouble() - 0.5) * 100.0;

        rift.setPosition(player.posX + xP, player.posY + yP, player.posZ + zP);
        player.getEntityWorld().spawnEntityInWorld(rift);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            Minecraft mc = Minecraft.getMinecraft();

            if (mc != null && !mc.isGamePaused())
            {
                DrugProperties drugProperties = DrugProperties.getDrugProperties(mc.renderViewEntity);

                if (drugProperties != null)
                {
                    SmoothCameraHelper.instance.update(mc.gameSettings.mouseSensitivity, DrugEffectInterpreter.getSmoothVision(drugProperties));
                }
            }
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent event)
    {
        if (event instanceof ConfigChangedEvent.OnConfigChangedEvent && event.modID.equals(Psychedelicraft.MODID))
        {
            PSConfig.loadConfig(event.configID);

            if (Psychedelicraft.config.hasChanged())
                Psychedelicraft.config.save();
        }
    }

    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event)
    {
        if (event.craftMatrix instanceof InventoryCrafting)
            RecipeActionRegistry.finalizeCrafting(event.crafting, (InventoryCrafting) event.craftMatrix, event.player);
    }
}
