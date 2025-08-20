package com.cucumbery.cucumbery.client.no_strip;

import com.cucumbery.cucumbery.client.CucumberyClient;
import com.cucumbery.cucumbery.client.CucumberyConfig;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Oxidizable;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public class NoStrip
{
	private CucumberyConfig config;

	private KeyBinding keyBinding;

	private Map<Block, Block> strippedBlocks;

	private Map<Block, BlockState> pathStates;

	public void onInitializeClient()
	{
		config = CucumberyClient.getInstance().getConfig();

		initFields();
		registerKeyBinding();
		registerEvent();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void initFields()
	{
		strippedBlocks = (new ImmutableMap.Builder()).put(Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_WOOD).put(Blocks.OAK_LOG, Blocks.STRIPPED_OAK_LOG)
				.put(Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD).put(Blocks.DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_LOG)
				.put(Blocks.PALE_OAK_WOOD, Blocks.STRIPPED_PALE_OAK_WOOD).put(Blocks.PALE_OAK_LOG, Blocks.STRIPPED_PALE_OAK_LOG)
				.put(Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_WOOD).put(Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG)
				.put(Blocks.CHERRY_WOOD, Blocks.STRIPPED_CHERRY_WOOD).put(Blocks.CHERRY_LOG, Blocks.STRIPPED_CHERRY_LOG)
				.put(Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_WOOD).put(Blocks.BIRCH_LOG, Blocks.STRIPPED_BIRCH_LOG)
				.put(Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_WOOD).put(Blocks.JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_LOG)
				.put(Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_WOOD).put(Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG)
				.put(Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM).put(Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE)
				.put(Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM).put(Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_HYPHAE)
				.put(Blocks.MANGROVE_WOOD, Blocks.STRIPPED_MANGROVE_WOOD).put(Blocks.MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_LOG)
				.put(Blocks.BAMBOO_BLOCK, Blocks.STRIPPED_BAMBOO_BLOCK).build();
		pathStates = Maps.newHashMap(
				(new ImmutableMap.Builder()).put(Blocks.GRASS_BLOCK, Blocks.DIRT_PATH.getDefaultState()).put(Blocks.DIRT, Blocks.DIRT_PATH.getDefaultState())
						.put(Blocks.PODZOL, Blocks.DIRT_PATH.getDefaultState()).put(Blocks.COARSE_DIRT, Blocks.DIRT_PATH.getDefaultState())
						.put(Blocks.MYCELIUM, Blocks.DIRT_PATH.getDefaultState()).put(Blocks.ROOTED_DIRT, Blocks.DIRT_PATH.getDefaultState()).build());
	}

	private void registerKeyBinding()
	{
		keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.no_strip.toggle", // KeyBinding translation
				InputUtil.Type.KEYSYM, // Input Type
				GLFW.GLFW_KEY_J, // Keycode
				"category.cucumbery.title" // Category translation
		));

		ClientTickEvents.END_CLIENT_TICK.register(client ->
		{
			while (keyBinding.wasPressed())
			{
				if (client.player == null)
				{
					continue;
				}
				config.setNoStripEnabled(!config.isNoStripEnabled());
				client.player.sendMessage(Text.translatable("message.no_strip." + (config.isNoStripEnabled() ? "enabled" : "disabled")), true);
			}
		});
	}

	private void registerEvent()
	{
		UseBlockCallback.EVENT.register(((playerEntity, world, hand, blockHitResult) ->
		{
			if (!world.isClient)
				return ActionResult.PASS;
			if (!config.isNoStripEnabled())
				return ActionResult.PASS;

			ItemStack stack = playerEntity.getStackInHand(hand);
			BlockPos blockPos = blockHitResult.getBlockPos();
			BlockState blockState = world.getBlockState(blockPos);

			if (stack.getComponents().contains(DataComponentTypes.TOOL))
			{
				if (strippedBlocks.containsKey(blockState.getBlock()) || Oxidizable.OXIDATION_LEVEL_DECREASES.get().containsKey(blockState.getBlock())
						|| HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get().containsKey(blockState.getBlock()))
				{
					informPlayer(playerEntity);
					return ActionResult.FAIL;
				}
			}

			if (stack.getItem() instanceof ShovelItem && pathStates.containsKey(blockState.getBlock()))
			{
				informPlayer(playerEntity);
				return ActionResult.FAIL;
			}

			return ActionResult.PASS;
		}));
	}

	private void informPlayer(PlayerEntity player)
	{
		player.sendMessage(Text.translatable("message.no_strip.prevented", KeyBindingHelper.getBoundKeyOf(keyBinding).getLocalizedText()), true);
	}
}
