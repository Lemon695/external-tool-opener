package com.lemon.externaltool.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.lemon.externaltool.model.ExternalTool;
import com.lemon.externaltool.service.ExternalToolService;
import com.lemon.externaltool.ui.ToolValidator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tool Config Panel
 * 采用 MVC 模式重构的配置面板
 * State Management:
 * - workingTools: deep copy of the service state.
 * - GUI fields: strictly bound to the currently selected item in workingTools.
 * - Modifications: Updates are written immediately to the selected tool object.
 */
public class ToolConfigPanel {

    private final ExternalToolService service;

    private JPanel mainPanel;
    private JBList<ExternalTool> toolList;
    private DefaultListModel<ExternalTool> listModel;

    // Right-side Form Components
    private JTextField nameField;
    private TextFieldWithBrowseButton pathField;
    private JTextArea extensionsArea;
    private JTextField commandTemplateField;
    private JCheckBox enabledCheckBox;
    private JCheckBox defaultCheckBox;
    private JButton testButton;
    private JLabel errorLabel;

    // State
    private List<ExternalTool> workingTools; // The master list we are editing
    private boolean isModified = false;
    private boolean isUpdatingUI = false; // Guard flag to prevent loop updates
    private JButton detectButton; // Detect tools button

    public ToolConfigPanel() {
        this.service = ExternalToolService.getInstance();
        initUI();
        // Initial load happens on reset() which is called by IDE
    }

