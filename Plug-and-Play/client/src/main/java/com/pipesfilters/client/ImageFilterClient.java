package com.pipesfilters.client;

import com.pipesfilters.core.ProcessingContext;
import com.pipesfilters.orchestrator.ParallelOrchestrator;
import com.pipesfilters.registry.ComponentManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.pipesfilters.plugins.Plugin;
import com.pipesfilters.plugins.TerminalPlugin;

public class ImageFilterClient extends JFrame {

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 750;

    private final java.util.List<JCheckBox> filterCheckBoxes = new java.util.ArrayList<>();
    private final java.util.Map<JCheckBox, Plugin> checkBoxPluginMap = new java.util.LinkedHashMap<>();
    private final JPanel filterRadioPanel = new JPanel();
    private final JLabel originalImageLabel = new JLabel("Imagen Inicial", SwingConstants.CENTER);
    private final JPanel filteredImagesPanel = new JPanel();
    private final JTextArea consoleArea = new JTextArea(8, 40);
    private final JTextArea metadataArea = new JTextArea(8, 30);

    private byte[] currentImageBytes;
    private String currentImageName;
    private Path outputDir;
    private final ComponentManager componentManager = new ComponentManager();

    public ImageFilterClient() {
        super("Aplicacion de procesamiento de imagenes con filtros con Arquitectura Plugin");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        add(buildLeftPanel(), BorderLayout.WEST);
        add(buildRightPanel(), BorderLayout.CENTER);

        refreshPlugins();
    }

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setPreferredSize(new Dimension(380, HEIGHT));

        JButton loadImageBtn = new JButton("Cargar Imagen");
        styleButton(loadImageBtn);
        loadImageBtn.addActionListener(e -> loadImage());

        JButton loadComponentBtn = new JButton("Cargar plugin manualmente");
        styleButton(loadComponentBtn);
        loadComponentBtn.addActionListener(e -> loadExternalComponent());

        JButton loadFolderBtn = new JButton("Cargar todos los plugins");
        styleButton(loadFolderBtn);
        loadFolderBtn.addActionListener(e -> loadPluginsFolder());

        JPanel filterPanel = createTitledPanel("Componentes (filtros)");
        filterPanel.setLayout(new BorderLayout());
        filterRadioPanel.setLayout(new BoxLayout(filterRadioPanel, BoxLayout.Y_AXIS));
        filterRadioPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        JScrollPane filterScroll = new JScrollPane(filterRadioPanel);
        filterPanel.add(filterScroll, BorderLayout.CENTER);

