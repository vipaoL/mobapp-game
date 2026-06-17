// SPDX-License-Identifier: LGPL-2.1-only

package io.github.vipaol.mobapp.game;

import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.foundation.NSOperationQueue;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.uikit.*;

import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.game.MenuCanvas;
import mobileapplication3.ui.IUIComponent;

public class IOSLauncher extends UIApplicationDelegateAdapter {
    private UIWindow window;
    private UIViewController rootViewController;
    private IUIComponent root = null;

    @Override
    public boolean didFinishLaunching(UIApplication application, UIApplicationLaunchOptions launchOptions) {
        rootViewController = new UIViewController();
        Logger.logToStdout(true);
        Platform.init(rootViewController);

        rootViewController.setView(RootContainer.getInst());

        window = new UIWindow(UIScreen.getMainScreen().getBounds());
        window.setRootViewController(rootViewController);
        window.makeKeyAndVisible();

        NSOperationQueue.getMainQueue().addOperation(() -> {
            try {
                if (root == null) {
                    Logger.log("delayed start: init menu");
                    root = new MenuCanvas();
                } else {
                    Logger.log("delayed start: skipping menu init");
                }
                RootContainer.setRootUIComponent(root);
                Logger.log("delayed start: done");
            } catch (Exception ex) {
                Logger.log(ex);
            }
        });

        return true;
    }

    @Override
    public boolean openURL(UIApplication app, NSURL url, UIApplicationOpenURLOptions options) {
        try {
            Logger.log("openURL(" + url.getPath() + ")");

            IUIComponent editorUI = FileOpenUtil.handleFileOpenURL(url);
            if (editorUI != null) {
                root = editorUI;
            }

            return true;
        } catch (Exception ex) {
            Logger.log(ex);
            return false;
        }
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            java.io.StringWriter sw = new java.io.StringWriter();
            throwable.printStackTrace(new java.io.PrintWriter(sw));
            System.err.println("CRASH");
            System.err.println(sw.toString());
        });

        try (NSAutoreleasePool pool = new NSAutoreleasePool()) {
            UIApplication.main(args, null, IOSLauncher.class);
        }
    }
}
