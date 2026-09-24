package com.plugandplay.core.plugins;

/**
 * Marcador para plugins que deben ejecutarse después de que todos los demás
 * plugins hayan terminado (por ejemplo persistence).
 */
public interface TerminalPlugin extends Plugin {
}
