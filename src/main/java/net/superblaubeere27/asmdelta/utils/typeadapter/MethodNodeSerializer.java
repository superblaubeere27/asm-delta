/*
 * Copyright (c) 2019 superblaubeere27
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.superblaubeere27.asmdelta.utils.typeadapter;

import com.google.gson.*;
import net.superblaubeere27.asmdelta.utils.Hex;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Objects;

public class MethodNodeSerializer implements JsonSerializer<MethodNode>, JsonDeserializer<MethodNode> {

    @Override
    public JsonElement serialize(MethodNode methodNode, java.lang.reflect.Type type, JsonSerializationContext jsonSerializationContext) {
        ClassNode classNode = new ClassNode();

        classNode.version = Opcodes.V1_8;
        classNode.access = Opcodes.ACC_PRIVATE;
        classNode.name = "asdf";
        classNode.signature = null;
        classNode.superName = "java/lang/Object";
        classNode.interfaces = Collections.emptyList();

        methodNode.accept(classNode);

        ClassWriter classWriter = new ClassWriter(0);


        classNode.accept(classWriter);

        return new JsonPrimitive(Hex.encodeHexString(classWriter.toByteArray()));
    }

    @Override
    public MethodNode deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        ClassNode classNode = new ClassNode();

        ClassReader cr;

        try {
            cr = new ClassReader(Hex.decodeHex(jsonElement.getAsString()));
        } catch (IOException e) {
            throw new JsonParseException(e);
        }

        cr.accept(classNode, 0);

        if (Objects.requireNonNull(classNode.methods).size() != 1) {
            throw new JsonParseException("Class has more than a method");
        }

        return classNode.methods.get(0);
    }
}