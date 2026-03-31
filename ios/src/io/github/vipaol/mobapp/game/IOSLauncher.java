// SPDX-License-Identifier: LGPL-2.1-only

package io.github.vipaol.mobapp.game;

import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.*;
import mobileapplication3.platform.Platform;
import mobileapplication3.platform.ui.RootContainer;
import mobileapplication3.game.MenuCanvas;

public class IOSLauncher extends UIApplicationDelegateAdapter {
    private UIWindow window;
    private UIViewController rootViewController;

    @Override
    public boolean didFinishLaunching(UIApplication application, UIApplicationLaunchOptions launchOptions) {
        rootViewController = new UIViewController();
        Platform.init(rootViewController);

        RootContainer.createView(rootViewController);
        RootContainer.getInst().setFrame(UIScreen.getMainScreen().getBounds());
        rootViewController.setView(RootContainer.getInst());

        window = new UIWindow(UIScreen.getMainScreen().getBounds());
        window.setRootViewController(rootViewController);
        window.makeKeyAndVisible();

        RootContainer.setRootUIComponent(new MenuCanvas());

        return true;
    }

    public static void main(String[] args) {
        try (NSAutoreleasePool pool = new NSAutoreleasePool()) {
            UIApplication.main(args, null, IOSLauncher.class);
        }
    }
}
