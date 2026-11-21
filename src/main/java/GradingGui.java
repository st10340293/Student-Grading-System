import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Swing front-end for StudentGradingSystem. Master-detail layout: student list on
 * the left, selected student (details + subject marks) on the right.
 * Reuses the existing model and CSV persistence unchanged.
 */
public class GradingGui extends JPanel {
    // Semantic colour tokens (text/background pairs all >= 4.5:1)
    static final Color BG = new Color(0xF4F6F8);
    static final Color SURFACE = Color.WHITE;
    static final Color BORDER = new Color(0xD0D5DD);
    static final Color TEXT = new Color(0x1B1F29);
    static final Color MUTED = new Color(0x4B5565);
    static final Color PRIMARY = new Color(0x1D4ED8);
    static final Color PRIMARY_HOVER = new Color(0x1E40AF);
    static final Color DANGER = new Color(0xB42318);
    static final Color SUCCESS = new Color(0x05603A);
    static final Color ROW_SELECTED = new Color(0xDBE6FE);

    // 4/8 spacing scale
    static final int S1 = 4, S2 = 8, S3 = 16, S4 = 24;

    private static final String DATA_FILE = "students.csv";

    private final StudentGradingSystem sgs;

    private final StudentTableModel studentModel = new StudentTableModel();
    private final JTable studentTable = new JTable(studentModel);
    private final TableRowSorter<StudentTableModel> sorter;
    private final JTextField search = new JTextField();
    private final CardLayout listCards = new CardLayout();
    private final JPanel listCardPanel = new JPanel(listCards);
    private final JLabel emptyListMsg = new JLabel("", SwingConstants.CENTER);

    private final CardLayout detailCards = new CardLayout();
    private final JPanel detailCardPanel = new JPanel(detailCards);
    private final JLabel formTitle = new JLabel();
    private final JTextField idField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JComboBox<Integer> gradeBox = new JComboBox<>(new Integer[]{8, 9, 10, 11, 12});
    private final JLabel summary = new JLabel();
    private final JLabel formFeedback = new JLabel(" ");
    private final JButton saveBtn = new Btn("Create student", Btn.PRIMARY);
    private final JButton deleteBtn = new Btn("Delete student", Btn.DANGER);

    private final SubjectTableModel subjectModel = new SubjectTableModel();
    private final JTable subjectTable = new JTable(subjectModel);
    private final JTextField subjectField = new JTextField();
    private final JTextField markField = new JTextField();
    private final JLabel subjectFeedback = new JLabel(" ");
    private final JButton subjectSaveBtn = new Btn("Add / update subject", Btn.SECONDARY);
    private final JButton subjectRemoveBtn = new Btn("Remove selected", Btn.SECONDARY);

    private final JLabel stats = new JLabel();
    private final JLabel dirtyLabel = new JLabel(" ");

    private Student current;       // student shown in the form; null while creating
    private boolean creating;
    private boolean dirty;
    private boolean adjusting;     // suppress selection listeners during programmatic changes

    public GradingGui(StudentGradingSystem sgs) {
        this.sgs = sgs;
        this.sorter = new TableRowSorter<>(studentModel);
        setLayout(new BorderLayout());
        setBackground(BG);
        applyFonts();
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        wireActions();
        refresh(null);
    }

