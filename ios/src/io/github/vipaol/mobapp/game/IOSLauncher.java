// SPDX-License-Identifier: LGPL-2.1-only

package io.github.vipaol.mobapp.game;

import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.foundation.NSOperationQueue;
import org.robovm.apple.uikit.*;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.game.MenuCanvas;

public class IOSLauncher extends UIApplicationDelegateAdapter {
    private UIWindow window;
    private UIViewController rootViewController;

    @Override
    public boolean didFinishLaunching(UIApplication application, UIApplicationLaunchOptions launchOptions) {
        rootViewController = new UIViewController();
        Logger.logToStdout(true);
        Platform.init(rootViewController);

        RootContainer.createView(rootViewController);
        CGRect screenBounds = UIScreen.getMainScreen().getBounds();
        RootContainer.getInst().setFrame(screenBounds);

        rootViewController.setView(RootContainer.getInst());

        window = new UIWindow(screenBounds);
        window.setRootViewController(rootViewController);
        window.makeKeyAndVisible();

        NSOperationQueue.getMainQueue().addOperation(() -> {
            try {
                System.out.println("Delayed start: Init MenuCanvas");
                RootContainer.setRootUIComponent(new MenuCanvas());
                System.out.println("Delayed start: Done");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        return true;
    }

    public static void main(String[] args) {
        try (NSAutoreleasePool pool = new NSAutoreleasePool()) {
            UIApplication.main(args, null, IOSLauncher.class);
        }
    }
}