        JPanel spacer = new JPanel();
        spacer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));

        JButton runBtn = new JButton("Ejecutar filtro seleccionado");
        styleButton(runBtn);
        runBtn.addActionListener(e -> runSelectedFilter());

        JPanel consolePanel = createTitledPanel("Salida de mensajes");
        consolePanel.setLayout(new BorderLayout());
        consoleArea.setEditable(false);
        consoleArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane consoleScroll = new JScrollPane(consoleArea);
        consolePanel.add(consoleScroll, BorderLayout.CENTER);

        panel.add(loadImageBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(loadComponentBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(loadFolderBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(filterPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(runBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(consolePanel);

        return panel;
    }

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 0, 15, 15));

        JPanel imagesPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        JPanel originalPanel = createTitledPanel("Imagen Inicial");
        originalPanel.setLayout(new BorderLayout());
        originalImageLabel.setBorder(LineBorder.createGrayLineBorder());
        originalPanel.add(originalImageLabel, BorderLayout.CENTER);

        JPanel filteredPanel = createTitledPanel("Imagenes con filtros");
        filteredPanel.setLayout(new BorderLayout());
        filteredImagesPanel.setLayout(new GridLayout(0, 2, 10, 10));
        filteredImagesPanel.setBorder(LineBorder.createGrayLineBorder());
        JScrollPane filteredScroll = new JScrollPane(filteredImagesPanel);
        filteredPanel.add(filteredScroll, BorderLayout.CENTER);

        imagesPanel.add(originalPanel);
        imagesPanel.add(filteredPanel);

        JPanel metadataPanel = createTitledPanel("Metadata extraida");
        metadataPanel.setLayout(new BorderLayout());
        metadataArea.setEditable(false);
        metadataArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane metadataScroll = new JScrollPane(metadataArea);
        metadataPanel.add(metadataScroll, BorderLayout.CENTER);
        metadataPanel.setPreferredSize(new Dimension(320, HEIGHT));

        panel.add(imagesPanel, BorderLayout.CENTER);
        panel.add(metadataPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(
                LineBorder.createGrayLineBorder(),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12),
                new Color(0x003E68)
        ));
        return panel;
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(0x1E3A8A));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setPreferredSize(new Dimension(300, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void loadImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Imagenes (*.png, *.jpg, *.jpeg)", "png", "jpg", "jpeg"
        ));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path path = chooser.getSelectedFile().toPath();
        try {
            currentImageBytes = Files.readAllBytes(path);
            currentImageName = path.getFileName().toString();

            BufferedImage img = ImageIO.read(new ByteArrayInputStream(currentImageBytes));
            originalImageLabel.setIcon(new ImageIcon(scale(img, 420, 280)));
            originalImageLabel.setText("");

            log("Imagen cargada: " + currentImageName + " (" + currentImageBytes.length + " bytes)");

        } catch (Exception ex) {
            logError("Error cargando imagen: " + ex.getMessage());
        }
    }

    private void loadExternalComponent() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JAR de plugin", "jar"));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path jarPath = chooser.getSelectedFile().toPath();
        try {
            componentManager.installPluginJar(jarPath);
            refreshPlugins();
            log("Plugin cargado: " + jarPath.getFileName());
        } catch (Exception ex) {
            logError("Error cargando plugin: " + ex.getMessage());
        }
    }

    private void loadPluginsFolder() {
        Path folder = Paths.get("plugins").toAbsolutePath().normalize();
        if (!Files.isDirectory(folder)) {
            logError("No existe la carpeta de plugins: " + folder);
            return;
        }
        try {
            componentManager.installPluginFolder(folder);
            refreshPlugins();
            log("Plugins cargados desde: " + folder);
        } catch (Exception ex) {
            logError("Error cargando carpeta de plugins: " + ex.getMessage());
        }
    }

    private void refreshPlugins() {
        filterCheckBoxes.clear();
        checkBoxPluginMap.clear();
        filterRadioPanel.removeAll();

        Map<String, Plugin> plugins = componentManager.discoverPlugins();

        boolean first = true;
        for (Plugin plugin : plugins.values().stream().sorted(java.util.Comparator.comparing(Plugin::id)).toList()) {
            if (plugin instanceof TerminalPlugin) {
                continue;
            }
            JCheckBox check = new JCheckBox(plugin.id());
            check.setSelected(first);
            check.setFont(new Font("SansSerif", Font.PLAIN, 12));
            filterCheckBoxes.add(check);
            checkBoxPluginMap.put(check, plugin);
            filterRadioPanel.add(check);
            first = false;
        }

        filterRadioPanel.revalidate();
        filterRadioPanel.repaint();
    }

    private void runSelectedFilter() {
        if (currentImageBytes == null) {
            logError("Debes cargar una imagen primero.");
            return;
        }

        List<Plugin> selectedPlugins = filterCheckBoxes.stream()
                .filter(AbstractButton::isSelected)
                .map(checkBoxPluginMap::get)
                .toList();
        if (selectedPlugins.isEmpty()) {
            logError("Selecciona al menos un filtro de la lista.");
            return;
        }

        Map<String, Plugin> discovered = componentManager.discoverPlugins();
        boolean hasTerminal = discovered.values().stream().anyMatch(p -> p instanceof TerminalPlugin);
        if (!hasTerminal) {
            logError("No hay un plugin terminal (persistence) cargado. Cargalo manualmente antes de ejecutar.");
            return;
        }

        List<String> selectedIds = selectedPlugins.stream()
                .map(Plugin::id)
                .toList();
        Map<String, Plugin> suffixToPlugin = new java.util.LinkedHashMap<>();
        for (Plugin plugin : selectedPlugins) {
            String suffix = plugin.suffix();
            if (suffix != null && !suffix.isBlank()) {
                suffixToPlugin.put(suffix.toLowerCase(), plugin);
            }
        }

        try {
            outputDir = Paths.get("result", "client-output-" + Instant.now().getEpochSecond()).toAbsolutePath().normalize();
            Files.createDirectories(outputDir);

            ProcessingContext input = new ProcessingContext(currentImageName, currentImageBytes);
            ParallelOrchestrator orchestrator = new ParallelOrchestrator(componentManager);
            List<ProcessingContext> results = orchestrator.run(List.of(input), outputDir, selectedIds);

            findAndShowResult(suffixToPlugin);
            showMetadata(results.isEmpty() ? input : results.get(0));
            log("Filtros ejecutados: " + selectedIds + " -> " + outputDir);

        } catch (Exception ex) {
            logError("Error ejecutando filtro: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showMetadata(ProcessingContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("Archivo: ").append(ctx.name()).append(System.lineSeparator());
        sb.append("Formato: ").append(ctx.getMetadata("format", String.class).orElse("N/A")).append(System.lineSeparator());
        sb.append("Dimensiones: ")
                .append(ctx.getMetadata("width", Integer.class).map(String::valueOf).orElse("N/A"))
                .append("x")
                .append(ctx.getMetadata("height", Integer.class).map(String::valueOf).orElse("N/A"))
                .append(System.lineSeparator());
        sb.append("Tamanio original: ").append(ctx.originalSize()).append(System.lineSeparator());
        sb.append("SHA256: ").append(ctx.getMetadata("sha256", String.class).orElse("N/A")).append(System.lineSeparator());
        sb.append("Ubicacion: ").append(ctx.getMetadata("ubication", String.class).orElse("N/A")).append(System.lineSeparator());
        sb.append("Plugins aplicados: ").append(ctx.getMetadata("appliedPlugins", String.class).orElse("N/A")).append(System.lineSeparator());
        ctx.getMetadata("metadataError", String.class).ifPresent(err -> sb.append("Error metadata: ").append(err).append(System.lineSeparator()));

        metadataArea.setText(sb.toString());
        metadataArea.setCaretPosition(0);
    }

    private void findAndShowResult(Map<String, Plugin> suffixToPlugin) throws Exception {
        filteredImagesPanel.removeAll();
        if (outputDir == null || !Files.isDirectory(outputDir)) {
            filteredImagesPanel.revalidate();
            filteredImagesPanel.repaint();
            return;
        }

        Map<String, java.util.List<Path>> filesBySuffix = new java.util.LinkedHashMap<>();
        try (var paths = Files.list(outputDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> !p.getFileName().toString().equals("resultado.txt"))
                    .forEach(p -> {
                        suffixToPlugin.keySet().stream()
                                .filter(s -> p.getFileName().toString().toLowerCase().contains(s))
                                .findFirst()
                                .ifPresent(s -> filesBySuffix.computeIfAbsent(s, k -> new java.util.ArrayList<>()).add(p));
                    });
        }

        if (filesBySuffix.isEmpty()) {
            filteredImagesPanel.setLayout(new BorderLayout());
            filteredImagesPanel.add(new JLabel("No se encontro imagen resultante", SwingConstants.CENTER), BorderLayout.CENTER);
            filteredImagesPanel.revalidate();
            filteredImagesPanel.repaint();
            return;
        }

        filteredImagesPanel.setLayout(new GridLayout(0, 2, 10, 10));
        for (Map.Entry<String, java.util.List<Path>> entry : filesBySuffix.entrySet()) {
            Plugin plugin = suffixToPlugin.get(entry.getKey());
            String title = plugin != null ? plugin.id() : entry.getKey();
            for (Path file : entry.getValue()) {
                try {
                    byte[] bytes = Files.readAllBytes(file);
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
                    if (img != null) {
                        JPanel imageCard = new JPanel(new BorderLayout());
                        imageCard.setBorder(BorderFactory.createTitledBorder(LineBorder.createGrayLineBorder(), title));
                        JLabel label = new JLabel(new ImageIcon(scale(img, 420, 280)), SwingConstants.CENTER);
                        imageCard.add(label, BorderLayout.CENTER);
                        filteredImagesPanel.add(imageCard);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        filteredImagesPanel.revalidate();
        filteredImagesPanel.repaint();
    }

    private BufferedImage scale(BufferedImage original, int maxWidth, int maxHeight) {
        if (original == null) {
            return null;
        }
        double scale = Math.min(
                (double) maxWidth / original.getWidth(),
                (double) maxHeight / original.getHeight()
        );
        int w = (int) (original.getWidth() * scale);
        int h = (int) (original.getHeight() * scale);
        BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, w, h, null);
        g.dispose();
        return scaled;
    }

    private void log(String message) {
        consoleArea.append("[" + Instant.now() + "] " + message + "\n");
        consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
    }

    private void logError(String message) {
        consoleArea.append("[" + Instant.now() + "] ERROR: " + message + "\n");
        consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ImageFilterClient().setVisible(true));
    }
}
