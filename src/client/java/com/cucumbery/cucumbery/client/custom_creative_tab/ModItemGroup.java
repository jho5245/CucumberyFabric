package com.cucumbery.cucumbery.client.custom_creative_tab;

import com.cucumbery.cucumbery.Cucumbery;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;

public class ModItemGroup
{
	public static final HashMap<String, ModItemGroup> GROUPS = new HashMap<>();

	public static final String DATA_FOLER_NAME = "CustomCreativeTab_Items";

	private final ItemStack icon;

	private final String id;

	private final List<ItemStack> itemStacks = new ArrayList<>();

	private final ItemGroup itemGroup;

	private final Identifier identifier;

	protected ModItemGroup(ItemStack icon, String id)
	{
		ItemGroup temp;
		this.icon = icon;
		this.id = id;
		identifier = Identifier.of(Cucumbery.MOD_ID, id);
		itemGroup = FabricItemGroup.builder().icon(() -> icon).displayName(
				Text.translatable("itemGroup." + Cucumbery.MOD_ID + "." + id)).build();
		GROUPS.put(id, this);
		try
		{
			Registry.register(Registries.ITEM_GROUP, identifier, itemGroup);
		}
		catch (IllegalStateException e)
		{
			saveFile();
			return;
		}
		ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, identifier)).register(content ->
		{
			int amount = 0;
			for (ItemStack itemStack : itemStacks)
			{
				content.add(itemStack);
				amount++;
				if (amount % 8 == 0)
				{
					ItemStack row = getRowDisplay(amount);
					content.add(row);
				}
			}
		});
	}

	@NotNull
	public ItemStack getIcon()
	{
		return icon;
	}

	@NotNull
	public String getId()
	{
		return id;
	}

	@Nullable
	public static ModItemGroup getById(@NotNull String id)
	{
		return GROUPS.get(id);
	}

	public boolean add(@NotNull ItemStack itemStack)
	{
		itemStack = itemStack.copy();
		itemStack.setCount(1);
		if (itemGroup.getDisplayStacks().contains(itemStack))
		{
			return false;
		}
		this.itemStacks.add(itemStack);
		saveFile();
		loadFile();
		return true;
	}

	public boolean remove()
	{
		return remove(this.itemStacks.size() - 1);
	}

	public boolean remove(int index)
	{
		if (getItemAmount() <= index)
		{
			return false;
		}
		this.itemStacks.remove(index);
		return true;
	}

	public ItemGroup getItemGroup()
	{
		return itemGroup;
	}

	public Identifier getIdentifier()
	{
		return identifier;
	}

	/**
	 * 파일을 저장합니다
	 *
	 * @return 저장한 아이템 개수
	 */
	public int saveFile()
	{
		int amount = 0;
		try
		{
			File file = new File(System.getProperty("user.dir") + "/" + DATA_FOLER_NAME + "/" + id + ".txt");
			if (!file.exists())
			{
				file.getParentFile().mkdirs();
				if (!file.createNewFile())
				{

				}
			}
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
			for (ItemStack itemStack : itemStacks)
			{
				String serial = serialize(itemStack);
				if (serial.startsWith("{components:{\"minecraft:custom_data\":{ForRow:1b"))
					continue;
				bufferedWriter.write((serial.startsWith("{components:{\"minecraft:custom_data\":{ForDisplay:1b") ? "empty" : serialize(itemStack)) + "\n");
				amount++;
			}
			bufferedWriter.close();
		}
		catch (Exception ignored)
		{

		}
		return amount;
	}

	/**
	 * 파일을 불러옵니다
	 *
	 * @return 불러온 아이템 개수
	 */
	public int loadFile()
	{
		int amount = 0;
		itemStacks.clear();
		itemGroup.getDisplayStacks().clear();
		File file = new File(System.getProperty("user.dir") + "/" + DATA_FOLER_NAME + "/" + id + ".txt");
		if (!file.exists())
		{
			return amount;
		}
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String str;
			while ((str = reader.readLine()) != null)
			{
				ItemStack itemStack = str.equals("empty") ? getEmpty() : deserialize(str);
				if (itemStack == null || itemStack.getItem() == null || itemStack.getItem().equals(Items.AIR))
				{
					continue;
				}
				itemStack.setCount(1);
				itemStacks.add(itemStack);
				itemGroup.getDisplayStacks().add(itemStack);
				amount++;
				if (amount % 8 == 0)
				{
					ItemStack row = getRowDisplay(amount);
					itemGroup.getDisplayStacks().add(row);
				}
			}
			// CustomCreativeTab.LOGGER.debug("loaded {} items", amount);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		return amount;
	}

	public int getItemAmount()
	{
		return itemStacks.size();
	}

	public static int loadFile(String fileName)
	{
		File file = new File(System.getProperty("user.dir") + "/" + DATA_FOLER_NAME + "/" + fileName + (fileName.endsWith(".txt") ? "" : ".txt"));
		if (!file.exists())
		{
			if (!file.mkdirs())
			{
				System.out.println("Could not create " + file.getAbsolutePath() + " file!");
				return -1;
			}
		}
		ItemStack item;
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String firstItemString = reader.readLine();
			item = deserialize(firstItemString);
			if (item == null)
				throw new Exception();
		}
		catch (Exception e)
		{
			item = new ItemStack(Items.BARRIER);
		}
		ModItemGroup modItemGroup = register(item, fileName);
		return modItemGroup.loadFile();
	}

	public static void saveFiles()
	{
		for (String id : GROUPS.keySet())
		{
			Objects.requireNonNull(getById(id)).saveFile();
		}
	}

	public static void loadFiles()
	{
		File folder = new File(System.getProperty("user.dir") + "/" + DATA_FOLER_NAME);
		if (!folder.exists())
		{
			if (!folder.mkdirs())
			{
				System.out.println("Could not create " + folder.getAbsolutePath() + " folder!");
				return;
			}
		}
		File[] files = folder.listFiles();
		if (files == null || files.length == 0)
		{
			ModItemGroup exampleGroup = register(new ItemStack(Items.ACACIA_BOAT), "example");
			exampleGroup.add(deserialize(
					"{components:{\"minecraft:item_model\":\"custom_creative_tab:example\",\"minecraft:item_name\":'샘플 아이템',\"minecraft:lore\":[{\"translate\":\"lore.custom_creative_tab.example_description\",\"italic\":false,\"color\":\"white\"},'',{\"translate\":\"lore.custom_creative_tab.example_description_2\",\"italic\":false,\"color\":\"white\"}]},count:1,id:\"minecraft:diamond\"}\n"));
			exampleGroup.saveFile();
			return;
		}
		for (File file : files)
		{
			if (!file.getName().endsWith(".txt"))
				continue;
			loadFile(file.getName().substring(0, file.getName().length() - 4));
		}
	}

	/**
	 * 새 아이템 그룹을 등록하거나 기존 그룹을 반환합니다.
	 *
	 * @param icon
	 * 		아이템 그룹 아이템
	 * @param id
	 * 		아이템 그룹 식별자
	 * @return 새 아이템 그룹 혹은 기존 그룹
	 */
	public static ModItemGroup register(@NotNull ItemStack icon, @NotNull String id)
	{
		if (GROUPS.containsKey(id))
		{
			return GROUPS.get(id);
		}
		return new ModItemGroup(icon, id);
	}

	public static ItemStack getEmpty()
	{
		return deserialize("{components:{\"minecraft:custom_data\":{ForDisplay:1b,uuid:\"" + UUID.randomUUID()
				+ "\"},\"minecraft:item_name\":'\"\"'},count:1,id:\"minecraft:light_gray_stained_glass_pane\"}\n");
	}

	public static ItemStack getRowDisplay(int row)
	{
		return deserialize(
				"{components:{\"minecraft:custom_data\":{ForRow:1b,uuid:\"" + UUID.randomUUID() + "\"},\"minecraft:item_name\":'\"" + (row - 7) + " ~ " + (row)
						+ "\"'},count:1,id:\"minecraft:black_stained_glass_pane\"}\n");
	}

	@NotNull
	public static String serialize(@NotNull ItemStack itemStack)
	{
		itemStack = itemStack.copy();
		if (itemStack.hasEnchantments())
		{
			itemStack.remove(DataComponentTypes.ENCHANTMENTS);
			System.out.println("인챈트 안됨");
		}
		return ItemStack.CODEC.encode(itemStack, NbtOps.INSTANCE, NbtOps.INSTANCE.empty()).result().orElseThrow().toString();
		////		System.out.println(ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, itemStack));
		//		return ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, itemStack).result().orElseThrow().toString();
	}

	@Nullable
	public static ItemStack deserialize(@NotNull String string)
	{
		try
		{
			return ItemStack.CODEC.parse(NbtOps.INSTANCE, NbtHelper.fromNbtProviderString(string)).getOrThrow();
			//			return ItemStack.fromNbtOrEmpty(WrapperLookup.of(Stream.empty()), NbtHelper.fromNbtProviderString(string));
		}
		catch (Exception ignored)
		{
			return null;
		}
	}
}
