import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpRequestEditor;

import java.awt.*;
import java.util.List;

/*[*]
 - custom "Redacted" tab on the REQUEST side of Repeater.
 - uses HttpTextPane for colored HTTP headers and line numbers.
 - supports auto-refresh via callback list.
 */
public class RedactedRequestEditor implements ExtensionProvidedHttpRequestEditor 
{

    private final HttpTextPane httpTextPane;
    private final List<RedactionRule> rules;
    private final List<Runnable> refreshCallbacks;
    private HttpRequest originalRequest;

    public RedactedRequestEditor(MontoyaApi api, EditorCreationContext context, List<RedactionRule> rules, List<Runnable> refreshCallbacks) 
    {
        this.rules = rules;
        this.refreshCallbacks = refreshCallbacks;

        this.httpTextPane = new HttpTextPane();

        // [/] register refresh callback ( see 18-03-2026 for :: notation and observer pattern)
        this.refreshCallbacks.add(this::refresh);
    }

    @Override
    public void setRequestResponse(HttpRequestResponse requestResponse) 
    {
        this.originalRequest = requestResponse.request();
        updateDisplay();
    }

    private void updateDisplay() 
    {
        if (originalRequest == null) return;

        String redacted = RedactionEngine.redact(originalRequest.toString(), rules);
        httpTextPane.setHttpText(redacted);
    }

    private void refresh() 
    {
        if (originalRequest != null) 
        {
            updateDisplay();
        }
    }

    @Override
    public HttpRequest getRequest() 
    {
        return originalRequest;
    }

    @Override
    public String caption() 
    {
        return "Redacted";
    }

    @Override
    public Component uiComponent() 
    {
        return httpTextPane;
    }

    @Override
    public boolean isEnabledFor(HttpRequestResponse requestResponse) 
    {
        return true;
    }

    @Override
    public Selection selectedData() 
    {
        return null;
    }

    @Override
    public boolean isModified() 
    {
        return false;
    }
}
