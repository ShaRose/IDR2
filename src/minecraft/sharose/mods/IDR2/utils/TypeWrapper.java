package sharose.mods.IDR2.utils;

import java.util.ArrayList;
import java.util.logging.Level;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

/**
 * @author ShaRose
 *	This is an interface designed to codify the most commonly used accessors for Blocks and Items.
 */
public abstract class TypeWrapper {
	
	public enum ObjectType {
		Block,
		Item
	}
	
	/**
	 * @return The current Object this wraps. This could be either a Block or an Item.
	 */
	public abstract Object getObject();
	
	/**
	 * @return The (cached) ObjectSignature for this wrapper.
	 */
	public abstract ObjectSignature getObjectSignature();
	
	/**
	 * @return The ID of this Wrapper.
	 */
	public abstract int getID();
	
	/**
	 * @param newID The new ID to set.
	 * @return The old ID.
	 */
	public abstract int setID(int newID);
	
	public int getOriginalID()
	{
		return getObjectSignature().getOriginalID();
	}
	
	/**
	 * @return The ObjectType of this Wrapper.
	 */
	public abstract ObjectType getType();
	
	/**
	 * @return A string of the currently executing mod's modID. Just a helper.
	 */
	protected String getActiveMod()
	{
		// I sure hope using this doesn't bite me in the ass later, but hey, massive speed boosts are massive speed boosts
		ModContainer mc = Loader.instance().activeModContainer();
        if (mc == null)
        {
            return "<VANILLA>";
        }
        return mc.getModId();
	}
	
	/**
	 * This is used to update the ObjectSignature. It should only be called once, early in creation.
	 * 
	 * @param originalID the Original ID
	 * @return Whether the update was allowed or not. If there was an exception, this should be thrown.
	 */
	public abstract boolean updateObjectSignature(int originalID);
}
