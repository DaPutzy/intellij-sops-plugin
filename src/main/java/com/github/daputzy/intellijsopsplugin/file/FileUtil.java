package com.github.daputzy.intellijsopsplugin.file;

import java.io.IOException;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ThrowableRunnable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtil {

	@Getter(lazy = true)
	private static final FileUtil instance = new FileUtil();

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
}
