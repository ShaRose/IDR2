package sharose.mods.IDR2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;

import net.minecraft.client.Minecraft;

import sharose.mods.IDR2.utils.ObjectSignature;

public class IDR2Mappings {
	
	private static IDR2Mappings INSTANCE;
	
	private static final boolean shouldCache = true; //TODO Benchmark the effects of this.
	
	private IDR2Mappings()
	{
		INSTANCE = this;
		// TODO:  Check this
		idPath = new File(Minecraft.getMinecraftDir().getAbsolutePath() + "/config/IDResolverknownIDs.properties");
		
		// Let's load stuff.
		loadMappings();
	}
	
	public static IDR2Mappings getInstance()
	{
		return (INSTANCE != null ? INSTANCE : new IDR2Mappings());
	}
	
	protected ArrayList<IDR2ModReference> modReferences = new ArrayList<IDR2ModReference>();
	private IDR2ModReference lastUsedMod;
	
	private File idPath;
	private boolean isStreamingSaves = true;
	private BufferedWriter streamingOutputStream = null;
	
	protected IDR2ModReference getModReference(ObjectSignature signature)
	{
		IDR2ModReference ref = getModReferenceRaw(signature.getModID());
		return (ref != null ? ref : IDR2ModReference.getModReference(signature.getModID()));
	}
	
	protected IDR2ModReference getModReferenceRaw(String modID)
	{
		if(shouldCache)
		{
			if(modID == lastUsedMod.getModID())
			{
				return lastUsedMod;
			}
		}
		for (IDR2ModReference modRef : modReferences) {
			if(modID == modRef.getModID())
			{
				if(shouldCache)
				{
					lastUsedMod = modRef;
				}
				return modRef;
			}
		}
		return null;
	}

	public ObjectSignature syncSignature(ObjectSignature signature)
	{
		IDR2ModReference modref = getModReference(signature);
		int index = modref.getIndex(signature);
		if (index == -1)
		{
			modref.add(signature);
			// Don't save: That way new signatures will stay dirty. Loaded ones won't.
			return signature;
		}
		return modref.get(index);
	}
	
	private String exportAllMappings() {
		String linebreak = System.getProperty("line.separator");
		StringBuilder mappingsOutput = new StringBuilder();
		mappingsOutput
				.append("#IDResolver Known / Set IDs file. Please do not edit manually.")
				.append(linebreak);
		mappingsOutput.append("SAVEVERSION=v2").append(linebreak);
		
		for (IDR2ModReference modRef : modReferences) {
			for (ObjectSignature signature : modRef.mappings) {
				mappingsOutput.append(signature).append(linebreak);
				signature.setDirty(false);
			}
			modRef.setDirty(false);
		}
		return mappingsOutput.toString();
	}

	private void openMappingOutputStream() throws IOException {
		if (streamingOutputStream != null) {
			streamingOutputStream.close();
			streamingOutputStream = null;
		}
		streamingOutputStream = new BufferedWriter(new FileWriter(idPath));
	}
	
	public void saveMappings()
	{
		try {
		if (isStreamingSaves) {
			if (streamingOutputStream == null) {
				openMappingOutputStream();
				streamingOutputStream.write(exportAllMappings());
			} else {
					for (IDR2ModReference modRef : modReferences) {
						if (modRef.isDirty()) {
							for (ObjectSignature signature : modRef.mappings) {
								if (signature.isDirty()) {
									streamingOutputStream.write(signature
											.toString());
									streamingOutputStream.newLine();
									signature.setDirty(false);
								}
							}
							modRef.setDirty(false);
						}
					}
				streamingOutputStream.flush();
			}
		} else {
			
				openMappingOutputStream();
				streamingOutputStream.write(exportAllMappings());
				streamingOutputStream.close();
				streamingOutputStream = null;
		}
		} catch (Throwable e) {
			IDR2.getLogger().log(Level.WARNING,"IDR2 was unable to save.",e);
		}
	}
	
	public void loadMappings()
 {
		try {
			idPath.getParentFile().mkdirs();
			if (idPath.createNewFile()) {
				IDR2.getLogger().log(Level.WARNING,
						"IDs File not found, creating new one.");
				InputStream stream = IDR2.class.getClassLoader()
						.getResourceAsStream("IDResolverDefaultIDs.properties");
				if (stream != null) {
					IDR2.getLogger().log(Level.FINE,
							"Loading settings from Default IDs file.");
					loadMappings(stream);
					stream.close();
				}
				saveMappings();

			} else {
				try {
					FileInputStream stream = new FileInputStream(idPath);
					loadMappings(stream);
					stream.close();
				} catch (IOException e) {
					IDR2.getLogger()
							.log(Level.INFO,
									"IDResolver - Existing config details are invalid: Creating new settings.");
					modReferences.clear();
				}
			}
		} catch (Throwable e) {
			// TODO: Do stuff.
		}
	}
	
	public void loadMappings(String mappingData)
	{
		StringReader reader = new StringReader(mappingData);
		loadMappings(reader);
		reader.close();
	}
	
	public void loadMappings(InputStream stream)
	{
		InputStreamReader reader = new InputStreamReader(stream);
		loadMappings(reader);
	}
	
	public void loadMappings(Reader stream)
	{
		// I'll use java's properties class here because it's easier. It doesn't load often anyways, unlike saving.
		try
		{
			Properties props = new Properties();
			props.load(stream);

			String saveVersion = props.getProperty("SAVEVERSION");
			
			// Should be version 3 of the mappings format. It's not REALLY a change from v2,
			// but it uses modIDs now, which might break v2 mappings. It's also technically a different mod.
			// I also can't port it for this reason.
			if(!saveVersion.equals("v3"))
			{
				IDR2.getLogger().log(Level.SEVERE,"Invalid save format. IDR2 only supports save mapping v3. Loaded mappings report version as: " + saveVersion);
				// Throw an exception so it doesn't just clobber the old save format, in case they want to save it or back it up.
				throw new RuntimeException("Invalid save format. IDR2 only supports save mapping v3. Loaded mappings report version as: " + saveVersion);
			}
			
			if(modReferences.size() != 0)
			{
				IDR2.getLogger().log(Level.WARNING,"Attempting to load new mappings, but there seem to be some already" + 
						" loaded. Leaving them alone, but this should be considered a serious bug!");
			}
			
			int validSignatures = 0;
			for (String key : props.stringPropertyNames())
			{
				if ("SAVEVERSION".equals(key))
				{
					continue;
				}
				ObjectSignature signature = new ObjectSignature(key,
						props.getProperty(key));
				if (signature.getValidity())
				{
					validSignatures++;
					IDR2ModReference modRef = getModReference(signature);
					if(!modRef.add(signature))
					{
						IDR2.getLogger().log(Level.WARNING,"ObjectSignature '" + signature.toString() + "' was loaded, but it already exists!");
					}
					// Set them to not dirty since they are loading stuff.
					signature.setDirty(false);
					modRef.setDirty(false);
				}
				else
				{
					IDR2.getLogger().log(Level.WARNING,"Attempted to parse invalid ObjectSignature '" + signature.toString() + "'!");
				}
				
			}

			IDR2.getLogger().log(
					Level.FINE,
					"Load finished. Total Parsable entries are ("
							+ validSignatures + " // " + (props.size() - 1)
							+ ") (Parsed / Total).");
		} catch (Throwable e)
		{
			// TODO: Something
		}
	}
}
