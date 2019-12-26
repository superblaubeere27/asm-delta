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

import java.util.ArrayList;
import java.util.List;

public class Scheduler {
    private final List<Thread> runningThreads = new ArrayList<>();
    private ScheduledRunnable runnable;

    public Scheduler(ScheduledRunnable runnable) {
        this.runnable = runnable;
    }


    public void run(int threads) {
        for (int i = 0; i < threads; i++) {
            runningThreads.add(new Thread(() -> {
                while (true) {
                    if (runnable.runTick()) break;
                }
            }, "Thread-" + i));
        }

        runningThreads.forEach(Thread::start);
    }

    public void waitFor() {
        for (Thread runningThread : runningThreads) {
            try {
                runningThread.join();
            } catch (InterruptedException ignored) {
                break;
            }
        }
    }

}
