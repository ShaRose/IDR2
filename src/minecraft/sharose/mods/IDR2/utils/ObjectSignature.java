package sharose.mods.IDR2.utils;

import java.util.logging.Level;

import sharose.mods.IDR2.IDR2Mappings;

/**
 * @author ShaRose This is a helper class designed to make it easy and fast to
 *         access metadata related to Signatures for storing Block and Item IDs.
 */
public class ObjectSignature {
	private String modID;
	private boolean isValid = false;
	private boolean isDirty = true;
	private Integer newID;
	private Integer originalID;
	private String originalKey;
	private String originalValue;
	
	private TypeWrapper.ObjectType type;

	private ObjectSignature(TypeWrapper.ObjectType type, int originalID,
			String modID) {
		this.type = type;
		this.originalID = originalID;
		this.modID = modID;
		originalKey = getKey();
		originalValue = this.originalID.toString();
		newID = originalID;
		isValid = true;
	}
	
	/**
	 * Creates a new ObjectSignature from the required information. If the signature is already loaded in IDMapping, 
	 * it will return a reference to that. If it doesn't exist, it will create the reference. Note if it is created, 
	 * it will be flagged as 'dirty', as it will not be resolved yet.
	 * 
	 * @param type
	 *            The ObjectType.
	 * @param originalID
	 *            The Original ID.
	 * @param ModID
	 *            The ModID.
	 */
	public static ObjectSignature getObjectSignature(TypeWrapper.ObjectType type, int originalID, String ModID)
	{
		return IDR2Mappings.getInstance().syncSignature(new ObjectSignature(type,originalID,ModID));
	}

	/**
	 * Creates a new ObjectSignature from a stored Key and Value. If Parsing
	 * fails, getValidity will return false, and all data will be nullified, but
	 * no exception will be thrown. The original Key and Value passed here can be retrieved via the
	 * getOriginalKey and getOriginalValue methods. It will be flagged as 'not dirty' because it should only be used during parsing.
	 * 
	 * @param key
	 *            The key to parse: Formatting should be (Type).(Original
	 *            ID)|(ModID).
	 * @param value
	 *            The value to parse. Simply an Integer for the 'new' ID.
	 */
	public ObjectSignature(String key, String value) {
		originalKey = key;
		originalValue = value;
		try {
			int dotIndex = key.indexOf('.');
			int pipeIndex = key.indexOf('|', dotIndex);
			if ((dotIndex < 4) || (pipeIndex < 1)) {
				throw new IllegalArgumentException(
						"Dot index or Pipe index not within allowed range. DotIndex: "
								+ dotIndex + " PipeIndex: " + pipeIndex);
			}

			String temp = key.substring(0, dotIndex);
			setType(TypeWrapper.ObjectType.valueOf(temp));

			temp = key.substring(dotIndex + 1, pipeIndex);
			setOriginalID(Integer.parseInt(temp));

			setModID(key.substring(pipeIndex + 1));

			setNewID(Integer.parseInt(value));
			isValid = true;
		} catch (Throwable e) {
			setModID(null);
			setOriginalID(null);
			setNewID(null);
			setType(null);
			isValid = false;
		}
		setDirty(false);
	}

	public boolean isVanilla()
	{
		return getValidity() && modID == "<VANILLA>";
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ObjectSignature)) {
			return false;
		}

		return equals((ObjectSignature) obj);
	}

	public boolean equals(ObjectSignature obj) {
		return equals(obj,true);
	}
	
	public boolean equals(ObjectSignature obj,boolean compareTypes) {
		if (!obj.getValidity() || !getValidity()) {
			return false;
		}
		return getOriginalID() == obj.getOriginalID() 
				&& getModID() == obj.getModID() 
				&& (compareTypes == (getType() == obj.getType()));
	}

	public String getModID() {
		return modID;
	}

	/**
	 * @return the formatted copy of the key. Formatting is (Type).(Original
	 *         ID)|(ModID). If ObjectSignature is marked as Invalid, will
	 *         return the original Key.
	 */
	public String getKey() {
		if (getValidity()) {
			return getType() + "." + getOriginalID() + "|" + getModID();
		}
		return getOriginalKey();
	}
	
	public String getValue(){
		if (getValidity()) {
			return getNewID().toString();
		}
		return getOriginalValue();
	}

	public Integer getNewID() {
		return newID;
	}

	public Integer getOriginalID() {
		return originalID;
	}

	public String getOriginalKey() {
		return originalKey;
	}

	public String getOriginalValue() {
		return originalValue;
	}

	public TypeWrapper.ObjectType getType() {
		return type;
	}

	/**
	 * @return True if this ObjectSignature was parsed correctly.
	 */
	public boolean getValidity() {
		return isValid;
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	private void setModID(String baseModName) {
		this.modID = baseModName;
	}

	public void setNewID(Integer newID) {
		this.newID = newID;
		setDirty(true);
		if(isValid)
		{
			IDR2Mappings.getInstance().saveMappings();
		}
	}

	private void setOriginalID(Integer originalID) {
		this.originalID = originalID;
	}

	private void setType(TypeWrapper.ObjectType type) {
		this.type = type;
	}

	public boolean isDirty()
	{
		return isDirty;
	}
	
	public void setDirty(boolean dirty)
	{
		isDirty = dirty;
	}
	
	@Override
	public String toString() {
		return getKey() + "=" + getValue();
	}
}
