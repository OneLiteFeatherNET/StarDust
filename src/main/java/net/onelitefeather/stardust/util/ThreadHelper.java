package net.onelitefeather.stardust.util;

public interface ThreadHelper {

    default void syncThreadForServiceLoader(Runnable runnable) {
        Thread currentThread = Thread.currentThread();
        ClassLoader originalClassLoader = currentThread.getContextClassLoader();
        ClassLoader pluginClassLoader = this.getClass().getClassLoader();
        try {
            currentThread.setContextClassLoader(pluginClassLoader);
            runnable.run();
        } finally {
            currentThread.setContextClassLoader(originalClassLoader);
        }
    }
}
