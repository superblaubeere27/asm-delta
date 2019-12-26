/*
 * Copyright (c) 2019 superblaubeere27
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.superblaubeere27.asmdelta.utils;

import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class InstructionComparator {

    public static boolean isSame(InsnList insnListA, InsnList insnListB, List<TryCatchBlockNode> tryCatchA, List<TryCatchBlockNode> tryCatchB) {
        if (insnListA.size() != insnListB.size() || tryCatchA.size() != tryCatchB.size()) {
            return false;
        }

        HashMap<LabelNode, Integer> labelIndexMap = calulateLabelIdecies(insnListA);
        labelIndexMap.putAll(calulateLabelIdecies(insnListB));

        var abstractInsnNodesA = insnListA.toArray();
        var abstractInsnNodesB = insnListB.toArray();

        for (int i = 0; i < abstractInsnNodesA.length; i++) {
            AbstractInsnNode a = abstractInsnNodesA[i];
            AbstractInsnNode b = abstractInsnNodesB[i];

            if (!isSame(labelIndexMap, a, b))
                return false;
        }

        for (int i = 0; i < tryCatchA.size(); i++) {
            TryCatchBlockNode a = tryCatchA.get(i);
            TryCatchBlockNode b = tryCatchB.get(i);

            if (!Objects.equals(a.type, b.type)
                    || !labelIndexMap.get(a.start).equals(labelIndexMap.get(b.start))
                    || !labelIndexMap.get(a.end).equals(labelIndexMap.get(b.end))
                    || !labelIndexMap.get(a.handler).equals(labelIndexMap.get(b.handler))) {
                return false;
            }
        }

        return true;
    }

    private static boolean isSame(HashMap<LabelNode, Integer> labelIndexMap, AbstractInsnNode a, AbstractInsnNode b) {
        if (a.getOpcode() != b.getOpcode() || a.getType() != b.getType() || a.getClass() != b.getClass()) {
            return false;
        }

        if (a instanceof FieldInsnNode) {
            FieldInsnNode fieldInsnNodeA = (FieldInsnNode) a;
            FieldInsnNode fieldInsnNodeB = (FieldInsnNode) b;

            return fieldInsnNodeA.owner.equals(fieldInsnNodeB.owner)
                    && fieldInsnNodeA.name.equals(fieldInsnNodeB.name)
                    && fieldInsnNodeA.desc.equals(fieldInsnNodeB.desc);
        }
        if (a instanceof MethodInsnNode) {
            MethodInsnNode methodInsnNodeA = (MethodInsnNode) a;
            MethodInsnNode methodInsnNodeB = (MethodInsnNode) b;

            return methodInsnNodeA.owner.equals(methodInsnNodeB.owner)
                    && methodInsnNodeA.name.equals(methodInsnNodeB.name)
                    && methodInsnNodeA.desc.equals(methodInsnNodeB.desc)
                    && methodInsnNodeA.itf == methodInsnNodeB.itf;
        }
        if (a instanceof TableSwitchInsnNode) {
            TableSwitchInsnNode switchA = (TableSwitchInsnNode) a;
            TableSwitchInsnNode switchB = (TableSwitchInsnNode) b;

            if (!labelIndexMap.get(switchA.dflt).equals(labelIndexMap.get(switchB.dflt))) {
                return false;
            }

            if (switchA.min != switchB.min || switchA.max != switchB.max) {
                return false;
            }

            return switchA.labels
                    .stream()
                    .map(labelIndexMap::get)
                    .collect(Collectors.toUnmodifiableList())
                    .equals(switchB.labels
                            .stream()
                            .map(labelIndexMap::get)
                            .collect(Collectors.toUnmodifiableList()));


        }
        if (a instanceof LineNumberNode) return true;
        if (a instanceof IincInsnNode) {
            IincInsnNode incA = (IincInsnNode) a;
            IincInsnNode incB = (IincInsnNode) b;

            return incA.incr == incB.incr && incA.var == incB.var;
        }
        if (a instanceof IntInsnNode) {
            return ((IntInsnNode) a).operand == ((IntInsnNode) b).operand;
        }
        if (a instanceof LabelNode) {
            return true;
        }
        if (a instanceof MultiANewArrayInsnNode) {
            MultiANewArrayInsnNode incA = (MultiANewArrayInsnNode) a;
            MultiANewArrayInsnNode incB = (MultiANewArrayInsnNode) b;

            return incA.desc.equals(incB.desc) && incA.dims == incB.dims;
        }
        if (a instanceof LdcInsnNode) {
            return ((LdcInsnNode) a).cst.equals(((LdcInsnNode) b).cst);
        }
        if (a instanceof TypeInsnNode) {
            return ((TypeInsnNode) a).desc.equals(((TypeInsnNode) b).desc);
        }
        if (a instanceof VarInsnNode) {
            return ((VarInsnNode) a).var == ((VarInsnNode) b).var;
        }
        if (a instanceof InvokeDynamicInsnNode) {
            InvokeDynamicInsnNode incA = (InvokeDynamicInsnNode) a;
            InvokeDynamicInsnNode incB = (InvokeDynamicInsnNode) b;

            return incA.bsm.equals(incB.bsm) && Arrays.equals(incA.bsmArgs, incB.bsmArgs) && incA.desc.equals(incB.desc) && incA.name.equals(incB.name);
        }
        if (a instanceof FrameNode) {
            return true; // Assuming true since if all instructions are the same, the frame can't be different
        }
        if (a instanceof JumpInsnNode) {
            JumpInsnNode incA = (JumpInsnNode) a;
            JumpInsnNode incB = (JumpInsnNode) b;

            return labelIndexMap.get(incA.label).equals(labelIndexMap.get(incB.label));
        }
        if (a instanceof LookupSwitchInsnNode) {
            LookupSwitchInsnNode switchA = (LookupSwitchInsnNode) a;
            LookupSwitchInsnNode switchB = (LookupSwitchInsnNode) b;

            if (!labelIndexMap.get(switchA.dflt).equals(labelIndexMap.get(switchB.dflt))) {
                return false;
            }

            if (!switchA.keys.equals(switchB.keys)) {
                return false;
            }

            return switchA.labels
                    .stream()
                    .map(labelIndexMap::get)
                    .collect(Collectors.toUnmodifiableList())
                    .equals(switchB.labels
                            .stream()
                            .map(labelIndexMap::get)
                            .collect(Collectors.toUnmodifiableList()));
        }

        return true;
    }

    private static HashMap<LabelNode, Integer> calulateLabelIdecies(InsnList insnListA) {
        HashMap<LabelNode, Integer> ret = new HashMap<>();

        var abstractInsnNodes = insnListA.toArray();

        for (int i = 0; i < abstractInsnNodes.length; i++) {
            if (abstractInsnNodes[i] instanceof LabelNode) {
                ret.put((LabelNode) abstractInsnNodes[i], i);
            }
        }

        return ret;
    }

}
