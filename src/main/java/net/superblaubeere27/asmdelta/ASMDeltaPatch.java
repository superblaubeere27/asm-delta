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
import net.superblaubeere27.asmdelta.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ASMDeltaPatch {
    private String patchName;
    private int asmDeltaVersion;
    private List<AbstractDifference> differenceList;

    public ASMDeltaPatch(String patchName, int asmDeltaVersion, List<AbstractDifference> differenceList) {
        this.patchName = patchName;
        this.asmDeltaVersion = asmDeltaVersion;
        this.differenceList = differenceList;
    }

    public static ASMDeltaPatch read(InputStream inputStream) throws IOException {
        try (var gz = new GZIPInputStream(inputStream)) {
            return Utils.GSON.fromJson(new String(gz.readAllBytes(), StandardCharsets.UTF_8), ASMDeltaPatch.class);
        }
    }

    public String getPatchName() {
        return patchName;
    }

    public int getAsmDeltaVersion() {
        return asmDeltaVersion;
    }

    public List<AbstractDifference> getDifferenceList() {
        return differenceList;
    }

    public void write(OutputStream outputStream) throws IOException {
        try (var gz = new GZIPOutputStream(outputStream)) {
            gz.write(Utils.GSON.toJson(this).getBytes(StandardCharsets.UTF_8));
        }
    }
}
