import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class Main extends JFrame {
    private static final Color BG = new Color(15, 18, 30);
    private static final Color PANEL = new Color(27, 32, 49);
    private static final Color PANEL_LIGHT = new Color(36, 43, 64);
    private static final Color BLUE = new Color(83, 145, 255);
    private static final Color ACTIVE = new Color(255, 190, 75);
    private static final Color GREEN = new Color(70, 215, 150);
    private static final Color TEXT = new Color(238, 242, 255);
    private static final Color MUTED = new Color(155, 165, 190);

    private int[] arr;
    private int[] originalArray;
    private final java.util.List<Step> steps = new ArrayList<>();

    private JComboBox<String> algorithmChoice;
    private JButton startButton, pauseButton, newButton, resetButton, compareButton;
    private JLabel statusLabel, comparisonLabel, moveLabel, timeLabel;
    private JLabel algorithmInfoLabel, sizeLabel, speedLabel;
    private JSpinner sizeSpinner;
    private JSlider speedSlider;
    private BarPanel barPanel;
    private javax.swing.Timer timer;

    private int stepIndex, comparisons, moves;
    private int activeA = -1, activeB = -1;
    private boolean running, paused, completed;
    private long startTime, elapsed;

    private static class Step {
        int[] values;
        int a, b;
        boolean comparison, move;

        Step(int[] values, int a, int b, boolean comparison, boolean move) {
            this.values = values.clone();
            this.a = a;
            this.b = b;
            this.comparison = comparison;
            this.move = move;
        }
    }

    public Main() {
        setTitle("SortRace | Algorithm Battle Arena");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 790);
        setMinimumSize(new Dimension(900, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainArea(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        originalArray = createArray(45);
        arr = originalArray.clone();
        pauseButton.setEnabled(false);
        updateAlgorithmInfo();
        updateStats();
        barPanel.repaint();

        newButton.addActionListener(e -> newArray());
        startButton.addActionListener(e -> startSorting());
        pauseButton.addActionListener(e -> togglePause());
        resetButton.addActionListener(e -> resetSorting());
        compareButton.addActionListener(e -> showComparisonArena());
        algorithmChoice.addActionListener(e -> updateAlgorithmInfo());
        speedSlider.addChangeListener(e -> {
            speedLabel.setText("Animation speed: " + speedSlider.getValue());
            if (timer != null) timer.setDelay(delayFromSpeed());
        });
        sizeSpinner.addChangeListener(e ->
                sizeLabel.setText("Array size: " + sizeSpinner.getValue()));
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(new EmptyBorder(22, 28, 14, 28));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("SORT RACE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel("ALGORITHM BATTLE ARENA  •  VISUALIZE. COMPARE. LEARN.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(MUTED);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(3));
        titleBlock.add(subtitle);

        JLabel version = new JLabel("JAVA • SWING");
        version.setForeground(BLUE);
        version.setFont(new Font("Segoe UI", Font.BOLD, 12));
        version.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(56, 78, 125)),
                new EmptyBorder(9, 13, 9, 13)));

        header.add(titleBlock, BorderLayout.WEST);
        header.add(version, BorderLayout.EAST);
        return header;
    }

    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout(0, 14));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(0, 24, 12, 24));

        JPanel controls = new JPanel(new GridBagLayout());
        controls.setBackground(PANEL);
        controls.setBorder(new EmptyBorder(14, 16, 14, 16));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 5, 4, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 0;

        algorithmChoice = new JComboBox<>(new String[]{
                "Bubble Sort", "Merge Sort", "Quick Sort", "Heap Sort"
        });
        styleCombo(algorithmChoice);
        sizeSpinner = new JSpinner(new SpinnerNumberModel(45, 15, 80, 5));
        sizeSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sizeSpinner.setPreferredSize(new Dimension(75, 34));

        newButton = makeButton("New Array", PANEL_LIGHT);
        startButton = makeButton("▶  Start Sorting", BLUE);
        pauseButton = makeButton("Ⅱ  Pause", new Color(125, 91, 205));
        resetButton = makeButton("↺  Reset", PANEL_LIGHT);
        compareButton = makeButton("⚔  Compare", new Color(35, 145, 125));

        addControl(controls, c, 0, "ALGORITHM", algorithmChoice, 1);
        addControl(controls, c, 1, "ARRAY SIZE", sizeSpinner, 1);
        addControl(controls, c, 2, "ACTIONS", newButton, 1);
        addControl(controls, c, 3, "", startButton, 1);
        addControl(controls, c, 4, "", pauseButton, 1);
        addControl(controls, c, 5, "", resetButton, 1);
        addControl(controls, c, 6, "BATTLE MODE", compareButton, 1);

        JPanel sliders = new JPanel(new GridLayout(1, 2, 22, 0));
        sliders.setBackground(PANEL);
        sliders.setBorder(new EmptyBorder(10, 16, 12, 16));

        JPanel sizeInfo = new JPanel(new BorderLayout(0, 4));
        sizeInfo.setOpaque(false);
        sizeLabel = smallLabel("Array size: 45");
        JLabel sizeHint = smallLabel("Choose a size, then click New Array");
        sizeInfo.add(sizeLabel, BorderLayout.NORTH);
        sizeInfo.add(sizeHint, BorderLayout.SOUTH);

        JPanel speedInfo = new JPanel(new BorderLayout(0, 4));
        speedInfo.setOpaque(false);
        speedLabel = smallLabel("Animation speed: 5");
        speedSlider = new JSlider(1, 10, 5);
        speedSlider.setOpaque(false);
        speedSlider.setForeground(BLUE);
        speedSlider.setMajorTickSpacing(3);
        speedSlider.setPaintTicks(true);
        speedSlider.setToolTipText("Higher value means faster animation");
        speedInfo.add(speedLabel, BorderLayout.NORTH);
        speedInfo.add(speedSlider, BorderLayout.CENTER);

        // Keep the array size helper and speed slider in a compact second row.
        sliders.add(sizeInfo);
        sliders.add(speedInfo);

        JPanel controlStack = new JPanel(new BorderLayout(0, 0));
        controlStack.setOpaque(false);
        controlStack.add(controls, BorderLayout.NORTH);
        controlStack.add(sliders, BorderLayout.SOUTH);

        JPanel chartCard = new JPanel(new BorderLayout());
        chartCard.setBackground(PANEL);
        chartCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 52, 75)),
                new EmptyBorder(12, 12, 8, 12)));

        JPanel chartHeader = new JPanel(new BorderLayout());
        chartHeader.setOpaque(false);
        JLabel chartTitle = new JLabel("LIVE ARRAY VISUALIZATION");
        chartTitle.setForeground(TEXT);
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel legend = new JLabel("  ● Unsorted     ● Comparing / moving     ● Sorted");
        legend.setForeground(MUTED);
        legend.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chartHeader.add(chartTitle, BorderLayout.WEST);
        chartHeader.add(legend, BorderLayout.EAST);

        barPanel = new BarPanel();
        chartCard.add(chartHeader, BorderLayout.NORTH);
        chartCard.add(barPanel, BorderLayout.CENTER);

        JPanel upper = new JPanel(new BorderLayout(0, 12));
        upper.setOpaque(false);
        upper.add(controlStack, BorderLayout.NORTH);
        upper.add(chartCard, BorderLayout.CENTER);

        main.add(upper, BorderLayout.CENTER);
        return main;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout(0, 10));
        footer.setBackground(BG);
        footer.setBorder(new EmptyBorder(0, 24, 20, 24));

        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setBackground(BG);
        comparisonLabel = makeStat("COMPARISONS", "0");
        moveLabel = makeStat("SWAPS / MOVES", "0");
        timeLabel = makeStat("ELAPSED TIME", "0.00 s");
        stats.add(comparisonLabel);
        stats.add(moveLabel);
        stats.add(timeLabel);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(PANEL);
        statusPanel.setBorder(new EmptyBorder(10, 14, 10, 14));
        statusLabel = new JLabel("Choose an algorithm and press Start Sorting.");
        statusLabel.setForeground(MUTED);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        algorithmInfoLabel = new JLabel(" ");
        algorithmInfoLabel.setForeground(BLUE);
        algorithmInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(algorithmInfoLabel, BorderLayout.EAST);

        footer.add(stats, BorderLayout.NORTH);
        footer.add(statusPanel, BorderLayout.SOUTH);
        return footer;
    }

    private void addControl(JPanel panel, GridBagConstraints c, int x,
                            String labelText, JComponent component, int width) {
        JPanel cell = new JPanel(new BorderLayout(0, 5));
        cell.setOpaque(false);
        JLabel label = smallLabel(labelText);
        cell.add(label, BorderLayout.NORTH);
        cell.add(component, BorderLayout.CENTER);
        c.gridx = x;
        c.gridwidth = width;
        c.weightx = 1;
        panel.add(cell, c);
    }

    private JLabel smallLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        return label;
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setPreferredSize(new Dimension(150, 34));
        combo.setBackground(PANEL_LIGHT);
        combo.setForeground(TEXT);
    }

    private JButton makeButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setBorder(new EmptyBorder(10, 13, 10, 13));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JLabel makeStat(String heading, String value) {
        JLabel label = new JLabel("<html><div style='text-align:center;'>"
                + "<span style='font-size:10px;color:#9BA6C0;'>" + heading + "</span>"
                + "<br><span style='font-size:22px;color:#EEF2FF;'>" + value
                + "</span></div></html>", SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(PANEL);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 52, 75)),
                new EmptyBorder(10, 5, 10, 5)));
        return label;
    }

    private int[] createArray(int size) {
        Random random = new Random();
        int[] values = new int[size];
        for (int i = 0; i < size; i++) values[i] = 10 + random.nextInt(90);
        return values;
    }

    private void newArray() {
        stopTimer();
        originalArray = createArray((Integer) sizeSpinner.getValue());
        arr = originalArray.clone();
        clearStats();
        completed = false;
        statusLabel.setText("New random array generated. Ready to race!");
        barPanel.repaint();
    }

    private void clearStats() {
        steps.clear();
        stepIndex = 0;
        comparisons = 0;
        moves = 0;
        activeA = activeB = -1;
        elapsed = 0;
        updateStats();
    }

    private void resetSorting() {
        stopTimer();
        arr = originalArray.clone();
        clearStats();
        completed = false;
        statusLabel.setText("Reset complete. Press Start Sorting.");
        barPanel.repaint();
    }

    private void startSorting() {
        if (running) return;
        stopTimer();
        arr = originalArray.clone();
        clearStats();
        completed = false;

        int[] work = arr.clone();
        String algorithm = (String) algorithmChoice.getSelectedItem();
        switch (algorithm) {
            case "Bubble Sort" -> bubbleSort(work);
            case "Merge Sort" -> mergeSort(work);
            case "Quick Sort" -> quickSort(work);
            case "Heap Sort" -> heapSort(work);
            default -> bubbleSort(work);
        }
        steps.add(new Step(work, -1, -1, false, false));

        running = true;
        paused = false;
        startButton.setEnabled(false);
        newButton.setEnabled(false);
        resetButton.setEnabled(false);
        algorithmChoice.setEnabled(false);
        sizeSpinner.setEnabled(false);
        pauseButton.setEnabled(true);
        pauseButton.setText("Ⅱ  Pause");

        startTime = System.currentTimeMillis();
        statusLabel.setText(algorithm + " is running...");
        timer = new javax.swing.Timer(delayFromSpeed(), e -> {
            if (paused) return;
            if (stepIndex >= steps.size()) {
                finishSorting();
                return;
            }
            Step step = steps.get(stepIndex++);
            arr = step.values.clone();
            activeA = step.a;
            activeB = step.b;
            if (step.comparison) comparisons++;
            if (step.move) moves++;
            elapsed = System.currentTimeMillis() - startTime;
            updateStats();
            barPanel.repaint();
            if (stepIndex >= steps.size()) finishSorting();
        });
        timer.start();
    }

    private int delayFromSpeed() {
        return 105 - speedSlider.getValue() * 10;
    }

    private void togglePause() {
        if (!running) return;
        paused = !paused;
        pauseButton.setText(paused ? "▶  Resume" : "Ⅱ  Pause");
        if (paused) {
            elapsed = System.currentTimeMillis() - startTime;
            statusLabel.setText("Sorting paused.");
        } else {
            startTime = System.currentTimeMillis() - elapsed;
            statusLabel.setText("Sorting resumed.");
        }
        updateStats();
    }

    private void finishSorting() {
        if (timer != null) timer.stop();
        running = false;
        paused = false;
        activeA = activeB = -1;
        elapsed = System.currentTimeMillis() - startTime;
        completed = true;
        startButton.setEnabled(true);
        pauseButton.setEnabled(false);
        pauseButton.setText("Ⅱ  Pause");
        newButton.setEnabled(true);
        resetButton.setEnabled(true);
        algorithmChoice.setEnabled(true);
        sizeSpinner.setEnabled(true);
        updateStats();
        statusLabel.setText("Sorting completed! " + algorithmChoice.getSelectedItem());
        barPanel.repaint();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        running = false;
        paused = false;
        if (startButton != null) startButton.setEnabled(true);
        if (pauseButton != null) {
            pauseButton.setEnabled(false);
            pauseButton.setText("Ⅱ  Pause");
        }
        if (newButton != null) newButton.setEnabled(true);
        if (resetButton != null) resetButton.setEnabled(true);
        if (algorithmChoice != null) algorithmChoice.setEnabled(true);
        if (sizeSpinner != null) sizeSpinner.setEnabled(true);
    }

    private void updateStats() {
        if (comparisonLabel != null)
            comparisonLabel.setText("<html><div style='text-align:center;'><span style='font-size:10px;color:#9BA6C0;'>COMPARISONS</span><br><span style='font-size:22px;color:#EEF2FF;'>"
                    + comparisons + "</span></div></html>");
        if (moveLabel != null)
            moveLabel.setText("<html><div style='text-align:center;'><span style='font-size:10px;color:#9BA6C0;'>SWAPS / MOVES</span><br><span style='font-size:22px;color:#EEF2FF;'>"
                    + moves + "</span></div></html>");
        if (timeLabel != null)
            timeLabel.setText("<html><div style='text-align:center;'><span style='font-size:10px;color:#9BA6C0;'>ELAPSED TIME</span><br><span style='font-size:22px;color:#EEF2FF;'>"
                    + String.format("%.2f s", elapsed / 1000.0)
                    + "</span></div></html>");
    }

    private void updateAlgorithmInfo() {
        if (algorithmInfoLabel == null || algorithmChoice == null) return;
        String algorithm = (String) algorithmChoice.getSelectedItem();
        String info = switch (algorithm) {
            case "Bubble Sort" -> "Time: O(n²)  •  Space: O(1)";
            case "Merge Sort" -> "Time: O(n log n)  •  Space: O(n)";
            case "Quick Sort" -> "Average: O(n log n)  •  Worst: O(n²)";
            case "Heap Sort" -> "Time: O(n log n)  •  Space: O(1)";
            default -> "";
        };
        algorithmInfoLabel.setText(info);
        if (!running && statusLabel != null)
            statusLabel.setText("Selected: " + algorithm + " — press Start Sorting.");
    }

    private static class AlgorithmMetric {
        String name;
        long comparisons;
        long moves;

        AlgorithmMetric(String name, long comparisons, long moves) {
            this.name = name;
            this.comparisons = comparisons;
            this.moves = moves;
        }
    }

    private java.util.List<AlgorithmMetric> calculateMetrics() {
        java.util.List<AlgorithmMetric> results = new ArrayList<>();
        String[] names = {"Bubble Sort", "Merge Sort", "Quick Sort", "Heap Sort"};
        for (String name : names) {
            steps.clear();
            int[] sample = originalArray.clone();
            switch (name) {
                case "Bubble Sort" -> bubbleSort(sample);
                case "Merge Sort" -> mergeSort(sample);
                case "Quick Sort" -> quickSort(sample);
                case "Heap Sort" -> heapSort(sample);
            }
            long comparisonsCount = 0;
            long movesCount = 0;
            for (Step step : steps) {
                if (step.comparison) comparisonsCount++;
                if (step.move) movesCount++;
            }
            results.add(new AlgorithmMetric(name, comparisonsCount, movesCount));
        }
        steps.clear();
        return results;
    }

    private void showComparisonArena() {
        if (running) {
            JOptionPane.showMessageDialog(this,
                    "Pause or finish the current sorting animation before comparing algorithms.",
                    "Sorting in progress", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        java.util.List<AlgorithmMetric> metrics = calculateMetrics();
        AlgorithmMetric winner = metrics.get(0);
        for (AlgorithmMetric metric : metrics) {
            if (metric.comparisons < winner.comparisons) winner = metric;
        }

        JDialog dialog = new JDialog(this, "SortRace | Algorithm Comparison Arena", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(1120, 820);
        dialog.setMinimumSize(new Dimension(950, 700));
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(BG);
        dialog.setLayout(new BorderLayout(12, 12));

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel heading = new JPanel(new BorderLayout(0, 6));
        heading.setOpaque(false);
        JLabel title = new JLabel("ALGORITHM COMPARISON ARENA");
        title.setForeground(TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel subtitle = new JLabel("Same input array • " + originalArray.length
                + " elements • Counts are based on the selected array");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        heading.add(title, BorderLayout.NORTH);
        heading.add(subtitle, BorderLayout.SOUTH);

        JPanel winnerCard = new JPanel(new BorderLayout(10, 0));
        winnerCard.setBackground(new Color(24, 57, 53));
        winnerCard.setBorder(new EmptyBorder(12, 15, 12, 15));
        JLabel winnerTitle = new JLabel("FEWEST COMPARISONS");
        winnerTitle.setForeground(new Color(140, 245, 200));
        winnerTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        JLabel winnerName = new JLabel(winner.name + "  •  " + winner.comparisons + " comparisons");
        winnerName.setForeground(Color.WHITE);
        winnerName.setFont(new Font("Segoe UI", Font.BOLD, 17));
        JPanel winnerText = new JPanel(new BorderLayout(0, 4));
        winnerText.setOpaque(false);
        winnerText.add(winnerTitle, BorderLayout.NORTH);
        winnerText.add(winnerName, BorderLayout.CENTER);
        winnerCard.add(new JLabel("🏆"), BorderLayout.WEST);
        winnerCard.add(winnerText, BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        top.add(winnerCard, BorderLayout.SOUTH);

        JPanel middle = new JPanel(new GridLayout(1, 2, 14, 0));
        middle.setOpaque(false);
        middle.setPreferredSize(new Dimension(1000, 300));
        middle.setMinimumSize(new Dimension(700, 260));

        JPanel chartCard = new JPanel(new BorderLayout(0, 8));
        chartCard.setBackground(PANEL);
        chartCard.setBorder(new EmptyBorder(14, 14, 14, 14));
        JLabel chartTitle = new JLabel("COMPARISONS BY ALGORITHM");
        chartTitle.setForeground(TEXT);
        chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chartCard.add(chartTitle, BorderLayout.NORTH);
        chartCard.add(new ComparisonChart(metrics), BorderLayout.CENTER);
        middle.add(chartCard);

        JPanel tableCard = new JPanel(new BorderLayout(0, 8));
        tableCard.setBackground(PANEL);
        tableCard.setBorder(new EmptyBorder(14, 14, 14, 14));
        JLabel tableTitle = new JLabel("MEASURED RESULTS");
        tableTitle.setForeground(TEXT);
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        String[] columns = {"Algorithm", "Comparisons", "Moves"};
        Object[][] rows = new Object[metrics.size()][3];
        for (int i = 0; i < metrics.size(); i++) {
            AlgorithmMetric m = metrics.get(i);
            rows[i][0] = m.name;
            rows[i][1] = m.comparisons;
            rows[i][2] = m.moves;
        }
        JTable resultTable = makeDarkTable(columns, rows);
        tableCard.add(tableTitle, BorderLayout.NORTH);
        JScrollPane resultsScroll = new JScrollPane(resultTable);
        resultsScroll.setPreferredSize(new Dimension(450, 220));
        resultsScroll.getViewport().setBackground(PANEL_LIGHT);
        resultsScroll.setBackground(PANEL_LIGHT);
        tableCard.add(resultsScroll, BorderLayout.CENTER);
        middle.add(tableCard);

        JPanel complexityCard = new JPanel(new BorderLayout(0, 8));
        complexityCard.setBackground(PANEL);
        complexityCard.setBorder(new EmptyBorder(12, 14, 12, 14));
        JLabel complexityTitle = new JLabel("TIME & SPACE COMPLEXITY REFERENCE");
        complexityTitle.setForeground(TEXT);
        complexityTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        String[] complexityColumns = {"Algorithm", "Best", "Average", "Worst", "Space"};
        Object[][] complexityRows = {
                {"Bubble Sort", "O(n)", "O(n²)", "O(n²)", "O(1)"},
                {"Merge Sort", "O(n log n)", "O(n log n)", "O(n log n)", "O(n)"},
                {"Quick Sort", "O(n log n)", "O(n log n)", "O(n²)", "O(log n)*"},
                {"Heap Sort", "O(n log n)", "O(n log n)", "O(n log n)", "O(1)"}
        };
        JTable complexityTable = makeDarkTable(complexityColumns, complexityRows);
        complexityCard.add(complexityTitle, BorderLayout.NORTH);
        JScrollPane complexityScroll = new JScrollPane(complexityTable);
        complexityScroll.setPreferredSize(new Dimension(1000, 155));
        complexityScroll.getViewport().setBackground(PANEL_LIGHT);
        complexityScroll.setBackground(PANEL_LIGHT);
        complexityCard.add(complexityScroll, BorderLayout.CENTER);

        JLabel note = new JLabel("Note: the winner is based on comparison count for this array, not every possible input. *Quick Sort space is average; worst-case stack space can be O(n).", SwingConstants.CENTER);
        note.setForeground(MUTED);
        note.setFont(new Font("Segoe UI", Font.PLAIN, 10));

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);
        bottom.add(complexityCard, BorderLayout.CENTER);
        bottom.add(note, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(middle, BorderLayout.CENTER);
        center.add(bottom, BorderLayout.SOUTH);

        content.add(top, BorderLayout.NORTH);
        content.add(center, BorderLayout.CENTER);
        dialog.add(content, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private JTable makeDarkTable(String[] columns, Object[][] rows) {
        JTable table = new JTable(rows, columns);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.setBackground(PANEL_LIGHT);
        table.setForeground(TEXT);
        table.setGridColor(new Color(55, 63, 87));
        table.setSelectionBackground(new Color(57, 83, 132));
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBackground(new Color(43, 52, 76));
        table.getTableHeader().setForeground(TEXT);
        table.getTableHeader().setReorderingAllowed(false);
        return table;
    }

    private class ComparisonChart extends JPanel {
        private final java.util.List<AlgorithmMetric> metrics;
        ComparisonChart(java.util.List<AlgorithmMetric> metrics) {
            this.metrics = metrics;
            setBackground(PANEL);
            setOpaque(true);
            setPreferredSize(new Dimension(480, 280));
            setMinimumSize(new Dimension(300, 220));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int left = 42, right = 16, top = 18, bottom = 42;
            int chartW = getWidth() - left - right;
            int chartH = getHeight() - top - bottom;
            long max = 1;
            for (AlgorithmMetric m : metrics) max = Math.max(max, m.comparisons);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(MUTED);
            for (int line = 0; line <= 4; line++) {
                int y = top + chartH - (chartH * line / 4);
                g2.setColor(new Color(54, 62, 85));
                g2.drawLine(left, y, left + chartW, y);
                g2.setColor(MUTED);
                String label = String.valueOf(max * line / 4);
                g2.drawString(label, Math.max(1, left - 35), y + 4);
            }
            int slot = chartW / metrics.size();
            int barW = Math.max(18, slot / 2);
            for (int i = 0; i < metrics.size(); i++) {
                AlgorithmMetric m = metrics.get(i);
                int h = (int) ((double) m.comparisons / max * Math.max(1, chartH));
                int x = left + i * slot + (slot - barW) / 2;
                int y = top + chartH - h;
                g2.setColor(m.name.equals("" ) ? BLUE : new Color(83, 145, 255));
                g2.fillRoundRect(x, y, barW, h, 7, 7);
                g2.setColor(TEXT);
                String value = String.valueOf(m.comparisons);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(value, x + (barW - fm.stringWidth(value)) / 2, Math.max(top + 11, y - 5));
                g2.setColor(MUTED);
                String shortName = switch (m.name) {
                    case "Bubble Sort" -> "Bubble";
                    case "Merge Sort" -> "Merge";
                    case "Quick Sort" -> "Quick";
                    default -> "Heap";
                };
                g2.drawString(shortName, x + (barW - fm.stringWidth(shortName)) / 2, top + chartH + 17);
            }
            g2.dispose();
        }
    }

    private void record(int[] a, int x, int y, boolean comparison, boolean move) {
        steps.add(new Step(a, x, y, comparison, move));
    }

    private void bubbleSort(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            for (int j = 0; j < a.length - i - 1; j++) {
                record(a, j, j + 1, true, false);
                if (a[j] > a[j + 1]) {
                    swap(a, j, j + 1);
                    record(a, j, j + 1, false, true);
                }
            }
        }
    }

    private void mergeSort(int[] a) {
        int n = a.length;
        int[] temp = new int[n];
        for (int width = 1; width < n; width *= 2) {
            for (int left = 0; left < n; left += 2 * width) {
                int mid = Math.min(left + width, n);
                int right = Math.min(left + 2 * width, n);
                if (mid >= right) continue;
                int i = left, j = mid, k = left;
                while (i < mid && j < right) {
                    record(a, i, j, true, false);
                    if (a[i] <= a[j]) temp[k++] = a[i++];
                    else temp[k++] = a[j++];
                }
                while (i < mid) temp[k++] = a[i++];
                while (j < right) temp[k++] = a[j++];
                for (int p = left; p < right; p++) {
                    a[p] = temp[p];
                    record(a, p, -1, false, true);
                }
            }
        }
    }

    private void quickSort(int[] a) {
        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{0, a.length - 1});
        while (!stack.isEmpty()) {
            int[] range = stack.pop();
            int low = range[0], high = range[1];
            if (low >= high) continue;
            int pivot = a[high];
            int i = low - 1;
            for (int j = low; j < high; j++) {
                record(a, j, high, true, false);
                if (a[j] < pivot) {
                    i++;
                    if (i != j) {
                        swap(a, i, j);
                        record(a, i, j, false, true);
                    }
                }
            }
            if (i + 1 != high) {
                swap(a, i + 1, high);
                record(a, i + 1, high, false, true);
            }
            int p = i + 1;
            stack.push(new int[]{low, p - 1});
            stack.push(new int[]{p + 1, high});
        }
    }

    private void heapSort(int[] a) {
        int n = a.length;
        for (int i = n / 2 - 1; i >= 0; i--) heapify(a, n, i);
        for (int end = n - 1; end > 0; end--) {
            swap(a, 0, end);
            record(a, 0, end, false, true);
            heapify(a, end, 0);
        }
    }

    private void heapify(int[] a, int size, int root) {
        while (true) {
            int largest = root;
            int left = 2 * root + 1;
            int right = 2 * root + 2;
            if (left < size) {
                record(a, root, left, true, false);
                if (a[left] > a[largest]) largest = left;
            }
            if (right < size) {
                record(a, largest, right, true, false);
                if (a[right] > a[largest]) largest = right;
            }
            if (largest == root) break;
            swap(a, root, largest);
            record(a, root, largest, false, true);
            root = largest;
        }
    }

    private void swap(int[] a, int i, int j) {
        int temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    private class BarPanel extends JPanel {
        BarPanel() {
            setBackground(PANEL);
            setBorder(new EmptyBorder(16, 12, 12, 12));
            setPreferredSize(new Dimension(800, 360));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (arr == null || arr.length == 0) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int gap = arr.length > 60 ? 2 : 4;
            int barWidth = Math.max(2,
                    (panelWidth - gap * (arr.length + 1)) / arr.length);
            int bottom = panelHeight - 24;
            int availableHeight = Math.max(50, panelHeight - 55);

            for (int i = 0; i < arr.length; i++) {
                int height = (int) ((arr[i] / 100.0) * availableHeight);
                int x = gap + i * (barWidth + gap);
                int y = bottom - height;

                if (i == activeA || i == activeB) g2.setColor(ACTIVE);
                else if (completed) g2.setColor(GREEN);
                else g2.setColor(BLUE);

                g2.fillRoundRect(x, y, barWidth, height,
                        barWidth > 5 ? 5 : 2, 5);
            }

            g2.setColor(new Color(78, 87, 112));
            g2.drawLine(5, bottom, panelWidth - 5, bottom);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
