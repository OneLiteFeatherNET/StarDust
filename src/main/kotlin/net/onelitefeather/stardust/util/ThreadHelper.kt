package net.onelitefeather.stardust.util


interface ThreadHelper {
    fun syncThreadForServiceLoader(runnable: Runnable) {
        val currentThread = Thread.currentThread()
        val originalClassLoader = currentThread.contextClassLoader
        val pluginClassLoader = this.javaClass.classLoader
        try {
            currentThread.contextClassLoader = pluginClassLoader
            runnable.run()
        } finally {
            currentThread.contextClassLoader = originalClassLoader
        }
    }
}
