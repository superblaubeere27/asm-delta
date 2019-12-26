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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.superblaubeere27.asmdelta.difference.AbstractDifference;
import net.superblaubeere27.asmdelta.utils.typeadapter.AbstractDifferenceSerializer;
import net.superblaubeere27.asmdelta.utils.typeadapter.ClassNodeSerializer;
import net.superblaubeere27.asmdelta.utils.typeadapter.MethodNodeSerializer;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils {
    public static final Gson GSON;

    static {
        GSON = new GsonBuilder()
                .registerTypeAdapter(ClassNode.class, new ClassNodeSerializer())
                .registerTypeAdapter(MethodNode.class, new MethodNodeSerializer())
                .registerTypeAdapter(AbstractDifference.class, new AbstractDifferenceSerializer())
                .create();
    }

    public static String prettyprint(MethodNode insnNode) {
        final Printer printer = new Textifier();
        TraceMethodVisitor methodPrinter = new TraceMethodVisitor(printer);
        insnNode.accept(methodPrinter);
        StringWriter sw = new StringWriter();
        printer.print(new PrintWriter(sw));
        printer.getText().clear();
        return sw.toString().trim();
    }

    public static String prettyprint(InsnList insnNode) {
        final Printer printer = new Textifier();
        TraceMethodVisitor methodPrinter = new TraceMethodVisitor(printer);
        insnNode.accept(methodPrinter);
        StringWriter sw = new StringWriter();
        printer.print(new PrintWriter(sw));
        printer.getText().clear();
        return sw.toString().trim();
    }

    public static String prettyprint(ClassNode insnNode) {
        StringWriter sw = new StringWriter();
        TraceClassVisitor methodPrinter = new TraceClassVisitor(new PrintWriter(sw));
        insnNode.accept(methodPrinter);
//        printer.print(new PrintWriter(sw));
//        printer.getText().clear();
        return sw.toString().trim();
    }

}
