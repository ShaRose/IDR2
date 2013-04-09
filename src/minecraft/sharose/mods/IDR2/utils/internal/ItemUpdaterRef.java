package sharose.mods.IDR2.utils.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.minecraft.item.Item;

public class ItemUpdaterRef implements Updater {

	private static Field itemIdField;
	
	static
	{
		int pubfinalmod = Modifier.FINAL + Modifier.PUBLIC;
		try
		{
			for (Field field : Item.class.getFields())
			{
				if ((field.getModifiers() == pubfinalmod) && (field.getType() == int.class))
				{
					itemIdField = field;
					break;
				}
			}
		} catch (Throwable e)
		{
			throw new RuntimeException("ID Resolver - Unable to set up reflection for Block!");
		}
	}
	
	@Override
	public void swapData(int oldID, int newID) {
		try {
			Item olditem = Item.itemsList[newID];
			Item.itemsList[newID] = Item.itemsList[oldID];
			if (olditem != null) {
				itemIdField.set(olditem, oldID);
			}
			if (Item.itemsList[newID] != null) {
				itemIdField.set(Item.itemsList[newID], newID);
			}
			Item.itemsList[oldID] = olditem;
		} catch (Throwable e) {
			throw new IllegalArgumentException("Unable to override itemID!", e);
		}
	}

	@Override
	public boolean isInitialized() {
		return true;
	}

}
