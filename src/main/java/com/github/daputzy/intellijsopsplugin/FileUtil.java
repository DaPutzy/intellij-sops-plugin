package com.github.daputzy.intellijsopsplugin;

import java.io.IOException;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.fileTypes.UnknownFileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ThrowableRunnable;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class FileUtil {

	public @NotNull Document getDocument(final VirtualFile file) {
		final Document document = ReadAction.compute(() -> FileDocumentManager.getInstance().getDocument(file));

		if (document == null) {
			throw new RuntimeException("Could not get document for file");
		}

		return document;
	}

	public @NotNull String getContent(final VirtualFile file) {
		return ReadAction.compute(() -> LoadTextUtil.loadText(file).toString());
	}

	public void writeContentBlocking(final VirtualFile file, final String content) {
		final ThrowableRunnable<IOException> runnable = () -> file.setBinaryContent(content.getBytes(file.getCharset()));

		ApplicationManager.getApplication().invokeAndWait(() -> {
			try {
				WriteAction.runAndWait(runnable);
			} catch (final IOException e) {
				throw new RuntimeException("Could not write content to file", e);
			}
		});
	}

	public FileType getFileType(final VirtualFile file) {
		final FileType fileType = FileTypeRegistry.getInstance().getFileTypeByFileName(file.getName());

		if (fileType instanceof UnknownFileType) {
			return PlainTextFileType.INSTANCE;
		}

		return fileType;
	}
}
