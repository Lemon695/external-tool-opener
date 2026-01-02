package com.lemon.externaltool.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.lemon.externaltool.model.ExternalTool;
import com.lemon.externaltool.service.ExternalToolService;
import com.lemon.externaltool.util.ProcessExecutor;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Tool Config Panel
 * 工具配置界面面板
 */
public class ToolConfigPanel {
    
    private final Project project;
    private final ExternalToolService service;
    
    private JPanel mainPanel;
    private JBList<ExternalTool> toolList;
    private DefaultListModel<ExternalTool> listModel;
    
    // 编辑区域组件
    private JTextField nameField;
    private TextFieldWithBrowseButton pathField;
    private JTextArea extensionsArea;
    private JTextField commandTemplateField;
    private JCheckBox enabledCheckBox;
    private JCheckBox defaultCheckBox;
    private JButton testButton;
    
    private List<ExternalTool> workingTools;
    private ExternalTool selectedTool;
    private boolean modified = false;
    
    public ToolConfigPanel(Project project) {
        this.project = project;
        this.service = ExternalToolService.getInstance(project);
        initUI();
        loadTools();
    }
    
    /**
     * 初始化UI
     */
    private void initUI() {
        mainPanel = new JPanel(new BorderLayout());
        
        // 左侧：工具列表
        JPanel leftPanel = createLeftPanel();
        
        // 右侧：编辑区域
        JPanel rightPanel = createRightPanel();
        
        // 使用分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(250);
        splitPane.setResizeWeight(0.3);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
    }
    
