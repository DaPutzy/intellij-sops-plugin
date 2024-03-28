package com.github.daputzy.intellijsopsplugin.file;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.LocalTimeCounter;
import org.jetbrains.annotations.NotNull;

public class DecryptedSopsFileWithReference extends LightVirtualFile {

    /**
     * the original encrypted sops file
     */
    private final VirtualFile original;

    /**
     * suffix for the end of the title (i.e. filename.ending suffix)
     */
    private final String suffix;

    public DecryptedSopsFileWithReference(final VirtualFile original, final String content, final String suffix) {
        super(original, content, LocalTimeCounter.currentTime());

        this.original = original;
        this.suffix = suffix;
    }

    @Override
    public @NotNull String getName() {
        return this.original.getName() + " " + suffix;
    }

    public @NotNull String getFilePath() {
        return this.original.getPath();
    }
}
