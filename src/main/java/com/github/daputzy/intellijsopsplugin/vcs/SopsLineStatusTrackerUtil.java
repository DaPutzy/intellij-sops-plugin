package com.github.daputzy.intellijsopsplugin.vcs;

import java.util.Optional;

import com.github.daputzy.intellijsopsplugin.SopsProjectDisposable;
import com.github.daputzy.intellijsopsplugin.file.DecryptedSopsFileWithReference;
import com.github.daputzy.intellijsopsplugin.file.FileUtil;
import com.github.daputzy.intellijsopsplugin.sops.ExecutionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.ex.SimpleLocalLineStatusTracker;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Shows VCS change markers (gutter) in decrypted sops editors.
 * The base revision is the decrypted HEAD revision of the encrypted file,
 * or the decrypted content at the time of opening if there is no HEAD revision.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SopsLineStatusTrackerUtil {

	private static final Logger LOG = Logger.getInstance(SopsLineStatusTrackerUtil.class);

	@Getter(lazy = true)
	private static final SopsLineStatusTrackerUtil instance = new SopsLineStatusTrackerUtil();

	/**
	 * installs a line status tracker on the document of the decrypted in memory file,
	 * must be called on the EDT after the in memory file has been opened
	 *
	 * @param project          project
	 * @param original         original (encrypted) file
	 * @param inMemoryFile     decrypted in memory file
	 * @param decryptedContent decrypted content of the original file at the time of opening
	 */
	public void install(
		@NotNull final Project project,
		@NotNull final VirtualFile original,
		@NotNull final DecryptedSopsFileWithReference inMemoryFile,
		@NotNull final String decryptedContent
	) {
		final Document document = FileUtil.getInstance().getDocument(inMemoryFile);
		final SimpleLocalLineStatusTracker tracker = SimpleLocalLineStatusTracker.createTracker(project, document, inMemoryFile);

		final MessageBusConnection connection = project.getMessageBus().connect(SopsProjectDisposable.getInstance(project));

		// the tracker is not managed by the platform and must be released,
		Disposer.register(connection, tracker::release);

		connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
			@Override
			public void fileClosed(@NotNull final FileEditorManager source, @NotNull final VirtualFile closedFile) {
				if (inMemoryFile.equals(closedFile)) {
					connection.disconnect();
				}
			}
		});

		// show changes against the opened content right away, replaced by the decrypted HEAD once available
		tracker.setBaseRevision(decryptedContent);

		ApplicationManager.getApplication().executeOnPooledThread(() -> {
			try {
				getModifiedHeadContent(project, original).ifPresent(headContent ->
					ExecutionUtil.getInstance().decrypt(project, original, headContent, decryptedHead ->
						ApplicationManager.getApplication().invokeLater(() -> {
							if (!tracker.isReleased()) {
								tracker.setBaseRevision(decryptedHead);
							}
						})
					)
				);
			} catch (final VcsException | RuntimeException e) {
				LOG.warn("Could not determine HEAD revision of sops file " + original.getPath(), e);
			}
		});
	}

	/**
	 * @return the (encrypted) HEAD content if the file is modified compared to HEAD, otherwise empty
	 * (unmodified: HEAD equals the opened content; unversioned / newly added: there is no HEAD)
	 */
	private Optional<String> getModifiedHeadContent(
		@NotNull final Project project,
		@NotNull final VirtualFile original
	) throws VcsException {
		if (ProjectLevelVcsManager.getInstance(project).getVcsFor(original) == null) {
			return Optional.empty();
		}

		final Change change = ChangeListManager.getInstance(project).getChange(original);

		if (change == null || change.getBeforeRevision() == null) {
			return Optional.empty();
		}

		return Optional.ofNullable(change.getBeforeRevision().getContent());
	}
}
