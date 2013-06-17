import com.google.common.base.Predicate;
import org.w3c.dom.Document;

import javax.swing.JFrame;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class TestHarness {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    showBrowser();
                } catch (Exception e) {
                    System.err.print(e.getMessage());
                }
            }
        });
    }

    private static void showBrowser() throws URISyntaxException, IOException {
        JFrame frame = new JFrame("JxBrowser Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BrowserPanel browserPanel = new BrowserPanel();
        browserPanel.init();
        browserPanel.loadEmbeddedApplication(new URI("resource:/app-test.html"));
        browserPanel.waitReady(new Predicate<Document>() {
            @Override
            public boolean apply(org.w3c.dom.Document input) {
                return true;
            }
        });
        browserPanel.registerCallbacks();
        frame.getContentPane().add(browserPanel);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
}