    /**
     * 创建左侧工具列表面板
     */
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 工具列表
        listModel = new DefaultListModel<>();
        toolList = new JBList<>(listModel);
        toolList.setCellRenderer(new ToolListCellRenderer());
        toolList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 监听选择变化
        toolList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    saveCurrentTool();
                    loadSelectedTool();
                }
            }
        });
        
        // 工具栏装饰器
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(toolList)
                .setAddAction(button -> addTool())
                .setRemoveAction(button -> removeTool())
                .setMoveUpAction(button -> moveToolUp())
                .setMoveDownAction(button -> moveToolDown());
        
        panel.add(decorator.createPanel(), BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建右侧编辑区域面板
     */
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 工具名称
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        nameField = new JTextField();
        nameField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> modified = true));
        formPanel.add(nameField, gbc);
        
        // 可执行文件路径
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Path:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        pathField = new TextFieldWithBrowseButton();
        pathField.addBrowseFolderListener(
                "Select Executable",
                "Choose the executable file for this tool",
                project,
                new FileChooserDescriptor(true, false, false, false, false, false)
        );
        pathField.getTextField().getDocument().addDocumentListener(new SimpleDocumentListener(() -> modified = true));
        formPanel.add(pathField, gbc);
        
        // 支持的扩展名
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formPanel.add(new JLabel("Extensions:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        extensionsArea = new JTextArea(3, 20);
        extensionsArea.setLineWrap(true);
        extensionsArea.setWrapStyleWord(true);
        extensionsArea.getDocument().addDocumentListener(new SimpleDocumentListener(() -> modified = true));
        JBScrollPane extensionsScroll = new JBScrollPane(extensionsArea);
        formPanel.add(extensionsScroll, gbc);
        
        gbc.gridx = 1; gbc.gridy = 3;
        JLabel extensionsHint = new JLabel("<html><i>Comma-separated list, e.g., .md, .markdown<br>Leave empty to support all file types</i></html>");
        extensionsHint.setFont(extensionsHint.getFont().deriveFont(Font.PLAIN, 11f));
        formPanel.add(extensionsHint, gbc);
        
        // 命令模板
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        formPanel.add(new JLabel("Command Template:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        commandTemplateField = new JTextField();
        commandTemplateField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> modified = true));
        formPanel.add(commandTemplateField, gbc);
        
        gbc.gridx = 1; gbc.gridy = 5;
        JLabel cmdHint = new JLabel("<html><i>Variables: {path}, {file}, {fileDir}, {fileName}<br>Default: \"{path}\" \"{file}\"</i></html>");
        cmdHint.setFont(cmdHint.getFont().deriveFont(Font.PLAIN, 11f));
        formPanel.add(cmdHint, gbc);
        
        // 选项
        gbc.gridx = 1; gbc.gridy = 6;
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        enabledCheckBox = new JCheckBox("Enabled");
        enabledCheckBox.addActionListener(e -> modified = true);
        optionsPanel.add(enabledCheckBox);
        
        defaultCheckBox = new JCheckBox("Set as default for these extensions");
        defaultCheckBox.addActionListener(e -> modified = true);
        optionsPanel.add(defaultCheckBox);
        
        formPanel.add(optionsPanel, gbc);
        
        // 测试按钮
        gbc.gridx = 1; gbc.gridy = 7;
        testButton = new JButton("Test Tool");
        testButton.addActionListener(e -> testCurrentTool());
        formPanel.add(testButton, gbc);
        
        panel.add(formPanel, BorderLayout.NORTH);
        
        return panel;
    }
    
    /**
     * 加载工具列表
     */
    private void loadTools() {
        workingTools = new ArrayList<>(service.getAllTools());
        updateToolList();
    }
    
    /**
     * 更新工具列表显示
     */
    private void updateToolList() {
        listModel.clear();
        for (ExternalTool tool : workingTools) {
            listModel.addElement(tool);
        }
    }
    
    /**
     * 加载选中的工具
     */
    private void loadSelectedTool() {
        selectedTool = toolList.getSelectedValue();
        
        if (selectedTool != null) {
            nameField.setText(selectedTool.getName());
            pathField.setText(selectedTool.getExecutablePath());
            extensionsArea.setText(String.join(", ", selectedTool.getSupportedExtensions()));
            commandTemplateField.setText(selectedTool.getCommandTemplate());
            enabledCheckBox.setSelected(selectedTool.isEnabled());
            defaultCheckBox.setSelected(selectedTool.isDefault());
            
            setEditingEnabled(true);
        } else {
            clearEditing();
            setEditingEnabled(false);
        }
    }
    
    /**
     * 保存当前编辑的工具
     */
    private void saveCurrentTool() {
        if (selectedTool != null) {
            selectedTool.setName(nameField.getText().trim());
            selectedTool.setExecutablePath(pathField.getText().trim());
            
            // 解析扩展名列表
            String[] extensions = extensionsArea.getText().split(",");
            List<String> extList = new ArrayList<>();
            for (String ext : extensions) {
                String trimmed = ext.trim();
                if (!trimmed.isEmpty()) {
                    extList.add(trimmed);
                }
            }
            selectedTool.setSupportedExtensions(extList);
            
            selectedTool.setCommandTemplate(commandTemplateField.getText().trim());
            selectedTool.setEnabled(enabledCheckBox.isSelected());
            selectedTool.setDefault(defaultCheckBox.isSelected());
            
            // 更新列表显示
            int index = toolList.getSelectedIndex();
            if (index >= 0) {
                listModel.set(index, selectedTool);
            }
        }
    }
    
    /**
     * 清空编辑区域
     */
    private void clearEditing() {
        nameField.setText("");
        pathField.setText("");
        extensionsArea.setText("");
        commandTemplateField.setText("\"{path}\" \"{file}\"");
        enabledCheckBox.setSelected(true);
        defaultCheckBox.setSelected(false);
    }
    
    /**
     * 设置编辑区域是否可用
     */
    private void setEditingEnabled(boolean enabled) {
        nameField.setEnabled(enabled);
        pathField.setEnabled(enabled);
        extensionsArea.setEnabled(enabled);
        commandTemplateField.setEnabled(enabled);
        enabledCheckBox.setEnabled(enabled);
        defaultCheckBox.setEnabled(enabled);
        testButton.setEnabled(enabled);
    }
    
    /**
     * 添加工具
     */
    private void addTool() {
        ExternalTool newTool = new ExternalTool();
        newTool.setName("New Tool");
        newTool.setExecutablePath("");
        newTool.setSortOrder(workingTools.size());
        
        workingTools.add(newTool);
        listModel.addElement(newTool);
        toolList.setSelectedIndex(listModel.getSize() - 1);
        modified = true;
    }
    
    /**
     * 删除工具
     */
    private void removeTool() {
        int selectedIndex = toolList.getSelectedIndex();
        if (selectedIndex >= 0) {
            workingTools.remove(selectedIndex);
            listModel.remove(selectedIndex);
            modified = true;
        }
    }
    
    /**
     * 上移工具
     */
    private void moveToolUp() {
        int selectedIndex = toolList.getSelectedIndex();
        if (selectedIndex > 0) {
            ExternalTool tool = workingTools.remove(selectedIndex);
            workingTools.add(selectedIndex - 1, tool);
            updateToolList();
            toolList.setSelectedIndex(selectedIndex - 1);
            modified = true;
        }
    }
    
    /**
     * 下移工具
     */
    private void moveToolDown() {
        int selectedIndex = toolList.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < workingTools.size() - 1) {
            ExternalTool tool = workingTools.remove(selectedIndex);
            workingTools.add(selectedIndex + 1, tool);
            updateToolList();
            toolList.setSelectedIndex(selectedIndex + 1);
            modified = true;
        }
    }
    
    /**
     * 测试当前工具
     */
    private void testCurrentTool() {
        if (selectedTool == null) {
            return;
        }
        
        saveCurrentTool();
        
        boolean isValid = ProcessExecutor.testTool(selectedTool);
        
        String message = isValid 
                ? "Tool is configured correctly and ready to use!" 
                : "Tool executable not found or not accessible!";
        
        int messageType = isValid ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE;
        
        JOptionPane.showMessageDialog(mainPanel, message, "Test Tool", messageType);
    }
    
    /**
     * 应用更改
     */
    public void apply() {
        saveCurrentTool();
        
        // 更新sortOrder
        for (int i = 0; i < workingTools.size(); i++) {
            workingTools.get(i).setSortOrder(i);
        }
        
        // 清空现有配置
        for (ExternalTool tool : service.getAllTools()) {
            service.deleteToolConfig(tool.getId());
        }
        
        // 保存所有工具
        for (ExternalTool tool : workingTools) {
            service.saveToolConfig(tool);
        }
        
        modified = false;
    }
    
    /**
     * 重置更改
     */
    public void reset() {
        loadTools();
        if (!workingTools.isEmpty()) {
            toolList.setSelectedIndex(0);
        }
        modified = false;
    }
    
    /**
     * 是否已修改
     */
    public boolean isModified() {
        return modified;
    }
    
    /**
     * 获取主面板
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }
    
    /**
     * 工具列表渲染器
     */
    private static class ToolListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                                                     int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof ExternalTool) {
                ExternalTool tool = (ExternalTool) value;
                String text = tool.getName();
                
                if (!tool.isEnabled()) {
                    text += " (disabled)";
                }
                if (tool.isDefault()) {
                    text += " [default]";
                }
                
                setText(text);
            }
            
            return this;
        }
    }
    
    /**
     * 简单的文档监听器
     */
    private static class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final Runnable callback;
        
        public SimpleDocumentListener(Runnable callback) {
            this.callback = callback;
        }
        
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            callback.run();
        }
        
        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            callback.run();
        }
        
        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            callback.run();
        }
    }
}
