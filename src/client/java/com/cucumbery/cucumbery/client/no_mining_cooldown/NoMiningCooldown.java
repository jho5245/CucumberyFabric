package com.cucumbery.cucumbery.client.no_mining_cooldown;

import com.cucumbery.cucumbery.client.CucumberyClient;
import com.cucumbery.cucumbery.client.CucumberyConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class NoMiningCooldown
{
	private CucumberyConfig config;

	private KeyBinding keyBinding;

	public void onInitializeClient()
	{
		config = CucumberyClient.getInstance().getConfig();

		registerKeyBinding();
	}

	private void registerKeyBinding()
	{
		keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.no_mining_cooldown.toggle", // Keybinding translation
				InputUtil.Type.KEYSYM, // Input type
				GLFW.GLFW_KEY_H, // Keycode
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

				config.setNoMiningCooldownEnabled(!config.isNoMiningCooldownEnabled());
				client.player.sendMessage(Text.translatable("message.no_mining_cooldown." + (config.isNoMiningCooldownEnabled() ? "enabled" : "disabled")), true);
			}
		});
	}
}
