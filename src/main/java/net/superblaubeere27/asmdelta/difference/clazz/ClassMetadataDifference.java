/*
 * Copyright (c) 2019 superblaubeere27
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.superblaubeere27.asmdelta.difference.clazz;

import net.superblaubeere27.asmdelta.difference.AbstractDifference;
import net.superblaubeere27.asmdelta.difference.VerificationException;
import org.objectweb.asm.tree.ClassNode;

import java.util.HashMap;

public class ClassMetadataDifference extends AbstractDifference {
    private String className;

    private String outerClass;
    private String nestHostClass;
    private String outerMethod;
    private String outerMethodDesc;
    private String signature;
    private String sourceDebug;
    private String sourceFile;
    private String superName;

    public ClassMetadataDifference(String className, String outerClass, String nestHostClass, String outerMethod, String outerMethodDesc, String signature, String sourceDebug, String sourceFile, String superName) {
        this.className = className;
        this.outerClass = outerClass;
        this.nestHostClass = nestHostClass;
        this.outerMethod = outerMethod;
        this.outerMethodDesc = outerMethodDesc;
        this.signature = signature;
        this.sourceDebug = sourceDebug;
        this.sourceFile = sourceFile;
        this.superName = superName;
    }

    public static ClassMetadataDifference createNew(String className, String outerClass, String nestHostClass, String outerMethod, String outerMethodDesc, String signature, String sourceDebug, String sourceFile, String superName) {
        if (outerClass == null
                && nestHostClass == null
                && outerMethod == null
                && outerMethodDesc == null
                && signature == null
                && sourceDebug == null
                && sourceFile == null
                && superName == null) {
            return null;
        }


        return new ClassMetadataDifference(className, outerClass, nestHostClass, outerMethod, outerMethodDesc, signature, sourceDebug, sourceFile, superName);
    }


    @Override
    public void apply(HashMap<String, ClassNode> classes) throws VerificationException {
        ClassNode node = classes.get(className);

        if (outerClass != null)
            node.outerClass = outerClass;
        if (nestHostClass != null)
            node.nestHostClass = nestHostClass;
        if (outerMethod != null)
            node.outerMethod = outerMethod;
        if (outerMethodDesc != null)
            node.outerMethodDesc = outerMethodDesc;
        if (signature != null)
            node.signature = signature;
        if (sourceDebug != null)
            node.sourceDebug = sourceDebug;
        if (sourceFile != null)
            node.sourceFile = sourceFile;
        if (superName != null)
            node.superName = superName;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public boolean canBeAppliedAtRuntime() {
        return superName != null;
    }
}