    // ---------- layout ----------

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(S3, S4, S3, S4)));
        JLabel title = new JLabel("Student Grading System");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(TEXT);
        stats.setForeground(MUTED);
        dirtyLabel.setForeground(DANGER);
        dirtyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        JPanel right = new JPanel(new GridLayout(2, 1));
        right.setOpaque(false);
        stats.setHorizontalAlignment(SwingConstants.RIGHT);
        right.add(stats);
        right.add(dirtyLabel);
        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JComponent buildBody() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildListPane(), buildDetailPane());
        split.setDividerLocation(430);
        split.setResizeWeight(0.4);
        split.setBorder(new EmptyBorder(S3, S3, S3, S3));
        split.setOpaque(false);
        split.setDividerSize(S3);
        return split;
    }

    private JComponent buildListPane() {
        JPanel pane = card();
        pane.setLayout(new BorderLayout(0, S2));

        JLabel searchLabel = label("Search by name or ID");
        searchLabel.setLabelFor(search);
        search.setName("search");
        styleField(search);
        JButton add = new Btn("Add student", Btn.PRIMARY);
        add.setName("addStudent");
        add.addActionListener(e -> startCreate());

        JPanel top = new JPanel(new BorderLayout(S2, S1));
        top.setOpaque(false);
        top.add(searchLabel, BorderLayout.NORTH);
        top.add(search, BorderLayout.CENTER);
        top.add(add, BorderLayout.EAST);
        pane.add(top, BorderLayout.NORTH);

        studentTable.setName("studentTable");
        styleTable(studentTable);
        studentTable.setRowSorter(sorter);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.getAccessibleContext().setAccessibleName("Students");
        studentTable.getColumnModel().getColumn(0).setCellRenderer(padded(SwingConstants.RIGHT));
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        studentTable.getColumnModel().getColumn(1).setCellRenderer(padded(SwingConstants.LEFT));
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        studentTable.getColumnModel().getColumn(2).setCellRenderer(padded(SwingConstants.RIGHT));
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(55);
        studentTable.getColumnModel().getColumn(3).setCellRenderer(padded(SwingConstants.RIGHT));
        studentTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        studentTable.getColumnModel().getColumn(4).setCellRenderer(padded(SwingConstants.LEFT));
        studentTable.getColumnModel().getColumn(4).setPreferredWidth(70);

        emptyListMsg.setForeground(MUTED);
        listCardPanel.setOpaque(false);
        listCardPanel.add(scroll(studentTable), "table");
        listCardPanel.add(emptyListMsg, "empty");
        pane.add(listCardPanel, BorderLayout.CENTER);
        return pane;
    }

    private JComponent buildDetailPane() {
        JLabel none = new JLabel("<html><div style='text-align:center'>Select a student on the left,<br>or choose <b>Add student</b>.</div></html>",
                SwingConstants.CENTER);
        none.setForeground(MUTED);
        JPanel nonePane = card();
        nonePane.setLayout(new BorderLayout());
        nonePane.add(none, BorderLayout.CENTER);

        JPanel form = card();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        formTitle.setFont(formTitle.getFont().deriveFont(Font.BOLD, 16f));
        formTitle.setForeground(TEXT);
        formTitle.setAlignmentX(LEFT_ALIGNMENT);
        formTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        form.add(formTitle);
        form.add(Box.createVerticalStrut(S2));

        idField.setName("idField");
        nameField.setName("nameField");
        gradeBox.setName("gradeBox");
        styleField(idField);
        styleField(nameField);
        gradeBox.setBackground(SURFACE);
        gradeBox.setPreferredSize(new Dimension(80, 36));

        JPanel details = new JPanel(new GridBagLayout());
        details.setOpaque(false);
        details.setAlignmentX(LEFT_ALIGNMENT);
        addPair(details, 0, "Student ID (unique whole number)", idField, "Grade level", gradeBox);
        addPair(details, 1, "Name", nameField, null, null);
        form.add(details);

        formFeedback.setName("formFeedback");
        formFeedback.setAlignmentX(LEFT_ALIGNMENT);
        formFeedback.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        form.add(Box.createVerticalStrut(S2));
        form.add(formFeedback);
        form.add(Box.createVerticalStrut(S1));

        saveBtn.setName("saveBtn");
        deleteBtn.setName("deleteBtn");
        JPanel actions = buttonRow(saveBtn, deleteBtn);
        form.add(actions);

        form.add(Box.createVerticalStrut(S3));
        form.add(separator());
        form.add(Box.createVerticalStrut(S3));

        JLabel subjectsTitle = new JLabel("Subjects");
        subjectsTitle.setFont(subjectsTitle.getFont().deriveFont(Font.BOLD, 16f));
        subjectsTitle.setForeground(TEXT);
        subjectsTitle.setAlignmentX(LEFT_ALIGNMENT);
        form.add(subjectsTitle);
        summary.setName("summary");
        summary.setForeground(MUTED);
        summary.setAlignmentX(LEFT_ALIGNMENT);
        summary.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        form.add(summary);
        form.add(Box.createVerticalStrut(S2));

        subjectTable.setName("subjectTable");
        styleTable(subjectTable);
        subjectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        subjectTable.getAccessibleContext().setAccessibleName("Subjects and marks");
        subjectTable.getColumnModel().getColumn(0).setCellRenderer(padded(SwingConstants.LEFT));
        subjectTable.getColumnModel().getColumn(1).setCellRenderer(padded(SwingConstants.RIGHT));
        subjectTable.getColumnModel().getColumn(1).setMaxWidth(100);
        JScrollPane subjectScroll = scroll(subjectTable);
        subjectScroll.setAlignmentX(LEFT_ALIGNMENT);
        subjectScroll.setPreferredSize(new Dimension(300, 130));
        subjectScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        form.add(subjectScroll);
        form.add(Box.createVerticalStrut(S2));

        subjectField.setName("subjectField");
        markField.setName("markField");
        styleField(subjectField);
        styleField(markField);
        JPanel subjectForm = new JPanel(new GridBagLayout());
        subjectForm.setOpaque(false);
        subjectForm.setAlignmentX(LEFT_ALIGNMENT);
        markField.setPreferredSize(new Dimension(110, 36));
        addPair(subjectForm, 0, "Subject", subjectField, "Mark (0 to 100)", markField);
        form.add(subjectForm);

        subjectFeedback.setName("subjectFeedback");
        subjectFeedback.setAlignmentX(LEFT_ALIGNMENT);
        subjectFeedback.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        form.add(Box.createVerticalStrut(S2));
        form.add(subjectFeedback);
        form.add(Box.createVerticalStrut(S1));

        subjectSaveBtn.setName("subjectSaveBtn");
        subjectRemoveBtn.setName("subjectRemoveBtn");
        JPanel subjectActions = buttonRow(subjectSaveBtn, subjectRemoveBtn);
        form.add(subjectActions);
        form.add(Box.createVerticalGlue());

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);

        detailCardPanel.setOpaque(false);
        detailCardPanel.add(nonePane, "none");
        detailCardPanel.add(formScroll, "form");
        return detailCardPanel;
    }

    private static JPanel buttonRow(JButton a, JButton b) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.add(a);
        row.add(Box.createHorizontalStrut(S2));
        row.add(b);
        row.add(Box.createHorizontalGlue());
        return row;
    }

    /** Lays out up to two labelled fields side by side; the first field stretches. */
    private void addPair(JPanel p, int row, String t1, JComponent f1, String t2, JComponent f2) {
        JLabel l1 = label(t1);
        l1.setLabelFor(f1);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = row * 2; c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(row == 0 ? 0 : S2, 0, S1, 0);
        p.add(l1, c);
        c = new GridBagConstraints();
        c.gridx = 0; c.gridy = row * 2 + 1; c.weightx = 1; c.anchor = GridBagConstraints.WEST;
        c.fill = f1 instanceof JComboBox ? GridBagConstraints.NONE : GridBagConstraints.HORIZONTAL;
        if (f2 == null) c.gridwidth = 2;
        p.add(f1, c);
        if (f2 == null) return;
        JLabel l2 = label(t2);
        l2.setLabelFor(f2);
        c = new GridBagConstraints();
        c.gridx = 1; c.gridy = row * 2; c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(row == 0 ? 0 : S2, S2, S1, 0);
        p.add(l2, c);
        c = new GridBagConstraints();
        c.gridx = 1; c.gridy = row * 2 + 1; c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, S2, 0, 0);
        p.add(f2, c);
    }

    // ---------- behaviour ----------

    private void wireActions() {
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || adjusting) return;
            int viewRow = studentTable.getSelectedRow();
            if (viewRow >= 0) {
                showStudent(studentModel.at(studentTable.convertRowIndexToModel(viewRow)));
            } else if (!creating) {
                detailCards.show(detailCardPanel, "none");
            }
        });
        subjectTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || adjusting) return;
            int row = subjectTable.getSelectedRow();
            if (row >= 0) {
                subjectField.setText(subjectModel.nameAt(row));
                markField.setText(trimNumber(subjectModel.markAt(row)));
            }
            subjectRemoveBtn.setEnabled(row >= 0);
        });
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        saveBtn.addActionListener(e -> saveStudent());
        deleteBtn.addActionListener(e -> deleteStudent());
        subjectSaveBtn.addActionListener(e -> saveSubject());
        subjectRemoveBtn.addActionListener(e -> removeSubject());
        // Enter submits from the text fields
        nameField.addActionListener(e -> saveStudent());
        idField.addActionListener(e -> saveStudent());
        markField.addActionListener(e -> saveSubject());
        subjectField.addActionListener(e -> markField.requestFocusInWindow());

        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcutMask()), "save");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcutMask()), "new");
        getActionMap().put("save", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { saveToDisk(); }
        });
        getActionMap().put("new", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { startCreate(); }
        });
    }

    private static int shortcutMask() {
        try {
            return Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        } catch (HeadlessException e) {
            return java.awt.event.InputEvent.CTRL_DOWN_MASK;
        }
    }

    private void applyFilter() {
        String q = search.getText().trim();
        sorter.setRowFilter(q.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q), 0, 1));
        updateListState();
    }

    private void updateListState() {
        if (studentTable.getRowCount() > 0) {
            listCards.show(listCardPanel, "table");
            return;
        }
        emptyListMsg.setText(sgs.getStudents().isEmpty()
                ? "No students yet. Choose Add student to create one."
                : "No students match your search.");
        listCards.show(listCardPanel, "empty");
    }

    private void startCreate() {
        creating = true;
        current = null;
        adjusting = true;
        studentTable.clearSelection();
        adjusting = false;
        formTitle.setText("New student");
        idField.setEditable(true);
        idField.setText(String.valueOf(nextFreeId()));
        nameField.setText("");
        gradeBox.setSelectedItem(8);
        saveBtn.setText("Create student");
        deleteBtn.setVisible(false);
        setSubjectsEnabled(false);
        subjectModel.setStudent(null);
        summary.setText("Create the student first, then add subjects.");
        clearFeedback();
        detailCards.show(detailCardPanel, "form");
        nameField.requestFocusInWindow();
    }

    private void showStudent(Student s) {
        creating = false;
        current = s;
        formTitle.setText("Edit student");
        idField.setEditable(false);
        idField.setText(String.valueOf(s.getId()));
        nameField.setText(s.getName());
        gradeBox.setSelectedItem(s.getGradeLevel());
        saveBtn.setText("Save changes");
        deleteBtn.setVisible(true);
        setSubjectsEnabled(true);
        adjusting = true;
        subjectModel.setStudent(s);
        subjectTable.clearSelection();
        adjusting = false;
        subjectField.setText("");
        markField.setText("");
        subjectRemoveBtn.setEnabled(false);
        updateSummary();
        clearFeedback();
        detailCards.show(detailCardPanel, "form");
    }

    private void setSubjectsEnabled(boolean on) {
        subjectField.setEnabled(on);
        markField.setEnabled(on);
        subjectSaveBtn.setEnabled(on);
        subjectRemoveBtn.setEnabled(false);
        subjectTable.setEnabled(on);
    }

    private void saveStudent() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            feedback(formFeedback, "Enter the student's name.", true);
            nameField.requestFocusInWindow();
            return;
        }
        if (!csvSafe(name)) {
            feedback(formFeedback, "Name cannot contain commas or line breaks.", true);
            nameField.requestFocusInWindow();
            return;
        }
        int grade = (Integer) gradeBox.getSelectedItem();
        try {
            if (creating) {
                int id;
                try {
                    id = Integer.parseInt(idField.getText().trim());
                } catch (NumberFormatException ex) {
                    feedback(formFeedback, "Student ID must be a whole number, for example 1001.", true);
                    idField.requestFocusInWindow();
                    return;
                }
                sgs.addStudent(new Student(id, name, grade));
                markDirty();
                refresh(id);
                feedback(formFeedback, "Student created. You can now add subjects.", false);
                subjectField.requestFocusInWindow();
            } else {
                sgs.updateStudentDetails(current.getId(), name, grade);
                markDirty();
                refresh(current.getId());
                feedback(formFeedback, "Changes applied.", false);
            }
        } catch (IllegalArgumentException ex) {
            feedback(formFeedback, ex.getMessage(), true);
        }
    }

    private void deleteStudent() {
        if (current == null) return;
        Object[] options = {"Cancel", "Delete"};
        int choice = JOptionPane.showOptionDialog(this,
                "Delete " + current.getName() + " (ID " + current.getId() + ") and all their marks?\nThis cannot be undone.",
                "Delete student", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        if (choice != 1) return;
        sgs.deleteStudent(current.getId());
        markDirty();
        current = null;
        creating = false;
        refresh(null);
        detailCards.show(detailCardPanel, "none");
    }

    private void saveSubject() {
        if (current == null) return;
        String subject = subjectField.getText().trim();
        if (subject.isEmpty()) {
            feedback(subjectFeedback, "Enter a subject name.", true);
            subjectField.requestFocusInWindow();
            return;
        }
        if (!csvSafe(subject) || subject.contains(";") || subject.contains(":")) {
            feedback(subjectFeedback, "Subject cannot contain , ; : or line breaks.", true);
            subjectField.requestFocusInWindow();
            return;
        }
        double mark;
        try {
            mark = Double.parseDouble(markField.getText().trim());
        } catch (NumberFormatException ex) {
            feedback(subjectFeedback, "Mark must be a number from 0 to 100, for example 72.5.", true);
            markField.requestFocusInWindow();
            return;
        }
        try {
            boolean existed = current.getSubjectMark(subject) != null;
            sgs.addSubjectToStudent(current.getId(), subject, mark);
            markDirty();
            subjectModel.setStudent(current);
            updateSummary();
            studentModel.fireTableDataChanged();
            reselectCurrent();
            subjectField.setText("");
            markField.setText("");
            subjectTable.clearSelection();
            feedback(subjectFeedback, existed ? "Mark updated." : "Subject added.", false);
            subjectField.requestFocusInWindow();
        } catch (IllegalArgumentException ex) {
            feedback(subjectFeedback, ex.getMessage(), true);
            markField.requestFocusInWindow();
        }
    }

    private void removeSubject() {
        int row = subjectTable.getSelectedRow();
        if (current == null || row < 0) return;
        String name = subjectModel.nameAt(row);
        current.removeSubject(name);
        markDirty();
        subjectModel.setStudent(current);
        updateSummary();
        studentModel.fireTableDataChanged();
        reselectCurrent();
        subjectField.setText("");
        markField.setText("");
        subjectRemoveBtn.setEnabled(false);
        feedback(subjectFeedback, name + " removed.", false);
    }

    private void updateSummary() {
        summary.setText(current.getSubjects().isEmpty()
                ? "No subjects yet."
                : String.format("Average %.1f  |  %s", current.getAverage(), current.getLetterGrade()));
    }

    /** Re-reads the model, optionally re-selecting a student by ID. */
    private void refresh(Integer selectId) {
        adjusting = true;
        studentModel.fireTableDataChanged();
        adjusting = false;
        stats.setText(String.format("%d students  |  Class average %.1f",
                sgs.getStudents().size(), sgs.classAverage()));
        updateListState();
        if (selectId != null) {
            for (int i = 0; i < studentTable.getRowCount(); i++) {
                if (studentModel.at(studentTable.convertRowIndexToModel(i)).getId() == selectId) {
                    studentTable.setRowSelectionInterval(i, i);
                    studentTable.scrollRectToVisible(studentTable.getCellRect(i, 0, true));
                    break;
                }
            }
            if (studentTable.getSelectedRow() < 0 && current != null) showStudent(current);
        }
    }

    private void reselectCurrent() {
        stats.setText(String.format("%d students  |  Class average %.1f",
                sgs.getStudents().size(), sgs.classAverage()));
        for (int i = 0; i < studentTable.getRowCount(); i++) {
            if (studentModel.at(studentTable.convertRowIndexToModel(i)) == current) {
                adjusting = true;
                studentTable.setRowSelectionInterval(i, i);
                adjusting = false;
                break;
            }
        }
    }

    private int nextFreeId() {
        int max = 0;
        for (Student s : sgs.getStudents()) max = Math.max(max, s.getId());
        return max + 1;
    }

    private void markDirty() {
        dirty = true;
        dirtyLabel.setText("Unsaved changes (Ctrl+S to save, saved automatically on close)");
    }

    void saveToDisk() {
        sgs.save();
        dirty = false;
        dirtyLabel.setText(" ");
    }

    boolean isDirty() { return dirty; }

    private void clearFeedback() {
        formFeedback.setText(" ");
        subjectFeedback.setText(" ");
    }

    private static void feedback(JLabel l, String msg, boolean error) {
        l.setForeground(error ? DANGER : SUCCESS);
        l.setText(msg);
        l.getAccessibleContext().setAccessibleDescription(msg);
    }

    private static boolean csvSafe(String s) {
        return s.indexOf(',') < 0 && s.indexOf('\n') < 0 && s.indexOf('\r') < 0;
    }

    private static String trimNumber(double d) {
        return d == Math.rint(d) ? String.valueOf((long) d) : String.valueOf(d);
    }

    // ---------- styling helpers ----------

    private void applyFonts() {
        Font base = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        for (String key : new String[]{"Label.font", "Table.font", "TableHeader.font", "TextField.font",
                "ComboBox.font", "Button.font", "OptionPane.messageFont", "OptionPane.buttonFont"}) {
            UIManager.put(key, base);
        }
    }

    private static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(S3, S3, S3, S3)));
        return p;
    }

    private static JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(TEXT);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 13f));
        return l;
    }

    private static JComponent separator() {
        JSeparator s = new JSeparator();
        s.setForeground(BORDER);
        s.setAlignmentX(LEFT_ALIGNMENT);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }

    private static void styleField(JTextField f) {
        f.setBackground(SURFACE);
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(S1 + 2, S2, S1 + 2, S2)));
        f.setPreferredSize(new Dimension(180, 36));
        // Visible focus indicator: swap border colour/width on focus
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(new LineBorder(PRIMARY, 2, true), new EmptyBorder(S1 + 1, S2 - 1, S1 + 1, S2 - 1)));
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(S1 + 2, S2, S1 + 2, S2)));
            }
        });
    }

    private static void styleTable(JTable t) {
        t.setRowHeight(32);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(0xE4E7EC));
        t.setSelectionBackground(ROW_SELECTED);
        t.setSelectionForeground(TEXT);
        t.setForeground(TEXT);
        t.setBackground(SURFACE);
        t.setFillsViewportHeight(true);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setBackground(BG);
        t.getTableHeader().setForeground(TEXT);
        t.getTableHeader().setFont(t.getTableHeader().getFont().deriveFont(Font.BOLD));
    }

    private static DefaultTableCellRenderer padded(int align) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, false, row, col);
                setBorder(new EmptyBorder(0, S2, 0, S2));   // super resets the border on every call
                return this;
            }
        };
        r.setHorizontalAlignment(align);
        return r;
    }

    private static JScrollPane scroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setColumnHeaderView(t.getTableHeader());
        sp.setBorder(new LineBorder(BORDER, 1));
        sp.getViewport().setBackground(SURFACE);
        return sp;
    }

    /** Rounded button with hover/pressed/disabled/focus states. */
    static class Btn extends JButton {
        static final int PRIMARY = 0, SECONDARY = 1, DANGER = 2;
        private final int kind;

        Btn(String text, int kind) {
            super(text);
            this.kind = kind;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(S2, S3, S2, S3));
            setMargin(new Insets(S2, S3, S2, S3));
            setFont(getFont().deriveFont(Font.BOLD, 14f));
        }

        @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width, Math.max(40, d.height));
        }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill, fg, line;
            boolean hot = getModel().isRollover() || getModel().isPressed();
            if (!isEnabled()) {
                fill = new Color(0xEEF0F3); fg = new Color(0x6B7280); line = BORDER;
            } else if (kind == SECONDARY) {
                fill = hot ? new Color(0xE9EDF5) : SURFACE; fg = TEXT; line = BORDER;
            } else if (kind == DANGER) {
                fill = hot ? new Color(0xFEE4E2) : SURFACE; fg = GradingGui.DANGER; line = GradingGui.DANGER;
            } else {
                fill = hot ? PRIMARY_HOVER : GradingGui.PRIMARY; fg = Color.WHITE; line = fill;
            }
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g.setColor(line);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            if (isFocusOwner()) {
                g.setColor(GradingGui.PRIMARY);
                g.setStroke(new BasicStroke(2f));
                g.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 8, 8);
            }
            g.setColor(fg);
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(getText(), x, y);
            g.dispose();
        }
    }

    // ---------- table models ----------

    private class StudentTableModel extends AbstractTableModel {
        private final String[] cols = {"ID", "Name", "Grade", "Average", "Level"};

        Student at(int row) { return sgs.getStudents().get(row); }
        public int getRowCount() { return sgs.getStudents().size(); }
        public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }
        @Override public Class<?> getColumnClass(int c) {
            return c == 0 || c == 2 ? Integer.class : c == 3 ? Double.class : String.class;
        }
        public Object getValueAt(int r, int c) {
            Student s = at(r);
            switch (c) {
                case 0: return s.getId();
                case 1: return s.getName();
                case 2: return s.getGradeLevel();
                case 3: return Math.round(s.getAverage() * 10) / 10.0;
                default: return s.getLetterGrade();
            }
        }
    }

    private static class SubjectTableModel extends AbstractTableModel {
        private List<Map.Entry<String, Double>> rows = new ArrayList<>();

        void setStudent(Student s) {
            rows = s == null ? new ArrayList<>() : new ArrayList<>(s.getSubjects().entrySet());
            fireTableDataChanged();
        }
        String nameAt(int r) { return rows.get(r).getKey(); }
        double markAt(int r) { return rows.get(r).getValue(); }
        public int getRowCount() { return rows.size(); }
        public int getColumnCount() { return 2; }
        @Override public String getColumnName(int c) { return c == 0 ? "Subject" : "Mark"; }
        public Object getValueAt(int r, int c) {
            return c == 0 ? rows.get(r).getKey() : String.format("%.1f", rows.get(r).getValue());
        }
    }

    // ---------- entry point ----------

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) { }
            StudentGradingSystem sgs = new StudentGradingSystem(DATA_FILE);
            GradingGui gui = new GradingGui(sgs);
            JFrame frame = new JFrame("Student Grading System");
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override public void windowClosing(WindowEvent e) {
                    if (gui.isDirty()) gui.saveToDisk();
                    frame.dispose();
                }
            });
            frame.setContentPane(gui);
            frame.setMinimumSize(new Dimension(880, 640));
            frame.setSize(1100, 760);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
