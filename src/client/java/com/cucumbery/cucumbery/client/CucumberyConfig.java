package com.cucumbery.cucumbery.client;

import com.cucumbery.cucumbery.Cucumbery;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CucumberyConfig
{
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private boolean noStripEnabled = true;

	private boolean noMiningCooldownEnabled = false;

	private boolean noSilkTouchNoBreakEnabled = true;

	public static CucumberyConfig loadConfig(File file)
	{
		CucumberyConfig config;

		if (file.exists() && file.isFile())
		{
			try (FileInputStream fileInputStream = new FileInputStream(file);
					InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
					BufferedReader bufferedReader = new BufferedReader(inputStreamReader);)
			{
				Cucumbery.LOGGER.info("Loading config!");
				config = GSON.fromJson(bufferedReader, CucumberyConfig.class);
			}
			catch (IOException e)
			{
				throw new RuntimeException("Failed to load config", e);
			}
		}
		else
		{
			config = new CucumberyConfig();
		}

		config.saveConfig();

		return config;
	}

	public void saveConfig()
	{
		try (FileOutputStream stream = new FileOutputStream(CucumberyClient.getInstance().getConfigFile());
				Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8))
		{
			Cucumbery.LOGGER.info("Saving config!");
			GSON.toJson(this, writer);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to save config", e);
		}
	}

	public boolean isNoStripEnabled()
	{
		return noStripEnabled;
	}

	public void setNoStripEnabled(boolean noStripEnabled)
	{
		this.noStripEnabled = noStripEnabled;
	}

	public boolean isNoMiningCooldownEnabled()
	{
		return noMiningCooldownEnabled;
	}

	public void setNoMiningCooldownEnabled(boolean noMiningCooldownEnabled)
	{
		this.noMiningCooldownEnabled = noMiningCooldownEnabled;
	}

	public boolean isNoSilkTouchNoBreakEnabled()
	{
		return noSilkTouchNoBreakEnabled;
	}

	public void setNoSilkTouchNoBreakEnabled(boolean noSilkTouchNoBreakEnabled)
	{
		this.noSilkTouchNoBreakEnabled = noSilkTouchNoBreakEnabled;
	}
}
