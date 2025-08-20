package com.cucumbery.cucumbery.client.no_silk_touch_no_break;

import com.cucumbery.cucumbery.client.CucumberyClient;
import com.cucumbery.cucumbery.client.CucumberyConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameMode;
import org.lwjgl.glfw.GLFW;

public class NoSilkTouchNoBreak
{
	private KeyBinding keyBinding;

	private CucumberyConfig config;

	public void onInitializeClient()
	{
		config = CucumberyClient.getInstance().getConfig();

		registerKeyBinding();
		registerEvent();
	}

	private void registerKeyBinding()
	{
		keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.no_silk_touch_no_break.toggle", // Keybinding translation
				InputUtil.Type.KEYSYM, // Input type
				GLFW.GLFW_KEY_K, // Keycode
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
				config.setNoSilkTouchNoBreakEnabled(!config.isNoSilkTouchNoBreakEnabled());
				client.player.sendMessage(Text.translatable("message.no_silk_touch_no_break." + (config.isNoSilkTouchNoBreakEnabled() ? "enabled" : "disabled")), true);
			}
		});
	}

	private void registerEvent()
	{
		AttackBlockCallback.EVENT.register((playerEntity, world, hand, blockPos, direction) ->
		{
			if (!world.isClient)
				return ActionResult.PASS;
			if (!config.isNoSilkTouchNoBreakEnabled())
				return ActionResult.PASS;

			BlockEntity blockEntity = world.getBlockEntity(blockPos);

			if (playerEntity.getGameMode() != GameMode.CREATIVE && blockEntity != null && blockEntity.getType() == BlockEntityType.ENDER_CHEST)
			{
				ItemStack heldItem = playerEntity.getStackInHand(hand);
				RegistryEntry<Enchantment> silkTouch = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);

				if (heldItem.getEnchantments().getLevel(silkTouch) <= 0)
				{
					playerEntity.sendMessage(Text.translatable("message.no_silk_touch_no_break.prevented", KeyBindingHelper.getBoundKeyOf(keyBinding).getLocalizedText()),
							true);
					return ActionResult.FAIL;
				}
			}
			return ActionResult.PASS;
		});
	}
}
