package sharose.mods.IDR2;

import java.util.ArrayList;

import sharose.mods.IDR2.utils.ObjectSignature;

public class IDR2ModReference {
	
	private String modID;
	protected ArrayList<ObjectSignature> mappings;
	private boolean isDirty = false;
	private IDR2ModReference(String modID)
	{
		this.modID = modID;
		mappings = new ArrayList<ObjectSignature>();
	}
	
	public static IDR2ModReference getModReference(String modID)
	{
		IDR2ModReference ref = IDR2Mappings.getInstance().getModReferenceRaw(modID);
		if(ref != null)
		{
			return ref;
		}
		ref = new IDR2ModReference(modID);
		IDR2Mappings.getInstance().modReferences.add(ref);
		return ref;
	}
	
	public String getModID()
	{
		return modID;
	}
	
	public boolean contains(ObjectSignature signature)
	{
		return getIndex(signature) != -1;
	}

	public ObjectSignature get(int index)
	{
		return mappings.get(index);
	}
	
	public int getIndex(ObjectSignature signature)
	{
		return mappings.indexOf(signature);
	}
	
	public boolean isDirty()
	{
		return isDirty;
	}
	
	public void setDirty(boolean dirty)
	{
		isDirty = dirty;
	}
	
	public boolean add(ObjectSignature signature)
	{
		int index = mappings.indexOf(signature);
		if(index != -1)
			return false;
		mappings.add(signature);
		signature.setDirty(true);
		setDirty(true);
		return true;
	}
}
