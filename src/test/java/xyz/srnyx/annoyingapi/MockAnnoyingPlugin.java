package xyz.srnyx.annoyingapi;

import org.jetbrains.annotations.Nullable;
import xyz.srnyx.annoyingapi.library.AnnoyingLibraryManager;


public class MockAnnoyingPlugin extends AnnoyingPlugin {
    @Override @Nullable
    protected AnnoyingLibraryManager createLibraryManager() {
        // Class loader can't be casted for the library manager to work
        return null;
    }

    @Override
    protected void sendStartupMessages() {
        // Don't want to spam the test output with startup messages
    }
}
