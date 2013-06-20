import com.google.common.base.Predicate;
import com.teamdev.jxbrowser.Browser;
import com.teamdev.jxbrowser.BrowserFactory;
import com.teamdev.jxbrowser.BrowserFunction;
import com.teamdev.jxbrowser.BrowserType;
import com.teamdev.jxbrowser.gecko15.xpcom.util.PrivilegeProvider;
import com.teamdev.jxbrowser.prompt.DefaultPromptService;
import com.teamdev.jxbrowser.script.ScriptErrorEvent;
import com.teamdev.jxbrowser.script.ScriptErrorListener;
import com.teamdev.jxbrowser.script.ScriptErrorType;
import net.miginfocom.swing.MigLayout;
import org.w3c.dom.Document;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

public class BrowserPanel extends JPanel {
    protected Browser webBrowser;

    public String loadResourceContent(String resourceUrl) throws IOException {

        InputStream inputStream = this.getClass().getResourceAsStream(resourceUrl);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    public BrowserPanel() {
        PrivilegeProvider privilegeProvider = new PrivilegeProvider();
        privilegeProvider.enableUniversalXPConnectForLocalFiles();
        webBrowser = BrowserFactory.createBrowser(BrowserType.Mozilla15);
    }


    public void init() {
        setLayout(new MigLayout("", "[]", "[]"));

        webBrowser.getServices().getScriptErrorWatcher().addScriptErrorListener(new ScriptErrorListener() {
            public void scriptErrorHappened(ScriptErrorEvent scriptErrorEvent) {
                if (scriptErrorEvent.getType() == ScriptErrorType.ERROR || scriptErrorEvent.getType() ==
                        ScriptErrorType.EXCEPTION) {
                    System.err.println(String.format("Error: %s\r\n%s:%s", scriptErrorEvent.getMessage(),
                            scriptErrorEvent.getSourceFile(), scriptErrorEvent.getLineNumber()));
                }
            }
        });

        webBrowser.getServices().setPromptService(new DefaultPromptService());

        add(webBrowser.getComponent(), "width :1000:, height :700:, grow");
    }

    public void registerCallbacks() {
        webBrowser.registerFunction("errorCallback", new BrowserFunction() {

            @Override
            public Object invoke(Object... args) {
                onError(String.format("Error: %s", args));
                return null;
            }
        });

        webBrowser.registerFunction("bridgeCallback", new BrowserFunction() {
            @Override
            public Object invoke(final Object... args) {
                onBridgeCallback(args);
                return "success";
            }
        });
    }

    private void onBridgeCallback(Object[] args) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Bridge callback with args: ");
        for(Object arg: args) {
            sb.append(arg).append(", ");
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null, sb.toString(), "Bridge Callback", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    private void onError(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public void loadEmbeddedApplication(URI uri) throws IOException {
        if ("resource".equals(uri.getScheme())) {
            webBrowser.setContent(loadResourceContent(uri.getPath()));
        } else {
            webBrowser.navigate(uri.toString());
        }
    }

    public void waitReady(Predicate<Document> readySensor) {
        webBrowser.waitReady();
        while(!readySensor.apply(webBrowser.getDocument())) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void executeJavascript(String javascriptTemplate, Object... args) {
        String javascriptCommand = String.format(javascriptTemplate, args);
        webBrowser.executeScript(String.format("executeJavascript('%s')", javascriptCommand));
    }

    public void disposeBrowser() {
        remove(webBrowser.getComponent());
        webBrowser.dispose();
    }

}
