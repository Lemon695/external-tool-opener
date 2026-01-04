package com.lemon.externaltool.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.lemon.externaltool.model.DetectedTool;
import com.lemon.externaltool.model.ExternalTool;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dialog for selecting which detected tools to add
 */
public class ToolSelectionDialog extends DialogWrapper {

    private final List<DetectedTool> newTools;
    private final List<DetectedTool> updatedTools;
    private final ToolTableModel tableModel;
    private JTable toolTable;
    private JCheckBox enableAllCheckBox;
    private JLabel summaryLabel;

    public ToolSelectionDialog(List<DetectedTool> newTools, List<DetectedTool> updatedTools) {
        super(true);
        this.newTools = newTools;
        this.updatedTools = updatedTools;
        this.tableModel = new ToolTableModel(newTools);

        setTitle("Tool Detection - Add New Tools");
        init();
        updateSummary();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(700, 500));

        // Top: Summary
        summaryLabel = new JLabel();
        summaryLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(summaryLabel, BorderLayout.NORTH);

        // Center: Tool table
        toolTable = new JTable(tableModel);
        toolTable.setRowHeight(60);
        toolTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Column widths
        toolTable.getColumnModel().getColumn(0).setPreferredWidth(40); // Checkbox
        toolTable.getColumnModel().getColumn(0).setMaxWidth(40);
        toolTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Name
        toolTable.getColumnModel().getColumn(2).setPreferredWidth(300); // Path
        toolTable.getColumnModel().getColumn(3).setPreferredWidth(160); // Extensions

        // Custom renderer for multiline cells
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JLabel) {
                    JLabel label = (JLabel) c;
                    label.setVerticalAlignment(SwingConstants.TOP);

                    // Add recommended badge
                    if (column == 1 && tableModel.isRecommended(row)) {
                        String text = value.toString();
                        label.setText("<html><b>" + text + "</b><br/><font color='green'>⭐ Recommended</font></html>");
                    } else if (value != null) {
                        label.setText("<html>" + value.toString().replace("\n", "<br/>") + "</html>");
                    }
                }
                return c;
            }
        };

        for (int i = 1; i < toolTable.getColumnCount(); i++) {
            toolTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JScrollPane scrollPane = new JScrollPane(toolTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom: Options and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Updated tools info
        if (!updatedTools.isEmpty()) {
            JPanel updatedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            updatedPanel.setBorder(BorderFactory.createTitledBorder("Updated Existing Tools"));

            StringBuilder updatedText = new StringBuilder("<html>");
            for (DetectedTool tool : updatedTools) {
                updatedText.append("• ").append(tool.getName()).append(" (path updated)<br/>");
            }
            updatedText.append("</html>");

            JLabel updatedLabel = new JLabel(updatedText.toString());
            updatedPanel.add(updatedLabel);
            bottomPanel.add(updatedPanel, BorderLayout.NORTH);
        }

        // Options
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton selectAllBtn = new JButton("Select All");
        selectAllBtn.addActionListener(e -> {
            tableModel.selectAll(true);
            updateSummary();
        });

        JButton selectNoneBtn = new JButton("Select None");
        selectNoneBtn.addActionListener(e -> {
            tableModel.selectAll(false);
            updateSummary();
        });

        JButton selectPopularBtn = new JButton("Select Popular");
        selectPopularBtn.addActionListener(e -> {
            tableModel.selectPopular();
            updateSummary();
        });

        enableAllCheckBox = new JCheckBox("Enable all added tools immediately", true);

        optionsPanel.add(selectAllBtn);
        optionsPanel.add(selectNoneBtn);
        optionsPanel.add(selectPopularBtn);
        optionsPanel.add(Box.createHorizontalStrut(20));
        optionsPanel.add(enableAllCheckBox);

        bottomPanel.add(optionsPanel, BorderLayout.SOUTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void updateSummary() {
        int selected = tableModel.getSelectedCount();
        int total = newTools.size();
        summaryLabel.setText(String.format(
                "<html><b>Detected %d new tool%s (%d selected)</b></html>",
                total, total == 1 ? "" : "s", selected));
    }

    /**
     * Get selected tools to add
     */
    public List<ExternalTool> getSelectedTools() {
        List<ExternalTool> tools = new ArrayList<>();
        boolean enableAll = enableAllCheckBox.isSelected();

        for (int i = 0; i < newTools.size(); i++) {
            if (tableModel.isSelected(i)) {
                DetectedTool detected = newTools.get(i);
                ExternalTool tool = new ExternalTool();
                tool.setName(detected.getName());
                tool.setExecutablePath(detected.getDetectedPath());
                tool.setEnabled(enableAll);

                // Set extensions
                List<String> extensions = detected.getDefinition().getExtensions();
                tool.setSupportedExtensions(extensions != null ? extensions : new ArrayList<>());

                tools.add(tool);
            }
        }

        return tools;
    }

    /**
     * Table model for tool selection
     */
    private class ToolTableModel extends AbstractTableModel {
        private final String[] columnNames = { "", "Tool Name", "Path", "Extensions" };
        private final List<DetectedTool> tools;
        private final boolean[] selected;

        public ToolTableModel(List<DetectedTool> tools) {
            this.tools = tools;
            this.selected = new boolean[tools.size()];

            // Smart pre-selection: select tools with priority >= 8
            for (int i = 0; i < tools.size(); i++) {
                selected[i] = tools.get(i).getDefinition().getPriority() >= 8;
            }
        }

        @Override
        public int getRowCount() {
            return tools.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            DetectedTool tool = tools.get(rowIndex);

            switch (columnIndex) {
                case 0:
                    return selected[rowIndex];
                case 1:
                    return tool.getName();
                case 2:
                    return tool.getDetectedPath();
                case 3:
                    List<String> exts = tool.getDefinition().getExtensions();
                    if (exts == null || exts.isEmpty()) {
                        return "All files";
                    }
                    return String.join(", ", exts);
                default:
                    return "";
            }
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                selected[rowIndex] = (Boolean) value;
                fireTableCellUpdated(rowIndex, columnIndex);
                updateSummary();
            }
        }

        public boolean isSelected(int row) {
            return selected[row];
        }

        public boolean isRecommended(int row) {
            return tools.get(row).getDefinition().getPriority() >= 8;
        }

        public int getSelectedCount() {
            int count = 0;
            for (boolean s : selected) {
                if (s)
                    count++;
            }
            return count;
        }

        public void selectAll(boolean select) {
            for (int i = 0; i < selected.length; i++) {
                selected[i] = select;
            }
            fireTableDataChanged();
        }

        public void selectPopular() {
            for (int i = 0; i < tools.size(); i++) {
                selected[i] = isRecommended(i);
            }
            fireTableDataChanged();
        }
    }
}
