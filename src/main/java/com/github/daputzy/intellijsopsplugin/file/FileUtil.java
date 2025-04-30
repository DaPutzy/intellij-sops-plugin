package com.github.daputzy.intellijsopsplugin.file;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

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

	/**
	 * creates a temp file with the content and extension of the given virtual file
	 *
	 * @param file virtual file
	 * @return temp file
	 */
	public @NotNull File cloneContentToTempFile(@NotNull final VirtualFile file) {
		final File tempFile;
		try {
			tempFile = WriteAction.computeAndWait(() -> com.intellij.openapi.util.io.FileUtil.createTempFile(
				"intellij-sops-plugin-" + file.getNameWithoutExtension() + "-" + UUID.randomUUID(),
				'.' + file.getExtension(),
				true
			));
		} catch (final IOException e) {
			throw new RuntimeException("Could not create temp file.", e);
		}

		final String content = getContent(file);

		try {
			WriteAction.runAndWait(() -> com.intellij.openapi.util.io.FileUtil.writeToFile(
				tempFile,
				content,
				file.getCharset()
			));
		} catch (final IOException e) {
			throw new RuntimeException("Could not write to temp file.", e);
		}

		return tempFile;
	}
}
