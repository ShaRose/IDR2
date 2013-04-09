package sharose.mods.IDR2.utils.internal;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import net.minecraft.block.Block;

public class BlockUpdaterRef implements Updater {
	public static Field blockIdField;
	public static Field[] blockArrayFields;
	
	static
	{
		int pubfinalmod = Modifier.FINAL + Modifier.PUBLIC;
		ArrayList<Field> blockFields = new ArrayList<Field>();
		try
		{
			for (Field field : Block.class.getFields())
			{
				if ((field.getModifiers() == pubfinalmod) && (field.getType() == int.class))
				{
					blockIdField = field;
					blockIdField.setAccessible(true);
					continue;
				}
				if(Modifier.isStatic(field.getModifiers()) && field.getType().toString().startsWith("class ["))
				{
					field.setAccessible(true);
					if(Array.getLength(field.get(null)) != Block.blocksList.length)
					{
						continue;
					}
					blockFields.add(field);
				}
			}
		} catch (Throwable e)
		{
			throw new RuntimeException("ID Resolver - Unable to set up reflection for Block!");
		}
		blockArrayFields = blockFields.toArray(new Field[0]);
		if(blockFields == null || blockArrayFields.length == 0)
		{
			throw new RuntimeException("ID Resolver - Unable to set up reflection for Block!");
		}
	}

	@Override
	public void swapData(int oldID, int newID) {
		try {
			Block oldblock = Block.blocksList[newID];
			Block.blocksList[newID] = Block.blocksList[oldID];
			if (oldblock != null) {
				blockIdField.set(oldblock, oldID);

			}
			if (Block.blocksList[newID] != null) {
				blockIdField.set(Block.blocksList[newID], newID);
			}
			for (int i = 0; i < blockArrayFields.length; i++) {
				Object array = blockArrayFields[i].get(null);
				Object tempObject = Array.get(array, newID);
				Array.set(array, newID, Array.get(array, oldID));
				Array.set(array, oldID, tempObject);
			}
			Block.blocksList[oldID] = oldblock;
		} catch (Throwable e) {
			throw new IllegalArgumentException("Unable to override blockID!", e);
		}
	}
	
	@Override
	public boolean isInitialized() {
		return true;
	}
}
