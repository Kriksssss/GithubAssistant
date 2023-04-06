package kriksss.github;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public class IconApp {

    private final TrayIcon trayIcon;

    public IconApp() {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit()
                    .createImage(getClass().getResource("/icon.png"));

            trayIcon = new TrayIcon(image, "Github Assistant");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("Github Assistant");

            tray.add(trayIcon);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    public void  setMenu(String login, List<RepositoryDescription> reposDesc) {
        PopupMenu popup = new PopupMenu();

        MenuItem acc = popup.add(new MenuItem(login));
        acc.addActionListener(e -> openInBrowser("https://github.com/" + login));

        MenuItem notificationAcc = new MenuItem("notifications");
        notificationAcc.addActionListener(e -> openInBrowser("https://github.com/notifications" ));


        Menu menu = new Menu("Repositories");
        reposDesc
                .forEach(repo -> {
                    String name = repo.getPrs().size() > 0
                            ? String.format("(%d) %s", repo.getPrs().size(), repo.getName())
                            : repo.getName();
                    Menu repoSM = new Menu(name);

                    MenuItem openInBrowser = new MenuItem("Open in browser");
                    openInBrowser.addActionListener(e ->
                            openInBrowser(repo.getRepository().getHtmlUrl().toString())
                    );

                    repoSM.add(openInBrowser);

                    if (repo.getPrs().size() > 0) {
                        repoSM.addSeparator();
                    }

                    repo.getPrs()
                            .forEach(pr -> {
                                MenuItem prMI = new MenuItem(pr.getTitle());
                                prMI.addActionListener(e ->
                                        openInBrowser(pr.getHtmlUrl().toString())
                                );
                                repoSM.add(prMI);
                            });

                    menu.add(repoSM);
                });

        popup.add(acc);
        popup.addSeparator();
        popup.add(notificationAcc);
        popup.add(menu);
        trayIcon.setPopupMenu(popup);
    }

    public void openInBrowser(String url) {
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.browse(new URL(url).toURI());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void showNotification(String title, String text) {
        trayIcon.displayMessage(title, text, TrayIcon.MessageType.INFO);
        }
    }

