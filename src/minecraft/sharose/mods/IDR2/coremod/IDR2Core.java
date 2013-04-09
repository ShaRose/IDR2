package sharose.mods.IDR2.coremod;

import java.util.Map;

import cpw.mods.fml.relauncher.FMLRelauncher;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

/**
 * @author ShaRose
 * The core plugin. Just tells FML to use the patcher class.
 */
@IFMLLoadingPlugin.MCVersion("1.5.1")
public class IDR2Core implements IFMLLoadingPlugin {

	@Override
	public String[] getLibraryRequestClass() {
		return null;
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[]{"sharose.mods.IDR2.coremod.IDR2Patcher"};
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
	}
	
}
