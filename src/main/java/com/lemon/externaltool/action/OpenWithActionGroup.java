package com.lemon.externaltool.action;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.lemon.externaltool.model.ExternalTool;
import com.lemon.externaltool.service.ExternalToolService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Open With Action Group
 * "Open With..." Context Menu Action Group
 */
public class OpenWithActionGroup extends ActionGroup {

    private static final Logger LOG = Logger.getInstance(OpenWithActionGroup.class);

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        // Always return at least configure action to ensure menu is visible
        if (e == null) {
            return new AnAction[] { new ConfigureToolsAction() };
        }

        Project project = e.getProject();
        if (project == null) {
            return new AnAction[] { new ConfigureToolsAction() };
        }

        // --- ROBUST FILE RESOLUTION ---
        FileResolutionResult resolutionResult = resolveVirtualFile(e);
        VirtualFile file = resolutionResult.getResolvedFile();

        // --- ULTIMATE FALLBACK: FILE EDITOR MANAGER ---
        // If everything else fails, grab the currently selected file from the editor.
        // This is a "nuclear option" to ensure we almost always have a context.
        boolean usedFallback = false;
        if (file == null) {
            VirtualFile[] selectedFiles = FileEditorManager.getInstance(project).getSelectedFiles();
            if (selectedFiles.length > 0) {
                file = selectedFiles[0];
                usedFallback = true;
                resolutionResult.addStrategy("FALLBACK_EDITOR_SELECTION", file);
            }
        }

        // --- DIAGNOSTIC INFO CAPTURE ---
        String diagnosis = "";
        if (file == null) {
            diagnosis = "File detection failed (All resolution strategies returned null)";
            LOG.warn("File detection failed in place: " + e.getPlace());
        } else if (usedFallback) {
            diagnosis = "File resolved via Fallback (Editor Selection): " + file.getName();
        } else {
            diagnosis = "File resolved via Context: " + file.getName();
        }

        // --- GET TOOLS ONLY IF FILE EXISTS ---
        ExternalToolService service = ExternalToolService.getInstance();
        List<ExternalTool> tools = new ArrayList<>();
        if (file != null) {
            tools = service.getToolsForFile(file);
        }

        List<AnAction> actions = new ArrayList<>();

        // 1. Matched Tools
        if (!tools.isEmpty()) {
            for (ExternalTool tool : tools) {
                actions.add(new OpenWithSubAction(tool));
            }
            actions.add(Separator.getInstance());
        }

        // 2. Configuration Action
        actions.add(new ConfigureToolsAction());

