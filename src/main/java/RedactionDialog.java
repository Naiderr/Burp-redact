import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/*[*]
 - the popup dialog for managing redaction rules.
 - opened from Burp menu: Extensions → Redact → Configure Rules
*/
public class RedactionDialog extends JDialog 
{

    private final List<RedactionRule> rules;
    private final List<Runnable> refreshCallbacks;
    private final DefaultTableModel tableModel;

    // [*] constructor 
    public RedactionDialog(List<RedactionRule> rules, List<Runnable> refreshCallbacks) 
    {
        this.rules = rules;
        this.refreshCallbacks = refreshCallbacks;

        setTitle("Redaction Rules");
        setSize(520, 420);
        setLocationRelativeTo(null); 
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE); 
        setLayout(new BorderLayout(10, 10));

        // [/] tOP PANEL
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add New Rule"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // [/] row 1: Field Name
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Field Name:"), gbc);

        JTextField nameField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        inputPanel.add(nameField, gbc);

        // [/] row 2: Type dropdown
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        inputPanel.add(new JLabel("Type:"), gbc);

        JComboBox<RedactionRule.RuleType> typeCombo = new JComboBox<>(RedactionRule.RuleType.values());
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        inputPanel.add(typeCombo, gbc);

        // [/] row 3: Add button
        JButton addButton = new JButton("+ Add Rule");
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(addButton, gbc);

        add(inputPanel, BorderLayout.NORTH);

        // [/] cENTER: Rules table
        String[] columns = {"Name", "Type"};
        tableModel = new DefaultTableModel(columns, 0) 
        {
            @Override
            public boolean isCellEditable(int row, int column) 
            {
                return false; // [/] read-only 
            }
        };

        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Active Rules"));
        add(scrollPane, BorderLayout.CENTER);

        // [/] bOTTOM: Remove + Close buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton removeButton = new JButton("Remove Selected");
        removeButton.addActionListener(e -> 
        {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) 
            {
                rules.remove(selectedRow);
                tableModel.removeRow(selectedRow);
                notifyRefresh(); // [/] update editor tabs immediately
            } else {
                JOptionPane.showMessageDialog(this, "Please select a rule to remove.");
            }
        });
        bottomPanel.add(removeButton);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> setVisible(false));
        bottomPanel.add(closeButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // [/] add button action
        addButton.addActionListener(e -> 
        {
            String name = nameField.getText().trim();
            if (name.isEmpty()) 
            {
                JOptionPane.showMessageDialog(this, "Please enter a field name.");
                return;
            }

            RedactionRule.RuleType type = (RedactionRule.RuleType) typeCombo.getSelectedItem();
            RedactionRule rule = new RedactionRule(name, type);

            // [/] add to shared list and table
            rules.add(rule);
            tableModel.addRow(new Object[]{rule.getName(), rule.getType().name()});

            // [/] clear the input field for the next rule
            nameField.setText("");

            notifyRefresh(); // [/] update editor tabs immediately
        });

        // [/] load any existing rules into the table (in case dialog is re-opened)
        refreshTable();
    }

    /*[*]
     - sync the table display with the actual rules list.
     - called when the dialog is shown, in case rules were modified externally.
     */
    private void refreshTable() 
    {
        tableModel.setRowCount(0);
        for (RedactionRule rule : rules) 
        {
            tableModel.addRow(new Object[]{rule.getName(), rule.getType().name()});
        }
    }

    /*[*]
    - notify all editor tabs to re-render with updated rules.
     */
    private void notifyRefresh() {
        for (Runnable callback : refreshCallbacks) 
        {
            try 
            {
                callback.run();
            } catch (Exception ex) 
            {
                // [/] editor might have been closed => ignore
            }
        }
    }
    //[*]
    @Override
    public void setVisible(boolean visible) 
    {
        if (visible) 
        {
            refreshTable(); // [/] for update table when dialog is opened
        }
        super.setVisible(visible);
    }
}
