package sharose.mods.IDR2.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.logging.Level;

import sharose.mods.IDR2.utils.internal.BlockUpdaterASM;
import sharose.mods.IDR2.utils.internal.BlockUpdaterRef;
import sharose.mods.IDR2.utils.internal.Updater;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.ModContainer;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class BlockWrapper extends TypeWrapper {

	private static Updater updater = new BlockUpdaterRef();
	
	static
	{
		// try to use the ASMified versions
		try
		{
			// just in case
			Updater asmUpdater = new BlockUpdaterASM();
			if(asmUpdater.isInitialized())
			{
				// Seems it was able to transform, so.
				updater = asmUpdater;
			}
		}
		catch(Throwable e)
		{
			// Swallow it
		}
	}
	
	Block wrappedBlock;
	Item wrappedBlockItem;
	ObjectSignature objectSignature;
	
	
	public BlockWrapper(Block block)
	{
		wrappedBlock = block;
	}
	
	@Override
	public Object getObject() {
		return wrappedBlock;
	}
	
	public Block getBlock() {
		return wrappedBlock;
	}
	
	public Item getItem() {
		return wrappedBlockItem;
	}

	@Override
	public ObjectSignature getObjectSignature() {
		return objectSignature;
	}

	@Override
	public int getID() {
		return wrappedBlock.blockID;
	}

	@Override
	public int setID(int newID) {
		return setID(newID,true);
	}
	
	public int setID(int newID,boolean includeItem)
	{
		int oldID = getID();

		updater.swapData(oldID, newID);

		if (includeItem)
		{
			wrappedBlockItem.wrapper.setID(newID, false);
		}

		getObjectSignature().setNewID(newID);
		return oldID;
	}

	@Override
	public ObjectType getType() {
		return ObjectType.Block;
	}
	
	@Override
	public boolean updateObjectSignature(int originalID) {
		if(objectSignature != null)
			return false;
		objectSignature = ObjectSignature.getObjectSignature(ObjectType.Block, originalID, getActiveMod());
		return true;
	}

}
