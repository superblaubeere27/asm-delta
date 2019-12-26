/*
 * Copyright (c) 2019 superblaubeere27
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.superblaubeere27.asmdelta;

import net.superblaubeere27.asmdelta.difference.AbstractDifference;
import net.superblaubeere27.asmdelta.difference.clazz.*;
import net.superblaubeere27.asmdelta.difference.fields.*;
import net.superblaubeere27.asmdelta.difference.methods.*;
import net.superblaubeere27.asmdelta.utils.InstructionComparator;
import net.superblaubeere27.asmdelta.utils.ScheduledRunnable;
import net.superblaubeere27.asmdelta.utils.Scheduler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ASMDelta {

    public static List<AbstractDifference> calculateDifference(int threadCount, File origFile, File newFile) throws IOException {
        HashMap<String, ClassNode> originalClasses = loadJar(threadCount, loadClasspathFile(origFile));
        HashMap<String, ClassNode> newClasses = loadJar(threadCount, loadClasspathFile(newFile));

        List<AbstractDifference> differences = new ArrayList<>();

        originalClasses.keySet().stream().filter(key -> !newClasses.containsKey(key)).map(RemoveClassDifference::new).forEach(differences::add); // Removed classes
        newClasses.keySet().stream().filter(key -> !originalClasses.containsKey(key)).map(key -> new AddClassDifference(newClasses.get(key))).forEach(differences::add); // Added classes

        LinkedList<String> collect = originalClasses.keySet().stream().filter(newClasses::containsKey).distinct().collect(Collectors.toCollection(LinkedList::new));

        ScheduledRunnable runnable = () -> {
            String next;

            synchronized (collect) {
                next = collect.poll();
            }

            if (next == null) return true;

            compareClasses(differences, originalClasses.get(next), newClasses.get(next));

            return false;
        };

        Scheduler scheduler = new Scheduler(runnable);

        scheduler.run(threadCount);
        scheduler.waitFor();

        return differences;
    }

    private static void compareClasses(List<AbstractDifference> differences, ClassNode oldClass, ClassNode newClass) {
        if (oldClass.access != newClass.access) {
            differences.add(new ClassAccessDifference(oldClass.name, oldClass.access, newClass.access));
        }

        ClassMetadataDifference metadataDifference = ClassMetadataDifference.createNew(
                oldClass.name,
                !Objects.equals(oldClass.outerClass, newClass.outerClass) ? newClass.outerClass : null,
                !Objects.equals(oldClass.nestHostClass, newClass.nestHostClass) ? newClass.nestHostClass : null,
                !Objects.equals(oldClass.outerMethod, newClass.outerMethod) ? newClass.outerMethod : null,
                !Objects.equals(oldClass.outerMethodDesc, newClass.outerMethodDesc) ? newClass.outerMethodDesc : null,
                !Objects.equals(oldClass.signature, newClass.signature) ? newClass.signature : null,
                !Objects.equals(oldClass.sourceDebug, newClass.sourceDebug) ? newClass.sourceDebug : null,
                !Objects.equals(oldClass.sourceFile, newClass.sourceFile) ? newClass.sourceFile : null,
                !Objects.equals(oldClass.superName, newClass.superName) ? newClass.superName : null
        );

        if (metadataDifference != null) {
            differences.add(metadataDifference);
        }

        if (oldClass.version != newClass.version) {
            differences.add(new ClassVersionDifference(oldClass.name, newClass.version));
        }

        // TODO Implement annotations ._.

        //<editor-fold desc="Fields">

        {
            HashMap<String, FieldNode> oldFields = new HashMap<>();

            if (oldClass.fields != null) {
                for (FieldNode field : oldClass.fields) {
                    oldFields.put(field.name, field);
                }
            }
            HashMap<String, FieldNode> newFields = new HashMap<>();

            if (newClass.fields != null) {
                for (FieldNode field : newClass.fields) {
                    newFields.put(field.name, field);
                }
            }

            oldFields.keySet().stream().filter(key -> !newFields.containsKey(key)).map(key -> new RemoveFieldDifference(oldClass.name, key)).forEach(differences::add); // Removed fields
            newFields.keySet().stream().filter(key -> !oldFields.containsKey(key)).map(key -> new AddFieldDifference(oldClass.name, newFields.get(key))).forEach(differences::add); // Added fields

            oldFields.keySet().stream().filter(newFields::containsKey).forEach(key -> compareField(differences, oldClass.name, oldFields.get(key), newFields.get(key)));
        }

        //</editor-fold>

        //<editor-fold desc="Methods">

        {
            HashMap<String, MethodNode> oldMethods = new HashMap<>();

            if (oldClass.fields != null) {
                for (MethodNode method : oldClass.methods) {
                    oldMethods.put(method.name + method.desc, method);
                }
            }
            HashMap<String, MethodNode> newMethods = new HashMap<>();

            if (newClass.fields != null) {
                for (MethodNode method : newClass.methods) {
                    newMethods.put(method.name + method.desc, method);
                }
            }

            oldMethods.keySet().stream().filter(key -> !newMethods.containsKey(key)).map(key -> {
                MethodNode methodNode = oldMethods.get(key);

                return new RemoveMethodDifference(oldClass.name, methodNode.name, methodNode.desc);
            }).forEach(differences::add); // Removed fields

            newMethods.keySet().stream().filter(key -> !oldMethods.containsKey(key)).map(key -> new AddMethodDifference(oldClass.name, newMethods.get(key))).forEach(differences::add); // Added fields

            oldMethods.keySet().stream().filter(newMethods::containsKey).forEach(key -> compareMethod(differences, oldClass.name, oldMethods.get(key), newMethods.get(key)));
        }

        //</editor-fold>


    }

    private static void compareMethod(List<AbstractDifference> differences, String className, MethodNode oldMethod, MethodNode newMethod) {
        if (oldMethod.access != newMethod.access) {
            differences.add(new MethodAccessDifference(className, oldMethod.name, oldMethod.desc, oldMethod.access, newMethod.access));
        }
        if (!Objects.equals(oldMethod.signature, newMethod.signature)) { // FieldNode.signature might be null
            differences.add(new MethodSignatureDifference(className, oldMethod.name, oldMethod.desc, newMethod.signature));
        }
        if (!(oldMethod.exceptions == null ? Collections.emptyList() : oldMethod.exceptions).equals(newMethod.exceptions == null ? Collections.emptyList() : newMethod.exceptions)) {
            differences.add(new MethodExceptionDifference(className, oldMethod.name, oldMethod.desc, newMethod.exceptions));
        }
        if (oldMethod.annotationDefault != newMethod.annotationDefault) {
            differences.add(new MethodAnnotationDefaultDifference(className, oldMethod.name, oldMethod.desc, newMethod.annotationDefault));
        }
        if (oldMethod.maxLocals != newMethod.maxLocals || oldMethod.maxStack != newMethod.maxStack) {
            differences.add(new MethodMaxsDifference(className, oldMethod.name, oldMethod.desc, newMethod.maxStack, newMethod.maxLocals));
        }
        if (!InstructionComparator.isSame(oldMethod.instructions, newMethod.instructions, oldMethod.tryCatchBlocks, newMethod.tryCatchBlocks)) {
            differences.add(new MethodInstructionDifference(className, oldMethod.name, oldMethod.desc, newMethod));
        }
    }

    private static void compareField(List<AbstractDifference> differences, String className, FieldNode oldField, FieldNode newField) {
        if (oldField.access != newField.access) {
            differences.add(new FieldAccessDifference(className, oldField.name, oldField.access, newField.access));
        }
        if (!oldField.desc.equals(newField.desc)) {
            differences.add(new FieldDescriptionDifference(className, oldField.name, newField.desc));
        }
        if (!Objects.equals(oldField.signature, newField.signature)) { // FieldNode.signature might be null
            differences.add(new FieldSignatureDifference(className, oldField.name, newField.signature));
        }
        if (!Objects.equals(oldField.value, newField.value)) {
            differences.add(new FieldValueDifference(className, oldField.name, newField.value));
        }
        // TODO Implement annotation stuff
    }

    public static HashMap<String, ClassNode> loadJar(int threadCount, List<byte[]> bytes1) {
        LinkedList<byte[]> byteList = new LinkedList<>(bytes1);
        HashMap<String, ClassNode> loaded = new HashMap<>();

        ScheduledRunnable runnable = () -> {
            Map<String, ClassNode> map = new HashMap<>();

            while (true) {
                byte[] bytes;

                synchronized (byteList) {
                    bytes = byteList.poll();
                }

                if (bytes == null) break;

                ClassReader reader = new ClassReader(bytes);
                ClassNode node = new ClassNode();
//                reader.accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                reader.accept(node, ClassReader.SKIP_DEBUG);
                map.put(node.name, node);
            }

            synchronized (loaded) {
                loaded.putAll(map);
            }

            return true;
        };

        Scheduler scheduler = new Scheduler(runnable);

        scheduler.run(threadCount);
        scheduler.waitFor();
        return loaded;
    }

    public static List<byte[]> loadClasspathFile(File file) throws IOException {
        ZipFile zipIn = new ZipFile(file);
        Enumeration<? extends ZipEntry> entries = zipIn.entries();
        boolean isJmod = file.getName().endsWith(".jmod");

        List<byte[]> byteList = new ArrayList<>(zipIn.size());

        while (entries.hasMoreElements()) {
            ZipEntry ent = entries.nextElement();
            if (ent.getName().endsWith(".class") && (!isJmod || !ent.getName().endsWith("module-info.class") && ent.getName().startsWith("classes/"))) {
                byteList.add(zipIn.getInputStream(ent).readAllBytes());
            }
        }
        zipIn.close();

        return byteList;
    }

}
