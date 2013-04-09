package sharose.mods.IDR2.coremod;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.minecraft.client.Minecraft;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import sharose.mods.IDR2.IDR2;
import sharose.mods.IDR2.utils.BlockWrapper;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import cpw.mods.fml.relauncher.IClassTransformer;

public class IDR2Patcher implements IClassTransformer, Opcodes {

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes) {
		if (transformedName.equals("net.minecraft.block.Block")) {
			return transformPrimaryHook(
					name,
					"(IL"+ (name.equals(transformedName) ? "net/minecraft/block/material/Material" : 
						FMLDeobfuscatingRemapper.INSTANCE.unmap("net/minecraft/block/material/Material"))
							+ ";)V", "BlockWrapper", bytes);
		}

		if (transformedName.equals("net.minecraft.item.Item")) {
			return transformPrimaryHook(name, "(I)V", "ItemWrapper", bytes);
		}

		if (transformedName.equals("sharose.mods.IDR2.utils.internal.BlockUpdaterASM")) {
			try {
				return transformBlockUpdater(bytes);
			} catch (Throwable e) {
				return bytes;
			}
		}

		if (transformedName.equals("sharose.mods.IDR2.utils.internal.ItemUpdaterASM")) {
			try {
				return transformItemUpdater(bytes);
			} catch (Throwable e) {
				return bytes;
			}
		}

		return bytes;
	}

	public byte[] transformPrimaryHook(String name, String constructorDesc,
			String wrapperType, byte[] bytes) {
		ClassNode cn = new ClassNode(ASM4);
		ClassReader cr = new ClassReader(bytes);
		cr.accept(cn, ASM4);

		// First, let's add the wrapper. We'll check if it's there first, for
		// development etc

		boolean fieldExists = false;
		for (FieldNode field : cn.fields) {
			if (field.name.equals("wrapper")
					&& field.desc.equals("Lsharose/mods/IDR2/utils/" + wrapperType
							+ ";")) {
				fieldExists = true;
			}
		}

		if (!fieldExists) {
			cn.fields
					.add(new FieldNode(ACC_PUBLIC, "wrapper",
							"Lsharose/mods/IDR2/utils/" + wrapperType + ";",
							null, null));
		}

		// Now that the field is added, let's add the other code!

		for (Object obj : cn.methods) {
			MethodNode methodNode = (MethodNode) obj;

			if ("<init>".equals(methodNode.name)
					&& (methodNode.desc == constructorDesc)) {

				AbstractInsnNode currentNode = methodNode.instructions
						.getFirst();

				// let's just loop past INVOKESPECIAL
				// java/lang/Object.<init>()V, then inject our stuff.
				// Simple, easy, and works with any other hacks that might be
				// here.

				boolean working = true;

				while (working) {
					if (currentNode.getOpcode() == INVOKESPECIAL) {
						MethodInsnNode testingNode = (MethodInsnNode) currentNode;
						if (testingNode.owner.equals("java/lang/Object")
								&& testingNode.name.equals("<init>")
								&& testingNode.desc.equals("()V")) {
							// Bingo!
							working = false;
						}
					}
					currentNode = currentNode.getNext();
				}
				// Ok, so now we have skipped past the Object init that we need
				// to leave alone. Now let's add our stuff.
				InsnList IDRCode = new InsnList();

				// This is fairly obvious. It also generates the signature.
				// wrapper = new BlaWrapper(this);
				IDRCode.add(new VarInsnNode(ALOAD, 0));
				IDRCode.add(new TypeInsnNode(NEW, "sharose/mods/IDR2/utils/"
						+ wrapperType + ""));
				IDRCode.add(new InsnNode(DUP));
				IDRCode.add(new VarInsnNode(ALOAD, 0));
				IDRCode.add(new MethodInsnNode(INVOKESPECIAL,
						"sharose/mods/IDR2/utils/" + wrapperType + "",
						"<init>", "(L" + name + ";)V"));
				IDRCode.add(new FieldInsnNode(PUTFIELD, name, "wrapper",
						"Lsharose/mods/IDR2/utils/" + wrapperType + ";"));

				// This attempts to load the signature: If it already exists, it replaces it with the loaded instance. Otherwise, it adds it to the mapping list.
				// If it's loaded isDirty will be false, but otherwise it'll be true.
				// wrapper.updateObjectSignature(par1);
				IDRCode.add(new VarInsnNode(ALOAD, 0));
				IDRCode.add(new FieldInsnNode(GETFIELD, name, "wrapper",
						"Lsharose/mods/IDR2/utils/" + wrapperType + ";"));
				IDRCode.add(new VarInsnNode(ILOAD, 1));
				IDRCode.add(new MethodInsnNode(INVOKEVIRTUAL,
						"sharose/mods/IDR2/utils/" + wrapperType + "",
						"updateObjectSignature", "(I)Z"));
				IDRCode.add(new InsnNode(POP));

				// Now it'll try and resolve stuff. If it's not dirty, it'll just return the stored ID. Otherwise, stuff happens.
				// par1 = IDR2.getResolvedID(wrapper);
				IDRCode.add(new VarInsnNode(ALOAD, 0));
				IDRCode.add(new FieldInsnNode(GETFIELD, name, "wrapper",
						"Lsharose/mods/IDR2/utils/" + wrapperType + ";"));
				IDRCode.add(new MethodInsnNode(INVOKESTATIC,
						"sharose/mods/IDR2/IDR2", "getResolvedID",
						"(Lsharose/mods/IDR2/utils/TypeWrapper;)I"));
				IDRCode.add(new VarInsnNode(ISTORE, 1));

				// And now I inject it!
				methodNode.instructions.insertBefore(currentNode, IDRCode);
			}
		}
		ClassWriter cw = new ClassWriter(ASM4);
		cn.accept(cw);
		return cw.toByteArray();
	}

	public byte[] transformBlockUpdater(byte[] bytes) {
		//TODO: Transform stuff.
		return bytes;
	}

	public byte[] transformItemUpdater(byte[] bytes) {
		//TODO: Transform stuff.
		return bytes;
	}
}
