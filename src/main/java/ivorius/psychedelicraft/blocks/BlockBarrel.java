/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.blocks;

import ivorius.psychedelicraft.fluids.FluidFermentable;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

public class BlockBarrel extends BlockContainer
{
    public BlockBarrel()
    {
        super(Material.wood);

        setStepSound(soundTypeWood);
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    public int getRenderType()
    {
        return -1;
    }

    @Override
    public IIcon getIcon(int par1, int par2)
    {
        return Blocks.planks.getIcon(0, 0);
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
    {
        if (willHarvest)
        {
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity instanceof TileEntityBarrel)
            {
                TileEntityBarrel tileEntityBarrel = (TileEntityBarrel) tileEntity;
                FluidStack fluidStack = tileEntityBarrel.drain(ForgeDirection.DOWN, TileEntityBarrel.BARREL_CAPACITY, true);
                ItemStack stack = new ItemStack(this, 1, ((TileEntityBarrel) tileEntity).barrelWoodType);

                if (fluidStack != null && fluidStack.amount > 0)
                    ((IFluidContainerItem) stack.getItem()).fill(stack, fluidStack, true);

                dropBlockAsItem(world, x, y, z, stack);
            }
        }

        return super.removedByPlayer(world, player, x, y, z, willHarvest);
    }

    @Override
    public void dropBlockAsItemWithChance(World p_149690_1_, int p_149690_2_, int p_149690_3_, int p_149690_4_, int p_149690_5_, float p_149690_6_, int p_149690_7_)
    {

    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float par7, float par8, float par9)
    {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityBarrel)
        {
            TileEntityBarrel tileEntityBarrel = (TileEntityBarrel) tileEntity;

            ItemStack heldItem = player.getHeldItem();

            if (heldItem != null && heldItem.getItem() instanceof IFluidContainerItem)
            {
                IFluidContainerItem fluidContainerItem = (IFluidContainerItem) heldItem.getItem();

                int maxFill = fluidContainerItem.fill(heldItem, tileEntityBarrel.drain(ForgeDirection.DOWN, 1, false), false);
                if (maxFill > 0)
                {
                    if (!world.isRemote)
                    {
                        FluidStack drained = tileEntityBarrel.drain(ForgeDirection.DOWN, maxFill, true);
                        fluidContainerItem.fill(heldItem, drained, true);
                    }
                }

                tileEntityBarrel.timeLeftTapOpen = 20;

                return true;
            }
        }

        return false;
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister)
    {

    }

    @Override
    public String getItemIconName()
    {
        return getTextureName();
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2)
    {
        return new TileEntityBarrel();
    }
}
