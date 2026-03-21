import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

import javax.swing.*;
import java.util.List;

/*[*] 
 - create the shared rules list
 - create the refresh callbacks list
 - register a "Redact" menu in Burp's menu bar
 - register "Redacted" tabs on both request and response editors
 */
public class Extension implements BurpExtension 
{

    // shared rules list
    private final List<RedactionRule> rules = new java.util.concurrent.CopyOnWriteArrayList<>();

    // refresh callbacks
    private final List<Runnable> refreshCallbacks = new java.util.concurrent.CopyOnWriteArrayList<>();

    @Override
    public void initialize(MontoyaApi api) 
    {
        api.extension().setName("Redaction Filter");

        // popup dialog (hidden initially)
        RedactionDialog dialog = new RedactionDialog(rules, refreshCallbacks);

        // register a "Redact" menu in Burp's top menu bar
        api.userInterface().menuBar().registerMenu(createMenu(dialog));

        // register "Redacted" tab for requests
        api.userInterface().registerHttpRequestEditorProvider(
            context -> new RedactedRequestEditor(api, context, rules, refreshCallbacks)
        );

        // register "Redacted" tab for responses
        api.userInterface().registerHttpResponseEditorProvider(
            context -> new RedactedResponseEditor(api, context, rules, refreshCallbacks)
        );

        api.logging().logToOutput("Redaction Filter loaded!");
        api.logging().logToOutput("Go to: Redact → Configure Rules to add redaction rules.");
    }

    private JMenu createMenu(RedactionDialog dialog) 
    {
        JMenu menu = new JMenu("Redact");

        JMenuItem configureItem = new JMenuItem("Configure Rules");
        configureItem.addActionListener(e -> dialog.setVisible(true));
        menu.add(configureItem);

        return menu;
    }
}