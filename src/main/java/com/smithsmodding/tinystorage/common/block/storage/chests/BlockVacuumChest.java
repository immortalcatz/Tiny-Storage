package com.smithsmodding.tinystorage.common.block.storage.chests;

import java.util.List;
import java.util.Random;

import com.smithsmodding.tinystorage.TinyStorage;
import com.smithsmodding.tinystorage.common.block.BlockContainerTinyStorage;
import com.smithsmodding.tinystorage.common.reference.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.smithsmodding.tinystorage.client.gui.widgets.settings.AccessMode;
import com.smithsmodding.tinystorage.common.core.TinyStorageLog;
import com.smithsmodding.tinystorage.common.creativetab.TabTinyStorage;
import com.smithsmodding.tinystorage.common.tileentity.TileEntityTinyStorage;
import com.smithsmodding.tinystorage.common.tileentity.implementations.TileEntityVacuumChest;
import com.smithsmodding.tinystorage.common.tileentity.implementations.sub.TileEntityVacuumChestLarge;
import com.smithsmodding.tinystorage.common.tileentity.implementations.sub.TileEntityVacuumChestMedium;
import com.smithsmodding.tinystorage.common.tileentity.implementations.sub.TileEntityVacuumChestSmall;
import com.smithsmodding.tinystorage.util.common.InventoryHelper;
import com.smithsmodding.tinystorage.util.common.PlayerHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockVacuumChest extends BlockContainerTinyStorage implements ITileEntityProvider {

	protected String textureName;
	private boolean isLockable;

	public BlockVacuumChest(Material mat, String textureName, boolean isLockable) {
		super(mat);
		this.setHardness(2.5f);
		this.isLockable = isLockable;
		this.textureName = textureName;
		if (!this.isLockable) {
			this.setBlockName(Names.UnlocalisedBlocks.VACUUM_CHEST + this.textureName);
		} else {
			this.setBlockName(Names.UnlocalisedBlocks.VACUUM_CHEST_LOCKED + this.textureName);
		}
		this.setCreativeTab(TabTinyStorage.creativeTab);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metaData) {
		if (metaData == 0) {
			return new TileEntityVacuumChestSmall();
		} else if (metaData == 1) {
			return new TileEntityVacuumChestMedium();
		} else if (metaData == 2) {
			return new TileEntityVacuumChestLarge();
		}
		return null;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		setBlockBoundsBasedOnState(world, x, y, z);
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
		setBlockBoundsBasedOnState(world, x, y, z);
		return super.getSelectedBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		updateChestBounds(world.getBlockMetadata(x, y, z));
	}

	private void updateChestBounds(int meta) {
		float f = 0.125F;
		if (meta == 0) {
			setBlockBounds(0.2f, 0.0f, 0.2f, 0.8f, 0.60f, 0.8f);
		}
		if (meta == 1) {
			setBlockBounds(0.125f, 0.0f, 0.125f, 1F - 0.125f, 0.72f, 1F - 0.125f);
		}
		if (meta == 2) {
			setBlockBounds(0.0625f, 0.0f, 0.0625f, 0.9375f, 0.875f, 0.9375f);
		}
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public int getRenderType() {
		return RenderIDs.vacuumChest;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float subX, float subY, float subZ) {
		if ((player.isSneaking() && player.getCurrentEquippedItem() != null) || world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN)) {
			return true;
		}
		if (world.isRemote) {
			return true;
		} else {
			if (!world.isRemote && world.getTileEntity(x, y, z) instanceof TileEntityVacuumChest) {
				TileEntityVacuumChest tileEntity = (TileEntityVacuumChest) world.getTileEntity(x, y, z);
				if (tileEntity.hasUniqueOwner()) {
					if (tileEntity.getUniqueOwner().equals(player.getGameProfile().getId().toString() + player.getDisplayName())|| tileEntity.isFriend(player)) {
						player.openGui(TinyStorage.instance, GUIs.VACUUM_CHEST.ordinal(), world, x, y, z);
					} else {
						PlayerHelper.sendChatMessage(player, new ChatComponentTranslation(Messages.Chat.CHEST_NOT_OWNED));
					}
				} else {
					player.openGui(TinyStorage.instance, GUIs.VACUUM_CHEST.ordinal(), world, x, y, z);
				}
			}
			return true;
		}
	}

	@Override
	public boolean onBlockEventReceived(World world, int x, int y, int z, int eventId, int eventData) {
		super.onBlockEventReceived(world, x, y, z, eventId, eventData);
		TileEntity tileentity = world.getTileEntity(x, y, z);
		return tileentity != null && tileentity.receiveClientEvent(eventId, eventData);
	}

	@Override
	public float getPlayerRelativeBlockHardness(EntityPlayer player, World world, int x, int y, int z) {
		if (world.getTileEntity(x, y, z) instanceof TileEntityVacuumChest) {
			TileEntityVacuumChest tileEntity = (TileEntityVacuumChest) world.getTileEntity(x, y, z);
			if (tileEntity.hasUniqueOwner()) {
				if (tileEntity.getUniqueOwner().equals(player.getGameProfile().getId().toString() + player.getDisplayName())) {
					return super.getPlayerRelativeBlockHardness(player, world, x, y, z);
				} else {
					return -1F;
				}
			} else {
				return super.getPlayerRelativeBlockHardness(player, world, x, y, z);
			}
		} else {
			return super.getPlayerRelativeBlockHardness(player, world, x, y, z);
		}
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		super.onEntityCollidedWithBlock(world, x, y, z, entity);
		if (!world.isRemote) {
			if (entity instanceof EntityItem) {
				if (world.getTileEntity(x, y, z) instanceof TileEntityVacuumChest) {
					TileEntityVacuumChest tileEntity = (TileEntityVacuumChest) world.getTileEntity(x, y, z);
					ItemStack itemStack = ((EntityItem) entity).getEntityItem();
					ItemStack itemstack1 = InventoryHelper.invInsert(tileEntity, itemStack, 2);
					if ((itemstack1 != null) && (itemstack1.stackSize != 0)) {
						((EntityItem) entity).setEntityItemStack(itemstack1);
					} else {
						((EntityItem) entity).setDead();
					}
				}
			}
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		dropInventory(world, x, y, z);
		super.breakBlock(world, x, y, z, block, meta);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLiving, ItemStack itemStack) {
		if (world.getTileEntity(x, y, z) instanceof TileEntityTinyStorage) {
			int direction = 0;
			int facing = MathHelper.floor_double(entityLiving.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

			if (facing == 0) {
				direction = ForgeDirection.NORTH.ordinal();
			} else if (facing == 1) {
				direction = ForgeDirection.EAST.ordinal();
			} else if (facing == 2) {
				direction = ForgeDirection.SOUTH.ordinal();
			} else if (facing == 3) {
				direction = ForgeDirection.WEST.ordinal();
			}

			if (itemStack.hasDisplayName()) {
				((TileEntityTinyStorage) world.getTileEntity(x, y, z)).setCustomName(itemStack.getDisplayName());
			}

			((TileEntityTinyStorage) world.getTileEntity(x, y, z)).setOrientation(direction);

			if (this.isLockable) {
				if (entityLiving instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) entityLiving;
					if (!PlayerHelper.isPlayerFake(player)) {
						((TileEntityTinyStorage) world.getTileEntity(x, y, z)).setUniqueOwner(entityLiving.getUniqueID().toString() + player.getDisplayName());
						((TileEntityTinyStorage) world.getTileEntity(x, y, z)).setOwner(player.getDisplayName());
						((TileEntityTinyStorage) world.getTileEntity(x, y, z)).setAccessMode(AccessMode.DISABLED);
					} else {
						TinyStorageLog.error("Something (not a player) just tried to place a locked chest!" + " | " + entityLiving.toString());
						world.removeTileEntity(x, y, z);
						world.setBlockToAir(x, y, z);
						if (itemStack != null && itemStack.stackSize > 0) {
							Random rand = new Random();

							float dX = rand.nextFloat() * 0.8F + 0.1F;
							float dY = rand.nextFloat() * 0.8F + 0.1F;
							float dZ = rand.nextFloat() * 0.8F + 0.1F;

							EntityItem entityItem = new EntityItem(world, x + dX, y + dY, z + dZ, itemStack.copy());

							if (itemStack.hasTagCompound()) {
								entityItem.getEntityItem().setTagCompound((NBTTagCompound) itemStack.getTagCompound().copy());
							}

							float factor = 0.05F;
							entityItem.motionX = rand.nextGaussian() * factor;
							entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
							entityItem.motionZ = rand.nextGaussian() * factor;
							world.spawnEntityInWorld(entityItem);
							itemStack.stackSize = 0;
						}
					}
				}
			}
		}
	}

	private void dropInventory(World world, int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);

		if (!(tileEntity instanceof IInventory)) {
			return;
		}

		IInventory inventory = (IInventory) tileEntity;
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack itemStack = inventory.getStackInSlot(i);

			if (itemStack != null && itemStack.stackSize > 0) {
				Random rand = new Random();

				float dX = rand.nextFloat() * 0.8F + 0.1F;
				float dY = rand.nextFloat() * 0.8F + 0.1F;
				float dZ = rand.nextFloat() * 0.8F + 0.1F;

				EntityItem entityItem = new EntityItem(world, x + dX, y + dY, z + dZ, itemStack.copy());

				if (itemStack.hasTagCompound()) {
					entityItem.getEntityItem().setTagCompound((NBTTagCompound) itemStack.getTagCompound().copy());
				}

				float factor = 0.05F;
				entityItem.motionX = rand.nextGaussian() * factor;
				entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
				entityItem.motionZ = rand.nextGaussian() * factor;
				world.spawnEntityInWorld(entityItem);
				itemStack.stackSize = 0;
			}
		}
	}

	@Override
	public void registerBlockIcons(IIconRegister iconRegister) {
		blockIcon = iconRegister.registerIcon(References.MOD_ID.toLowerCase() + ":" + textureName);
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs creativeTabs, List list) {
		for (int meta = 0; meta < 3; meta++) {
			//noinspection unchecked
			list.add(new ItemStack(item, 1, meta));
		}
	}

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random random) {
		super.randomDisplayTick(world, x, y, z, random);
		if ((random.nextInt(2) == 0)) {
			world.spawnParticle("portal", x + random.nextFloat(), y + 1.1F, z + random.nextFloat(), 0.0D, 0.05D, 0.0D);
		}
	}

	@Override
	public int damageDropped(int metaData) {
		return metaData;
	}

	@Override
	public String getTextureName() {
		return textureName;
	}

	public boolean getIsLockable() {
		return isLockable;
	}
}