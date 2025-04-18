package se.isselab.HAnS.trafficLight;

import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.ShowLogAction;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.GridBag;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Map;
import java.util.Set;

public class HansTrafficLightPanel {
    private static final String NO_FINDINGS_TEXT = "File is not mapped.";
    private static final String METRICS_ERROR_MSG = "Error while fetching mapping.";
    private static final String FEATURE_FILE_MAPPINGS = "Mapped features through *.feature-file";
    private static final String FEATURE_FOLDER_MAPPINGS = "Mapped features through *.feature-folder";

    JPanel panel = new JPanel(new GridBagLayout());

    private final JBLabel findingsSummaryLabel = new JBLabel(NO_FINDINGS_TEXT);

    private final JPanel fileMappingsPanel = new JPanel(new GridBagLayout());
    private final JPanel folderMappingsPanel = new JPanel(new GridBagLayout());

    private final Editor editor;

    public HansTrafficLightPanel(Editor editor) {
        this.editor = editor;

        ActionButton menuButton =
                new ActionButton(
                        new MenuAction(),
                        null,
                        ActionPlaces.EDITOR_POPUP,
                        ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE);

        GridBag gc = new GridBag();
        gc.nextLine().next().anchor(GridBagConstraints.LINE_START).weightx(1.0).fillCellHorizontally().insets(10, 10, 10, 10);

        JBLabel hansCrashed = new JBLabel(METRICS_ERROR_MSG);
        hansCrashed.setVisible(false);
        panel.add(hansCrashed, gc);

        panel.add(findingsSummaryLabel, gc);

        panel.add(menuButton, gc.next().anchor(GridBagConstraints.LINE_END).weightx(0.0).insets(10, 6, 10, 6));

        panel.add(fileMappingsPanel, gc.nextLine().next().anchor(GridBagConstraints.LINE_START).fillCellHorizontally().insets(0, 6, 10, 6));
        panel.add(folderMappingsPanel, gc.nextLine().next().anchor(GridBagConstraints.LINE_START).fillCellHorizontally().insets(0, 6, 10, 6));
    }

    private void handleIfAlive(Boolean isAlive) {
        findingsSummaryLabel.setVisible(isAlive);
    }

    protected void refresh(HansTrafficLightDashboardModel model) {
        handleIfAlive(model.isAlive());
        if (!model.hasFindings()) {
            findingsSummaryLabel.setText(NO_FINDINGS_TEXT);
            fileMappingsPanel.setVisible(false);
            folderMappingsPanel.setVisible(false);
        } else {
            findingsSummaryLabel.setText("Number of features connected to file: " + model.findingsCount());
            populateMappings(fileMappingsPanel, model.getFilePathsFeatureFileMapping(), FEATURE_FILE_MAPPINGS);
            populateMappings(folderMappingsPanel, model.getFilePathsFeatureFolderMapping(), FEATURE_FOLDER_MAPPINGS);
        }
        fileMappingsPanel.setBorder(BorderFactory.createLineBorder(JBColor.RED));
        folderMappingsPanel.setBorder(BorderFactory.createLineBorder(JBColor.BLUE));
        panel.revalidate();
        panel.repaint();
    }

    private void populateMappings(JPanel mappingsPanel, Map<String, Set<String>> mappings, String headerText) {
        GridBag gc = new GridBag();

        if (mappings != null && !mappings.isEmpty()) {
            JBLabel headerLabel = new JBLabel(headerText);
            headerLabel.setFont(new Font(null, Font.BOLD, 12));
            mappingsPanel.add(headerLabel, gc.nextLine().next().anchor(GridBagConstraints.LINE_START).insets(5, 10, 5, 10));
            mappings.forEach((path, features) -> features.forEach(feature -> {
                HyperlinkLabel featureLink = new HyperlinkLabel(feature);
                featureLink.addHyperlinkListener(e -> openLink(path));
                mappingsPanel.add(featureLink, gc.nextLine().next().anchor(GridBagConstraints.LINE_START).insets(5, 10, 5, 10));
            }));
        }
    }

    private void openLink(String filePath) {
        VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(filePath));
        if (virtualFile != null && this.editor.getProject() != null) {
            FileEditorManager.getInstance(this.editor.getProject()).openFile(virtualFile, true);
        } else {
            JOptionPane.showMessageDialog(null, "File not found: " + filePath);
        }
    }

    private static class MenuAction extends DefaultActionGroup implements HintManagerImpl.ActionToIgnore {
        public MenuAction() {
            add(new ShowLogAction());
            getTemplatePresentation().setPopupGroup(true);
            getTemplatePresentation().setIcon(AllIcons.Actions.More);
            getTemplatePresentation().putClientProperty(ActionButton.HIDE_DROPDOWN_ICON, true);
        }
    }
}
