package com.cucumbery.cucumbery.client.custom_creative_tab;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class CustomCreativeTab
{
	public static void registerCommand()
	{
		ClientCommandRegistrationCallback.EVENT.register(
				(((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("custom-creative-tab")
						.executes(context ->
						{
							final PlayerEntity player = context.getSource().getPlayer();
							if (player == null)
								return 0;
							player.sendMessage(Text.literal("현재 %s개의 아이템 그룹이 있습니다".formatted(ModItemGroup.GROUPS.size())), false);
							for (String id : ModItemGroup.GROUPS.keySet())
							{
								player.sendMessage(Text.literal(id), false);
							}
							player.sendMessage(Text.literal("명령어 목록"), false);
							player.sendMessage(Text.literal("/view-item - 손에 들고 있는 아이템 컴포넌트 참조"), false);
							player.sendMessage(Text.literal("/custom-creative-tab <그룹 ID> <save|load> - 해당 그룹 아이디 저장/불러오기"), false);
							player.sendMessage(Text.literal("/custom-creative-tab <그룹 ID> <add|remove|insert> - 해당 그룹에 아이템 추가/삭제/삽입"), false);
							player.sendMessage(Text.literal("주의사항! 게임 플레이 도중 새로 추가된 아이템 그룹은 다음 게임 실행 시 적용됩니다").formatted(Formatting.RED), false);
							return 1;
						}).then(argument("group_id", string()).suggests((context, builder) ->
						{
							for (String id : ModItemGroup.GROUPS.keySet())
							{
								ModItemGroup modItemGroup = ModItemGroup.getById(id);
								if (modItemGroup != null)
								{
									ItemStack icon = modItemGroup.getIcon();
									builder.suggest(id, () -> icon.getItem().toString());
								}
							}
							return builder.buildFuture();
						}).then(argument("argument", string()).suggests((context, builder) ->
						{
							for (String s : new String[] {
									"save",
									"load",
									"add",
									"remove",
									"insert"
							})
							{
								builder.suggest(s);
							}
							return builder.buildFuture();
						}).executes(context ->
						{
							final PlayerEntity player = context.getSource().getPlayer();
							if (player == null)
								return 0;
							String groupID = getString(context, "group_id");
							String argument = getString(context, "argument");
							ModItemGroup modItemGroup = ModItemGroup.register(new ItemStack(Items.ACACIA_BOAT), groupID);
							switch (argument)
							{
								case "save" ->
								{
									int amount = modItemGroup.saveFile();
									player.sendMessage(Text.literal("%s 파일의 총 %s개 아이템 저장".formatted(groupID, amount)), false);
									return 1;
								}
								case "load" ->
								{
									int amount = ModItemGroup.loadFile(groupID);
									player.sendMessage(Text.literal("%s 파일의 총 %s개 아이템 불러옴".formatted(groupID, amount)), false);
									return 1;
								}
								case "add" ->
								{
									ItemStack itemStack = player.getMainHandStack();
									if (itemStack == null || itemStack.getItem().equals(Items.AIR))
									{
										player.sendMessage(Text.literal("no items!").formatted(Formatting.RED), false);
										return 0;
									}
									if (modItemGroup.add(itemStack))
									{
										player.sendMessage(Text.literal("%s 파일에 %s번째 아이템을 추가했습니다".formatted(groupID, modItemGroup.getItemAmount())), false);
										return 1;
									}
									player.sendMessage(Text.literal("%s 파일에 이미 손에 들고 있는 아이템이 존재하여 저장할 수 없습니다".formatted(groupID)), false);
									return 0;
								}
								case "remove" ->
								{
									if (modItemGroup.remove())
									{
										player.sendMessage(Text.literal("%s 파일의 %s번째 아이템을 제거했습니다".formatted(groupID, modItemGroup.getItemAmount() + 1)), false);
										return 1;
									}
									player.sendMessage(Text.literal("%s 파일에는 저장된 아이템이 없어 제거할 수 없습니다".formatted(groupID)), false);
									return 0;
								}
								case "insert" ->
								{

								}
							}
							return 1;
						})))))));

		ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("view-item").executes(context ->
		{
			final PlayerEntity player = context.getSource().getPlayer();
			if (player != null)
			{
				ItemStack itemStack = player.getMainHandStack();
				if (itemStack == null || itemStack.getItem().equals(Items.AIR))
				{
					player.sendMessage(Text.literal("no items!").formatted(Formatting.RED), false);
					return 0;
				}
				player.sendMessage(Text.literal("손에 들고 있는 아이템의 NBT는 다음과 같습니다: " + ModItemGroup.serialize(itemStack)), false);
				return 1;
			}
			return 0;
		}))));

