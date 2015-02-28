package com.timthebrick.tinystorage.item.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.timthebrick.tinystorage.block.BlockWoolChest;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBlockWoolChestSmall extends ItemBlock {

	public ItemBlockWoolChestSmall(Block block) {
		super(block);
		this.setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int meta) {
		return meta;
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean flag) {
		list.add(StatCollector.translateToLocal("tooltip.tinystorage:woolChestPrefix.small"));
		if (Block.getBlockFromItem(itemStack.getItem()) != Blocks.air && Block.getBlockFromItem(itemStack.getItem()) instanceof BlockWoolChest) {
			BlockWoolChest block = (BlockWoolChest) Block.getBlockFromItem(itemStack.getItem());
			if (block.getIsLockable()) {
				list.add(StatCollector.translateToLocal("tooltip.tinystorage:woolChestPrefix.locked"));
			}
		}
	}

}
