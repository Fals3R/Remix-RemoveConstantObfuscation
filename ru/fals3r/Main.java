package ru.fals3r;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class Main {
	
	private static boolean is_ldc_digital(AbstractInsnNode ldc) {
		return ((LdcInsnNode)ldc).cst instanceof Number;
	}
	
	private static void remove_constants(String file_path) throws Exception {
		ClassNode class_node = new ClassNode();
		ClassReader class_reader = new ClassReader(new FileInputStream(file_path));
		class_reader.accept(class_node, 0);
		
		List<MethodNode> class_methods = class_node.methods;
		
		class_methods.forEach(method -> {
			InsnList instructions = method.instructions;
			for(int i = 0; i < instructions.size(); i++) {
				AbstractInsnNode instruction = instructions.get(i);
				if(instruction.getOpcode() == Opcodes.LDC && is_ldc_digital(instruction)) {
					if(i + 6 <= instructions.size()) { 
						AbstractInsnNode instr1 = instructions.get(i + 1);
						if(instr1.getOpcode() != Opcodes.LDC || !is_ldc_digital(instr1))
							continue;
						
						AbstractInsnNode instr2 = instructions.get(i + 2);
						if(instr2.getOpcode() != Opcodes.SWAP)
							continue;
						
						AbstractInsnNode instr3 = instructions.get(i + 3);
						if(instr3.getOpcode() != Opcodes.DUP_X1)
							continue;
						
						AbstractInsnNode instr4 = instructions.get(i + 4);
						if(instr4.getOpcode() != Opcodes.POP2)
							continue;
						
						AbstractInsnNode instr5 = instructions.get(i + 5);
						if(instr5.getOpcode() != Opcodes.LDC || !is_ldc_digital(instr5))
							continue;
						
						AbstractInsnNode instr6 = instructions.get(i + 6);
						if(instr6.getOpcode() != Opcodes.IXOR)
							continue;
						
						int firstLdc = ((Number)((LdcInsnNode)instruction).cst).intValue();
						int lastLdc = ((Number)((LdcInsnNode)instr5).cst).intValue();
						
						for(int k = 0; k < 7; k++) {
							AbstractInsnNode trash = instructions.get(i);
							instructions.remove(trash);
						}
						
						instructions.insertBefore(instructions.get(i), new LdcInsnNode(firstLdc ^ lastLdc));
					}
				}
			}
		});
		
		ClassWriter class_writer = new ClassWriter(0);
		class_node.accept(class_writer);
		
		try (FileOutputStream fos = new FileOutputStream(file_path)) {
			fos.write(class_writer.toByteArray());
		}
	}
	
	private static void anti_remix_constant_obfuscation(String classesDir) throws Exception {
		File[] files = new File(classesDir).listFiles();
		if (files != null) {
			for (File f : files) {
				if(f.isDirectory())
					anti_remix_constant_obfuscation(f.getAbsolutePath());
				else
					remove_constants(f.getAbsolutePath());
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		anti_remix_constant_obfuscation("C:\\kekendos\\test"); // path to victim classes
	}

}