/*		ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher.register(literal("save-item").executes(context ->
		{
			final PlayerEntity player = context.getSource().getPlayer();
			if (player != null)
			{
				ItemStack itemStack = player.getMainHandStack();
				if (itemStack == null || itemStack.getItem().equals(Items.AIR))
				{
					player.sendMessage(Text.literal("no items!").formatted(Formatting.RED));
					return 0;
				}
				if (addItem(itemStack))
				{
					player.sendMessage(Text.literal("손에 들고 있는 아이템을 추가했습니다! 개수: " + ITEM_STACKS.size()));
				}
				else
				{
					player.sendMessage(Text.literal("해당 아이템과 완전히 일치하는 아이템이 이미 등록되어 있어 추가할 수 없습니다"));
				}
				return 1;
			}
			return 0;
		}))));
		ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher.register(literal("save-item-empty").executes(context ->
		{
			final PlayerEntity player = context.getSource().getPlayer();
			if (player != null)
			{
				ItemStack itemStack = getEmpty();
				addItem(itemStack);
				player.sendMessage(Text.literal("공백 아이템을 추가하여 이제 " + ITEM_STACKS.size() + "개의 아이템이 있습니다"));
				return 1;
			}
			return 0;
		}))));
		ClientCommandRegistrationCallback.EVENT.register(
				((dispatcher, registryAccess) -> dispatcher.register(literal("save-item-at").then(argument("index", integer()).executes(context ->
				{
					int index = getInteger(context, "index");
					if (index < 1)
					{
						context.getSource().sendFeedback(Text.literal("숫자는 0보다 커야 합니다").formatted(Formatting.RED));
						return 0;
					}
					if (ITEM_STACKS.size() < index)
					{
						context.getSource().sendFeedback(Text.literal("해당 인덱스에는 아이템이 없습니다!").formatted(Formatting.RED));
						return 0;
					}
					final PlayerEntity player = context.getSource().getPlayer();
					if (player != null)
					{
						ItemStack itemStack = player.getMainHandStack();
						if (itemStack == null || itemStack.getItem().equals(Items.AIR))
						{
							player.sendMessage(Text.literal("no items!").formatted(Formatting.RED));
							return 0;
						}
						if (setItem(itemStack, index - 1))
						{
							context.getSource().sendFeedback(Text.literal("아이템 " + itemStack.getItem() + "을(를) " + index + "번 아이템으로 교체했습니다"));
							return 1;
						}
						else
						{
							player.sendMessage(Text.literal("해당 인덱스에 아이템이 없거나 겹치는 아이템이 있습니다!").formatted(Formatting.RED));
						}
					}
					return 0;
				})))));
		ClientCommandRegistrationCallback.EVENT.register(
				((dispatcher, registryAccess) -> dispatcher.register(literal("save-item-insert").then(argument("index", integer()).executes(context ->
				{
					int index = getInteger(context, "index");
					if (index < 1)
					{
						context.getSource().sendFeedback(Text.literal("숫자는 0보다 커야 합니다").formatted(Formatting.RED));
						return 0;
					}
					if (ITEM_STACKS.size() < index)
					{
						context.getSource().sendFeedback(Text.literal("해당 인덱스에는 아이템이 없습니다!").formatted(Formatting.RED));
						return 0;
					}
					final PlayerEntity player = context.getSource().getPlayer();
					if (player != null)
					{
						ItemStack itemStack = player.getMainHandStack();
						if (itemStack == null || itemStack.getItem().equals(Items.AIR))
						{
							player.sendMessage(Text.literal("no items!").formatted(Formatting.RED));
							return 0;
						}
						if (insertItem(itemStack, index - 1))
						{
							context.getSource().sendFeedback(Text.literal("아이템 " + itemStack.getItem() + "을(를) " + index + "번 아이템으로 들여넣었습니다"));
							return 1;
						}
						else
						{
							player.sendMessage(Text.literal("해당 인덱스에 아이템이 없거나 겹치는 아이템이 있습니다!").formatted(Formatting.RED));
						}
					}
					return 0;
				})))));
		ClientCommandRegistrationCallback.EVENT.register(
				((dispatcher, registryAccess) -> dispatcher.register(literal("save-item-at-empty").then(argument("index", integer()).executes(context ->
				{
					int index = getInteger(context, "index");
					if (index < 1)
					{
						context.getSource().sendFeedback(Text.literal("숫자는 0보다 커야 합니다").formatted(Formatting.RED));
						return 0;
					}
					if (ITEM_STACKS.size() < index)
					{
						context.getSource().sendFeedback(Text.literal("해당 인덱스에는 아이템이 없습니다!").formatted(Formatting.RED));
						return 0;
					}
					final PlayerEntity player = context.getSource().getPlayer();
					if (player != null)
					{
						ItemStack itemStack = getEmpty();
						if (setItem(itemStack, index - 1))
						{
							context.getSource().sendFeedback(Text.literal(index + "번 아이템을 공백으로 교체했습니다"));
							return 1;
						}
						else
						{
							player.sendMessage(Text.literal("해당 인덱스에 아이템이 없거나 겹치는 아이템이 있습니다!").formatted(Formatting.RED));
						}
					}
					return 0;
				})))));
		ClientCommandRegistrationCallback.EVENT.register(
				((dispatcher, registryAccess) -> dispatcher.register(literal("save-item-insert-empty").then(argument("index", integer()).executes(context ->
				{
					int index = getInteger(context, "index");
					if (index < 1)
					{
						context.getSource().sendFeedback(Text.literal("숫자는 0보다 커야 합니다").formatted(Formatting.RED));
						return 0;
					}
					if (ITEM_STACKS.size() < index)
					{
						context.getSource().sendFeedback(Text.literal("해당 인덱스에는 아이템이 없습니다!").formatted(Formatting.RED));
						return 0;
					}
					final PlayerEntity player = context.getSource().getPlayer();
					if (player != null)
					{
						ItemStack itemStack = getEmpty();
						if (insertItem(itemStack, index - 1))
						{
							context.getSource().sendFeedback(Text.literal(index + "번 아이템에 공백을 들여넣었습니다"));
							return 1;
						}
						else
						{
							player.sendMessage(Text.literal("해당 인덱스에 아이템이 없거나 겹치는 아이템이 있습니다!").formatted(Formatting.RED));
						}
					}
					return 0;
				})))));

		ClientCommandRegistrationCallback.EVENT.register(
				((dispatcher, registryAccess) -> dispatcher.register(literal("view-item-without-display").executes(context ->
				{
					final PlayerEntity player = context.getSource().getPlayer();
					if (player != null)
					{
						ItemStack itemStack = player.getMainHandStack();
						if (itemStack == null || itemStack.getItem().equals(Items.AIR))
						{
							player.sendMessage(Text.literal("no items!").formatted(Formatting.RED));
							return 0;
						}
						itemStack.remove(DataComponentTypes.LORE);
						itemStack.remove(DataComponentTypes.CUSTOM_NAME);
						player.sendMessage(Text.literal("손에 들고 있는 아이템의 NBT는 다음과 같습니다: " + itemStack.encode(WrapperLookup.of(Stream.empty())).asString()));
						return 1;
					}
					return 0;
				}))));
		ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher.register(literal("test-cmd").executes(context ->
		{
			final PlayerEntity player = context.getSource().getPlayer();
			if (player != null)
			{
				NbtCompound nbtCompound = NbtHelper.fromNbtProviderString(
						"{id:\"minecraft:tnt\",Count:3,components:{\"minecraft:enchantments\":{levels:{\"minecraft:sharpness\":3}}}}");
				ItemStack itemStack = ItemStack.fromNbtOrEmpty(WrapperLookup.of(Stream.empty()), nbtCompound);
				NbtCompound compound = new NbtCompound();
				compound.putString("id", "minecraft:" + itemStack.getItem());
				compound.putByte("count", (byte) itemStack.getCount());
				compound.put("components", itemStack.encode(WrapperLookup.of(Stream.empty())));
				player.sendMessage(Text.literal(compound.toString()));
				player.giveItemStack(itemStack);
			}
			return 0;
		}))));
		ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher.register(literal("save-items").executes(context ->
		{
			int amount = saveFiles();
			context.getSource().sendFeedback(Text.literal("메모리에 있는 아이템 값을 파일에 덮어씌웠습니다 (저장한 아이템 : " + amount + "개)"));
			return 1;
		}))));
		ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher.register(literal("load-items").executes(context ->
		{
			int amount = loadFiles();
			context.getSource().sendFeedback(Text.literal("파일에 있는 아이템 값을 로드했습니다 (로드된 아이템 : " + amount + "개)"));
			return 1;
		}))));
		ClientCommandRegistrationCallback.EVENT.register(
				((dispatcher, registryAccess) -> dispatcher.register(literal("remove-item").then(argument("index", integer()).executes(context ->
				{
					int index = getInteger(context, "index");
					if (index < 1)
					{
						context.getSource().sendFeedback(Text.literal("숫자는 0보다 커야 합니다").formatted(Formatting.RED));
						return 0;
					}
					if (ITEM_STACKS.size() < index)
					{
						context.getSource().sendFeedback(Text.literal("해당 인덱스에는 아이템이 없습니다!").formatted(Formatting.RED));
						return 0;
					}
					ItemStack itemStack = ITEM_STACKS.get(index - 1);
					ITEM_STACKS.remove(index - 1);
					context.getSource().sendFeedback(Text.literal("아이템 " + itemStack.getItem() + "을(를) 제거하여 이제 " + ITEM_STACKS.size() + "개 남았습니다"));
					saveFiles();
					loadFiles();
					return 1;
				})).executes(context ->
				{
					if (ITEM_STACKS.isEmpty())
					{
						context.getSource().sendFeedback(Text.literal("저장된 아이템이 하나도 없습니다!").formatted(Formatting.RED));
						return 0;
					}
					int size = ITEM_STACKS.size();
					ItemStack itemStack = ITEM_STACKS.get(size - 1);
					ITEM_STACKS.remove(size - 1);
					ITEM_GROUP.getDisplayStacks().remove(itemStack);
					context.getSource().sendFeedback(Text.literal("아이템 " + itemStack.getItem() + "을(를) 제거하여 이제 " + ITEM_STACKS.size() + "개 남았습니다"));
					saveFiles();
					loadFiles();
					return 1;
				}))));*/
	}

	public void onInitializeClient() {
		ModItemGroup.loadFiles();
		CustomCreativeTab.registerCommand();
	}
}
