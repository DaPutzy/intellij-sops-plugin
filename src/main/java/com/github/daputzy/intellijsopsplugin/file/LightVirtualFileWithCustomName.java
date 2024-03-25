package com.github.daputzy.intellijsopsplugin.file;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

public class LightVirtualFileWithCustomName extends LightVirtualFile {
    private final VirtualFile file;
    public LightVirtualFileWithCustomName(VirtualFile file, FileType fileType, String content) {
        super(file.getPath(), fileType, content);
        this.file = file;
    }

    @Override
    @NotNull
    public String getName() {
        return this.file.getName() + " [decrypted]";
    }

    public String getFilePath() {
        return this.file.getPath();
    }
}