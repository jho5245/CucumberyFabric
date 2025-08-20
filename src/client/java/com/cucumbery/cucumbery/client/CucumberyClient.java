package com.cucumbery.cucumbery.client;

import com.cucumbery.cucumbery.client.custom_creative_tab.CustomCreativeTab;
import com.cucumbery.cucumbery.client.no_mining_cooldown.NoMiningCooldown;
import com.cucumbery.cucumbery.client.no_silk_touch_no_break.NoSilkTouchNoBreak;
import com.cucumbery.cucumbery.client.no_strip.NoStrip;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.io.File;

public class CucumberyClient implements ClientModInitializer
{
	private static CucumberyClient instance;

	private String configFilePath;

	private File configFile;

	private CucumberyConfig config;

	@Override
	public void onInitializeClient()
	{
		instance = this;
		configFilePath = FabricLoader.getInstance().getConfigDir() + "/cucumbery_config.json";
		configFile = new File(configFilePath);
		config = CucumberyConfig.loadConfig(configFile);

		registerEvent();

		new CustomCreativeTab().onInitializeClient();
		new NoMiningCooldown().onInitializeClient();
		new NoStrip().onInitializeClient();
		new NoSilkTouchNoBreak().onInitializeClient();

	}

	private void registerEvent()
	{
		ClientLifecycleEvents.CLIENT_STOPPING.register((MinecraftClient client) -> config.saveConfig());
	}

	public File getConfigFile()
	{
		return configFile;
	}

	public CucumberyConfig getConfig()
	{
		return config;
	}

	public static CucumberyClient getInstance()
	{
		return instance;
	}
}
