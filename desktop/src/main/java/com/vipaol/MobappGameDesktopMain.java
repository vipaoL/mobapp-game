// SPDX-License-Identifier: GPL-3.0-or-later

package com.vipaol;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import mobileapplication3.editor.EditorSettings;
import mobileapplication3.editor.EditorUI;
import mobileapplication3.editor.MGStructs;
import mobileapplication3.editor.StructureViewerComponent;
import mobileapplication3.editor.elements.Element;
import mobileapplication3.game.DebugMenu;
import mobileapplication3.game.MenuCanvas;
import mobileapplication3.platform.FileUtils;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.MobappDesktopMain;
import mobileapplication3.platform.PlatformSettings;
import mobileapplication3.platform.ui.Graphics;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.ui.IUIComponent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MobappGameDesktopMain extends MobappDesktopMain {

    private IUIComponent root;

    public static void main(String[] args) {
        CLI cli = CLI.getCli();
        try {
            OptionSet options = cli.parser.parse(args);

            if (options.has(cli.help)) {
                cli.parser.printHelpOn(System.out);
                System.exit(0);
            }

            if (options.has(cli.thumbnailOut)) {
                List<File> nonOptions = (List<File>) options.nonOptionArguments();
                if (nonOptions == null || nonOptions.isEmpty()) {
                    Logger.logErr("Error: Input level file is required for thumbnail generation");
                    System.exit(1);
                }
                File inputFile = nonOptions.get(0);

                File outputFile = options.valueOf(cli.thumbnailOut);
                int size = options.valueOf(cli.thumbnailSize);

                FileUtils.setStoragePath(options.valueOf(cli.workdir));
                generateThumbnailAndExit(inputFile, outputFile, size);
            }
        } catch (Exception ex) {
            Logger.logErr("Error parsing arguments:");
            Logger.log(ex);
            System.exit(1);
        }

        new MobappGameDesktopMain(args);
    }

    private static void generateThumbnailAndExit(File inputFile, File outputFile, int size) {
        try {
            Element.highContrast = true;
            Element[] elements = MGStructs.readMGStruct(inputFile.getPath());
            if (elements == null) {
                elements = new Element[0];
            }

            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setClip(0, 0, size, size);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Graphics g = new Graphics(g2d);

            StructureViewerComponent viewer = new StructureViewerComponent(elements);
            viewer.init();
            viewer.setSize(size, size);
            viewer.postInit();
            viewer.setVisible(true);
            viewer.setFocused(true);

            viewer.setBgColor(0x000000);
            viewer.centerView(2000);
            viewer.roundBg(false);

            viewer.paint(g, 0, 0, size, size, false);

            g2d.dispose();

            ImageIO.write(image, "png", outputFile);
            System.exit(0);
        } catch (Exception ex) {
            Logger.logErr("Failed to generate thumbnail:");
            Logger.log(ex);
            System.exit(1);
        }
    }

    protected void parseArgs(String[] args) {
        CLI cli = CLI.getCli();
        try {
            OptionSet options = cli.parser.parse(args);
            DebugMenu.isDebugEnabled = options.has(cli.debug);
            Logger.logToStdout(options.has(cli.verbose) || DebugMenu.isDebugEnabled);
            FileUtils.setStoragePath(options.valueOf(cli.workdir));

            if (options.has(cli.fullscreen)) {
                PlatformSettings.setFullscreenModeOverride(true);
            } else if (options.has(cli.noFullscreen)) {
                PlatformSettings.setFullscreenModeOverride(false);
            }

            if (options.has(cli.bw)) {
                PlatformSettings.setBlackAndWhiteModeOverride(true);
            } else if (options.has(cli.noBw)) {
                PlatformSettings.setBlackAndWhiteModeOverride(false);
            }

            if (options.has(cli.fontSize)) {
                PlatformSettings.setFontSizeOverride(options.valueOf(cli.fontSize));
            }

            List<File> nonOptions = (List<File>) options.nonOptionArguments();
            if (nonOptions != null && !nonOptions.isEmpty()) {
                if (nonOptions.size() > 1) {
                    throw new IllegalArgumentException("Too many arguments");
                } else {
                    root = openFile(nonOptions.get(0));
                }
            }
        } catch (Exception e) {
            Logger.log(e);
            System.err.println("Error: " + e.getMessage());
            System.out.println();
            try {
                cli.parser.printHelpOn(System.out);
            } catch (IOException ex) {
                System.err.println("Use --help for usage information.");
            }
            System.exit(1);
        }
    }

    private static IUIComponent openFile(File file) {
        Element[] elements = MGStructs.readMGStruct(file.getPath());
        if (elements == null) {
            elements = new Element[0];
        }
        String name = file.getName();
        int mode = name.endsWith(".mgstruct") ? EditorUI.MODE_STRUCTURE : EditorUI.MODE_LEVEL;
        String newPath = (mode == EditorUI.MODE_STRUCTURE ?
                EditorSettings.getStructsFolderPath() :
                EditorSettings.getLevelsFolderPath())
                + FileUtils.SEP + name;
        return new EditorUI(mode, elements, newPath).setViewMode(true);
    }

    public MobappGameDesktopMain(String[] args) {
        super(args);
        if (root == null) {
            root = new MenuCanvas();
        }
        RootContainer.setRootUIComponent(root);
    }

    private static class CLI {
        private static CLI cli = null;

        final OptionParser parser = new OptionParser();
        final OptionSpec<Void> debug = parser.acceptsAll(Arrays.asList("debug", "d"), "Enable debug mode");
        final OptionSpec<Void> verbose = parser.acceptsAll(Arrays.asList("verbose", "v"), "Print logs to stdout");
        final OptionSpec<String> workdir = parser.accepts("workdir", "Directory for game settings, records and custom content")
                .withRequiredArg()
                .describedAs("directory")
                .defaultsTo(FileUtils.getStoragePath());
        final OptionSpec<Integer> fontSize = parser.accepts("font-size", "Override the default font size")
                .withRequiredArg()
                .ofType(Integer.class)
                .defaultsTo(PlatformSettings.DEFAULT_FONT_SIZE);
        final OptionSpec<Void> fullscreen = parser.accepts("fullscreen", "Enable fullscreen mode");
        final OptionSpec<Void> noFullscreen = parser.accepts("no-fullscreen", "Disable fullscreen mode");
        final OptionSpec<Void> bw = parser.acceptsAll(Arrays.asList("black-and-white", "bw"), "Enable black and white mode");
        final OptionSpec<Void> noBw = parser.acceptsAll(Arrays.asList("no-black-and-white", "no-bw"), "Disable black and white mode");
        final OptionSpec<File> thumbnailOut = parser.accepts("thumbnail-out", "Path to save generated PNG thumbnail")
                .withRequiredArg()
                .ofType(File.class)
                .describedAs("output.png");
        final OptionSpec<Integer> thumbnailSize = parser.accepts("thumbnail-size", "Size of the thumbnail")
                .withRequiredArg()
                .ofType(Integer.class)
                .defaultsTo(128);
        final OptionSpec<Void> help = parser.acceptsAll(Arrays.asList("help", "h", "?"), "Show help").forHelp();

        private CLI() {
            parser.nonOptions("Path to level or structure to open in view mode")
                    .ofType(File.class)
                    .describedAs("*.mgstruct or *.mglvl");
        }

        static CLI getCli() {
            if (cli == null) {
                cli = new CLI();
            }
            return cli;
        }
    }
}