package sharose.mods.IDR2.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Level;

import sharose.mods.IDR2.utils.internal.BlockUpdaterASM;
import sharose.mods.IDR2.utils.internal.BlockUpdaterRef;
import sharose.mods.IDR2.utils.internal.ItemUpdaterASM;
import sharose.mods.IDR2.utils.internal.ItemUpdaterRef;
import sharose.mods.IDR2.utils.internal.Updater;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public class ItemWrapper extends TypeWrapper {
	
	private static Updater updater = new ItemUpdaterRef();
	
	static
	{
		// try to use the ASMified versions
		try
		{
			// just in case
			Updater asmUpdater = new ItemUpdaterASM();
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
	
	Item wrappedItem;
	Block wrappedBlock;
	boolean isItemBlock;
	ObjectSignature objectSignature;
	
	public ItemWrapper(Item item)
	{
		wrappedItem = item;
		isItemBlock = ItemBlock.class.isInstance(wrappedItem);
	}
	
	public boolean isItemBlock()
	{
		return isItemBlock;
	}
	
	@Override
	public Object getObject() {
		return wrappedItem;
	}
	
	public Item getItem() {
		return wrappedItem;
	}
	
	public Block getMatchingBlock()
	{
		if(!isItemBlock())
			return null;
		if(wrappedBlock != null)
		{
			return wrappedBlock;
		}
		wrappedBlock = Block.blocksList[getID()];
		try
		{
			if(wrappedBlock.wrapper.getObjectSignature().getModID().equals(getObjectSignature().getModID()))
			{
				wrappedBlock.wrapper.wrappedBlockItem = this.getItem();
			}
		}
		catch(Throwable e)
		{
			wrappedBlock = null;
		}
		return wrappedBlock;
	}

	@Override
	public ObjectSignature getObjectSignature() {
		return objectSignature;
	}

	@Override
	public int getID() {
		return wrappedItem.itemID;
	}

	@Override
	public int setID(int newID) {
		return setID(newID,true);
	}
	
	public int setID(int newID,boolean includeBlock)
	{
		int oldID = getID();
		
		updater.swapData(oldID, newID);
		
		if(includeBlock && isItemBlock)
		{
			wrappedBlock.wrapper.setID(newID, false);
		}
		
		getObjectSignature().setNewID(newID);
		return oldID;
	}

	@Override
	public ObjectType getType() {
		return ObjectType.Item;
	}

	@Override
	public boolean updateObjectSignature(int originalID) {
		if(objectSignature != null)
			return false;
		objectSignature = ObjectSignature.getObjectSignature(ObjectType.Item, originalID, getActiveMod());
		
		return true;
	}

}