        return actions.toArray(new AnAction[0]);
    }

    /**
     * Try EVERYTHING to get a VirtualFile from the event.
     * Returns a FileResolutionResult with diagnostic information.
     */
    @NotNull
    private FileResolutionResult resolveVirtualFile(AnActionEvent e) {
        FileResolutionResult result = new FileResolutionResult();

        try {
            // 1. CommonDataKeys.VIRTUAL_FILE
            VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
            result.addStrategy("VIRTUAL_FILE", file);
            if (file != null) {
                result.setResolvedFile(file);
                return result;
            }

            // 2. CommonDataKeys.VIRTUAL_FILE_ARRAY
            VirtualFile[] files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
            result.addStrategy("VIRTUAL_FILE_ARRAY", files != null && files.length > 0 ? files[0] : null);
            if (files != null && files.length > 0) {
                result.setResolvedFile(files[0]);
                return result;
            }

            // 3. PlatformDataKeys.VIRTUAL_FILE (Old API, just in case)
            file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
            result.addStrategy("PLATFORM_VIRTUAL_FILE", file);
            if (file != null) {
                result.setResolvedFile(file);
                return result;
            }

            // 4. CommonDataKeys.PSI_FILE
            com.intellij.psi.PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
            VirtualFile psiVirtualFile = psiFile != null ? psiFile.getVirtualFile() : null;
            result.addStrategy("PSI_FILE", psiVirtualFile);
            if (psiVirtualFile != null) {
                result.setResolvedFile(psiVirtualFile);
                return result;
            }

            // 5. LangDataKeys.PSI_FILE (Old API)
            psiFile = e.getData(LangDataKeys.PSI_FILE);
            psiVirtualFile = psiFile != null ? psiFile.getVirtualFile() : null;
            result.addStrategy("LANG_PSI_FILE", psiVirtualFile);
            if (psiVirtualFile != null) {
                result.setResolvedFile(psiVirtualFile);
                return result;
            }

            // 6. CommonDataKeys.PSI_ELEMENT (Might be a directory or file)
            PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
            VirtualFile psiElementFile = null;
            if (psiElement instanceof PsiFileSystemItem) {
                psiElementFile = ((PsiFileSystemItem) psiElement).getVirtualFile();
            }
            result.addStrategy("PSI_ELEMENT", psiElementFile);
            if (psiElementFile != null) {
                result.setResolvedFile(psiElementFile);
                return result;
            }

            // 7. Editor (If triggered from editor)
            com.intellij.openapi.editor.Editor editor = e.getData(CommonDataKeys.EDITOR);
            VirtualFile editorFile = null;
            if (editor != null && editor.getDocument() != null) {
                editorFile = com.intellij.openapi.fileEditor.FileDocumentManager.getInstance()
                        .getFile(editor.getDocument());
            }
            result.addStrategy("EDITOR", editorFile);
            if (editorFile != null) {
                result.setResolvedFile(editorFile);
                return result;
            }

            // 8. SELECTED_ITEMS (Project View Nodes) - THIS IS THE KEY FOR PROJECT VIEW!
            // In ProjectViewPopup, IntelliJ uses SELECTED_ITEMS with PsiFileNode objects
            Object[] selectedItems = e.getData(PlatformCoreDataKeys.SELECTED_ITEMS);
            VirtualFile selectedItemFile = null;
            if (selectedItems != null && selectedItems.length > 0) {
                Object firstItem = selectedItems[0];
                // PsiFileNode is a common type in project view
                if (firstItem instanceof com.intellij.ide.util.treeView.AbstractTreeNode) {
                    com.intellij.ide.util.treeView.AbstractTreeNode<?> treeNode = (com.intellij.ide.util.treeView.AbstractTreeNode<?>) firstItem;
                    Object value = treeNode.getValue();
                    if (value instanceof PsiFileSystemItem) {
                        selectedItemFile = ((PsiFileSystemItem) value).getVirtualFile();
                    } else if (value instanceof VirtualFile) {
                        selectedItemFile = (VirtualFile) value;
                    }
                }
            }
            result.addStrategy("SELECTED_ITEMS", selectedItemFile);
            if (selectedItemFile != null) {
                result.setResolvedFile(selectedItemFile);
                return result;
            }

            // 9. NAVIGATABLE_ARRAY (Alternative approach)
            // This is crucial for ProjectViewPopup context where other strategies often
            // fail
            com.intellij.pom.Navigatable[] navigatables = e.getData(PlatformCoreDataKeys.NAVIGATABLE_ARRAY);
            VirtualFile navigatableFile = null;
            if (navigatables != null && navigatables.length > 0) {
                com.intellij.pom.Navigatable nav = navigatables[0];
                if (nav instanceof com.intellij.openapi.fileEditor.OpenFileDescriptor) {
                    navigatableFile = ((com.intellij.openapi.fileEditor.OpenFileDescriptor) nav).getFile();
                } else if (nav instanceof PsiElement) {
                    PsiElement psi = (PsiElement) nav;
                    if (psi instanceof PsiFileSystemItem) {
                        navigatableFile = ((PsiFileSystemItem) psi).getVirtualFile();
                    } else if (psi.getContainingFile() != null) {
                        navigatableFile = psi.getContainingFile().getVirtualFile();
                    }
                }
            }
            result.addStrategy("NAVIGATABLE_ARRAY", navigatableFile);
            if (navigatableFile != null) {
                result.setResolvedFile(navigatableFile);
                return result;
            }
        } catch (Exception ex) {
            // Check for weird exceptions during data access
            LOG.warn("Error resolving file", ex);
            result.setException(ex);
        }

        return result; // No file found
    }

    /**
     * Inner class to hold file resolution results and diagnostics
     */
    private static class FileResolutionResult {
        private VirtualFile resolvedFile = null;
        private final List<String> strategyResults = new ArrayList<>();
        private Exception exception = null;

        public void addStrategy(String strategyName, VirtualFile result) {
            String resultStr = result != null ? result.getName() : "null";
            strategyResults.add(strategyName + ": " + resultStr);
        }

        public void setResolvedFile(VirtualFile file) {
            this.resolvedFile = file;
        }

        public VirtualFile getResolvedFile() {
            return resolvedFile;
        }

        public List<String> getStrategyResults() {
            return strategyResults;
        }

        public void setException(Exception ex) {
            this.exception = ex;
        }

        public Exception getException() {
            return exception;
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        boolean visible = project != null;
        e.getPresentation().setVisible(visible);
        e.getPresentation().setEnabled(visible);
        e.getPresentation().setDisableGroupIfEmpty(false);
    }

    private static class ConfigureToolsAction extends AnAction {
        public ConfigureToolsAction() {
            super("Configure External Tools...");
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = e.getProject();
            if (project != null) {
                com.intellij.openapi.options.ShowSettingsUtil.getInstance()
                        .showSettingsDialog(project, "External Tool Opener");
            }
        }
    }
}