    private void initUI() {
        mainPanel = new JPanel(new BorderLayout());

        // Left: Tool List
        JPanel leftPanel = createLeftPanel();

        // Right: Edit Form
        JPanel rightPanel = createRightPanel();

        // Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(250);
        splitPane.setResizeWeight(0.3);

        mainPanel.add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        listModel = new DefaultListModel<>();
        toolList = new JBList<>(listModel);
        toolList.setCellRenderer(new ToolListCellRenderer());
        toolList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Selection Listener: Just switch the view, data is already saved in object
        toolList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateRightPanel();
            }
        });

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(toolList)
                .setAddAction(button -> addTool())
                .setRemoveAction(button -> removeTool())
                .setMoveUpAction(button -> moveToolUp())
                .setMoveDownAction(button -> moveToolDown());

        panel.add(decorator.createPanel(), BorderLayout.CENTER);
        
        // Add Detect Tools button at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        detectButton = new JButton("Detect Tools");
        detectButton.setToolTipText("Automatically detect external tools on your system");
        detectButton.addActionListener(e -> detectTools());
        buttonPanel.add(detectButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 1. Name
        addLabel(formPanel, gbc, 0, "Name:");
        nameField = new JTextField();
        bindField(nameField);
        addControl(formPanel, gbc, 0, nameField);

        // 2. Path
        addLabel(formPanel, gbc, 1, "Path:");
        pathField = new TextFieldWithBrowseButton();
        setupFileChooser(pathField);
        bindField(pathField.getTextField());
        addControl(formPanel, gbc, 1, pathField);

        // 3. Extensions
        addLabel(formPanel, gbc, 2, "Extensions:");
        extensionsArea = new JTextArea(3, 20);
        extensionsArea.setLineWrap(true);
        extensionsArea.setWrapStyleWord(true);
        bindField(extensionsArea);
        JBScrollPane extScroll = new JBScrollPane(extensionsArea);
        addControl(formPanel, gbc, 2, extScroll);

        addHint(formPanel, gbc, 3, "<html><i>Comma-separated (e.g. .md, .txt). Empty = All files.</i></html>");

        // 4. Command
        addLabel(formPanel, gbc, 4, "Command:");
        commandTemplateField = new JTextField();
        bindField(commandTemplateField);
        addControl(formPanel, gbc, 4, commandTemplateField);

        addHint(formPanel, gbc, 5,
                "<html><i>Vars: {path}, {file}, {fileDir}. Default: \"{path}\" \"{file}\"</i></html>");

        // 5. Options
        gbc.gridx = 1;
        gbc.gridy = 6;
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        enabledCheckBox = new JCheckBox("Enabled");
        enabledCheckBox.addActionListener(e -> updateCurrentToolModel());
        optionsPanel.add(enabledCheckBox);
        optionsPanel.add(Box.createHorizontalStrut(15));

        defaultCheckBox = new JCheckBox("Default");
        defaultCheckBox.addActionListener(e -> updateCurrentToolModel());
        optionsPanel.add(defaultCheckBox);

        formPanel.add(optionsPanel, gbc);

        // 6. Buttons & Validation
        gbc.gridy = 7;
        testButton = new JButton("Test Tool");
        testButton.addActionListener(e -> testTool());
        formPanel.add(testButton, gbc);

        gbc.gridy = 8;
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        formPanel.add(errorLabel, gbc);

        // Push everything up
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(formPanel, BorderLayout.NORTH);
        panel.add(wrapper, BorderLayout.CENTER);

        return panel;
    }

    // --- Data Binding & State Management ---

    private void updateRightPanel() {
        isUpdatingUI = true;
        try {
            ExternalTool selected = toolList.getSelectedValue();
            boolean hasSelection = selected != null;

            nameField.setEnabled(hasSelection);
            pathField.setEnabled(hasSelection);
            extensionsArea.setEnabled(hasSelection);
            commandTemplateField.setEnabled(hasSelection);
            enabledCheckBox.setEnabled(hasSelection);
            defaultCheckBox.setEnabled(hasSelection);
            testButton.setEnabled(hasSelection);

            if (hasSelection) {
                nameField.setText(selected.getName());
                pathField.setText(selected.getExecutablePath());
                extensionsArea.setText(String.join(", ", selected.getSupportedExtensions()));
                commandTemplateField.setText(selected.getCommandTemplate());
                enabledCheckBox.setSelected(selected.isEnabled());
                defaultCheckBox.setSelected(selected.isDefault());
                validateForm(selected);
            } else {
                nameField.setText("");
                pathField.setText("");
                extensionsArea.setText("");
                commandTemplateField.setText("");
                enabledCheckBox.setSelected(false);
                defaultCheckBox.setSelected(false);
                errorLabel.setText(" ");
            }
        } finally {
            isUpdatingUI = false;
        }
    }

    private void updateCurrentToolModel() {
        if (isUpdatingUI)
            return;

        ExternalTool selected = toolList.getSelectedValue();
        if (selected == null)
            return;

        // Update Model
        selected.setName(nameField.getText().trim());
        selected.setExecutablePath(pathField.getText().trim());
        selected.setCommandTemplate(commandTemplateField.getText().trim());
        selected.setEnabled(enabledCheckBox.isSelected());
        selected.setDefault(defaultCheckBox.isSelected());

        // Parse extensions
        List<String> exts = new ArrayList<>();
        String[] tokens = extensionsArea.getText().split("[,;\\s]+");
        for (String t : tokens) {
            String clean = t.trim();
            if (!clean.isEmpty())
                exts.add(clean);
        }
        selected.setSupportedExtensions(exts);

        // Mark as modified
        isModified = true;

        // Validate
        validateForm(selected);

        // Repaint list to show name changes
        toolList.repaint();
    }

    private void validateForm(ExternalTool tool) {
        ToolValidator.ValidationResult result = ToolValidator.validate(tool);
        if (result.isValid) {
            errorLabel.setText(" ");
            testButton.setEnabled(true);
        } else {
            errorLabel.setText(result.message);
            testButton.setEnabled(false);
        }
    }

    // --- Actions ---

    private void addTool() {
        ExternalTool newTool = new ExternalTool();
        newTool.setName("New Tool");
        newTool.setExecutablePath("");

        workingTools.add(newTool);
        listModel.addElement(newTool);
        toolList.setSelectedValue(newTool, true);
        isModified = true;
    }

    private void removeTool() {
        int idx = toolList.getSelectedIndex();
        if (idx != -1) {
            workingTools.remove(idx);
            listModel.remove(idx);
            isModified = true;

            if (idx < listModel.getSize()) {
                toolList.setSelectedIndex(idx);
            } else if (!listModel.isEmpty()) {
                toolList.setSelectedIndex(listModel.getSize() - 1);
            }
        }
    }

    private void moveToolUp() {
        int idx = toolList.getSelectedIndex();
        if (idx > 0) {
            ExternalTool tool = workingTools.remove(idx);
            workingTools.add(idx - 1, tool);

            listModel.remove(idx);
            listModel.add(idx - 1, tool);

            toolList.setSelectedIndex(idx - 1);
            isModified = true;
        }
    }

    private void moveToolDown() {
        int idx = toolList.getSelectedIndex();
        if (idx != -1 && idx < listModel.getSize() - 1) {
            ExternalTool tool = workingTools.remove(idx);
            workingTools.add(idx + 1, tool);

            listModel.remove(idx);
            listModel.add(idx + 1, tool);

            toolList.setSelectedIndex(idx + 1);
            isModified = true;
        }
    }

    private void testTool() {
        ExternalTool selected = toolList.getSelectedValue();
        if (selected == null)
            return;

        String path = selected.getExecutablePath();
        File f = new File(path);

        boolean isMacApp = path.endsWith(".app") && System.getProperty("os.name").toLowerCase().contains("mac");
        boolean exists;

        if (isMacApp) {
            exists = f.exists() && f.isDirectory();
        } else {
            exists = f.exists() && f.isFile();
        }

        if (exists) {
            JOptionPane.showMessageDialog(mainPanel, "Path is valid!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(mainPanel, "Executable/Bundle not found!", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Configurable Contract ---

    public boolean isModified() {
        return isModified;
    }

    public void apply() {
        // Deep copy back to service
        List<ExternalTool> toSave = new ArrayList<>();
        for (int i = 0; i < workingTools.size(); i++) {
            ExternalTool t = workingTools.get(i).clone();
            t.setSortOrder(i);
            toSave.add(t);
        }
        service.setTools(toSave);
        isModified = false;
    }

    public void reset() {
        workingTools = new ArrayList<>();
        listModel.clear();

        for (ExternalTool tool : service.getAllTools()) {
            ExternalTool clone = tool.clone();
            workingTools.add(clone);
            listModel.addElement(clone);
        }

        if (!listModel.isEmpty()) {
            toolList.setSelectedIndex(0);
        } else {
            updateRightPanel(); // clear
        }
        isModified = false;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    // --- Helpers ---

    private void addLabel(JPanel p, GridBagConstraints gbc, int row, String text) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        p.add(new JLabel(text), gbc);
    }

    private void addControl(JPanel p, GridBagConstraints gbc, int row, JComponent comp) {
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        p.add(comp, gbc);
    }

    private void addHint(JPanel p, GridBagConstraints gbc, int row, String html) {
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        JLabel l = new JLabel(html);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 11f));
        l.setForeground(Color.GRAY);
        p.add(l, gbc);
    }

    private void bindField(javax.swing.text.JTextComponent field) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateCurrentToolModel();
            }

            public void removeUpdate(DocumentEvent e) {
                updateCurrentToolModel();
            }

            public void changedUpdate(DocumentEvent e) {
                updateCurrentToolModel();
            }
        });
    }

    private void setupFileChooser(TextFieldWithBrowseButton field) {
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, true, false, false, false, false) {
            @Override
            public boolean isFileSelectable(com.intellij.openapi.vfs.VirtualFile file) {
                // Allow matching files AND directories if they are .app
                if (file.isDirectory()) {
                    return file.getName().endsWith(".app");
                }
                return true;
            }
        };
        field.addBrowseFolderListener("Select Executable", "Choose tool executable or macOS .app bundle", null,
                descriptor);
    }

    private static class ToolListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ExternalTool) {
                ExternalTool t = (ExternalTool) value;
                String txt = t.getName();
                if (!t.isEnabled())
                    txt += " (Disabled)";
                if (t.isDefault())
                    txt += " [Default]";
                setText(txt);
            }
            return this;
        }
    }

    /**
     * Detect tools automatically
     */
    private void detectTools() {
        detectButton.setEnabled(false);
        detectButton.setText("Detecting...");
        SwingUtilities.invokeLater(() -> {
            try {
                com.lemon.externaltool.service.ToolDetectionService detectionService = new com.lemon.externaltool.service.ToolDetectionService();
                List<com.lemon.externaltool.model.DetectedTool> detected = detectionService.detectAvailableTools();
                List<com.lemon.externaltool.model.DetectedTool> newTools = new ArrayList<>();
                List<com.lemon.externaltool.model.DetectedTool> updatedTools = new ArrayList<>();
                for (com.lemon.externaltool.model.DetectedTool tool : detected) {
                    if (!tool.isAvailable()) continue;
                    boolean exists = workingTools.stream().anyMatch(t -> t.getName().equalsIgnoreCase(tool.getName()));
                    if (exists) {
                        updatedTools.add(tool);
                    } else {
                        newTools.add(tool);
                    }
                }
                if (newTools.isEmpty() && updatedTools.isEmpty()) {
                    JOptionPane.showMessageDialog(mainPanel, "No new tools detected. All available tools are already configured.", "Tool Detection", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                for (com.lemon.externaltool.model.DetectedTool updated : updatedTools) {
                    for (ExternalTool existing : workingTools) {
                        if (existing.getName().equalsIgnoreCase(updated.getName())) {
                            existing.setExecutablePath(updated.getDetectedPath());
                            break;
                        }
                    }
                }
                if (!newTools.isEmpty()) {
                    ToolSelectionDialog dialog = new ToolSelectionDialog(newTools, updatedTools);
                    if (dialog.showAndGet()) {
                        List<ExternalTool> selectedTools = dialog.getSelectedTools();
                        for (ExternalTool tool : selectedTools) {
                            workingTools.add(tool);
                            listModel.addElement(tool);
                        }
                        if (!selectedTools.isEmpty()) {
                            toolList.setSelectedIndex(workingTools.size() - 1);
                            isModified = true;
                        }
                        String updateMsg = updatedTools.isEmpty() ? "" : String.format(" Updated %d existing tool%s.", updatedTools.size(), updatedTools.size() == 1 ? "" : "s");
                        String message = String.format("Successfully added %d tool%s.%s", selectedTools.size(), selectedTools.size() == 1 ? "" : "s", updateMsg);
                        JOptionPane.showMessageDialog(mainPanel, message, "Tool Detection Complete", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else if (!updatedTools.isEmpty()) {
                    isModified = true;
                    toolList.repaint();
                    JOptionPane.showMessageDialog(mainPanel, String.format("Updated %d existing tool%s.", updatedTools.size(), updatedTools.size() == 1 ? "" : "s"), "Tool Detection Complete", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel, "Detection failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } finally {
                detectButton.setEnabled(true);
                detectButton.setText("Detect Tools");
            }
        });
    }

    }
