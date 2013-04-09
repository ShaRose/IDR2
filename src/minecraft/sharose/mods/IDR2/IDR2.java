package sharose.mods.IDR2;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import sharose.mods.IDR2.utils.BlockWrapper;
import sharose.mods.IDR2.utils.TypeWrapper;
import sharose.mods.IDR2.utils.TypeWrapper.ObjectType;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;

@Mod(name = "ID Resolver 2", modid = "IDR2", version = "2.0", acceptedMinecraftVersions = "1.5.1",dependencies="after:*")
public class IDR2 {
	private static Logger _logger;
	
	public static Logger getLogger()
	{
		if (_logger != null)
		{
			return _logger;
		}
		_logger = Logger.getLogger("IDR2");
		
		_logger.setParent(FMLLog.getLogger());
		_logger.setUseParentHandlers(true);
		
		//TODO: Add a special file for IDR2 stuff.
		
		return _logger;
	}
	
	private static Boolean wasBlockInited = false;
	private static Boolean wasItemInited = false;

	public static int getResolvedID(TypeWrapper wrapper)
	{
		if(!wasBlockInited && wrapper.getType() == ObjectType.Block)
		{
			wasBlockInited = true;
		}
		if(!wasItemInited && wrapper.getType() == ObjectType.Item)
		{
			wasItemInited = true;
		}
		getLogger().log(Level.FINEST,"Attempting to get resolved ID: Signature is " + wrapper.getObjectSignature().toString());
		
		if(!wrapper.getObjectSignature().isDirty())
		{
			// Not dirty? Assume it's loaded and therefore good to go.
			return wrapper.getID();
		}
		
		// TODO: The actual resolve part. Also the GUI and stuff. God this is going to be horrible. Let's leave this till last.
		
		
		return wrapper.getID();
	}
}
