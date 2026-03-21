import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.Selection;
import burp.api.montoya.ui.editor.extension.EditorCreationContext;
import burp.api.montoya.ui.editor.extension.ExtensionProvidedHttpResponseEditor;

import java.awt.*;
import java.util.List;

/*[*]
 - custom "Redacted" tab on the RESPONSE side of Repeater.
 - uses HttpTextPane for colored HTTP headers and line numbers.
 - supports auto-refresh via callback list.
 */
public class RedactedResponseEditor implements ExtensionProvidedHttpResponseEditor 
{

    private final HttpTextPane httpTextPane;
    private final List<RedactionRule> rules;
    private final List<Runnable> refreshCallbacks;
    private HttpResponse originalResponse;

    public RedactedResponseEditor(MontoyaApi api, EditorCreationContext context,List<RedactionRule> rules, List<Runnable> refreshCallbacks) 
    {
        this.rules = rules;
        this.refreshCallbacks = refreshCallbacks;

        this.httpTextPane = new HttpTextPane();

        this.refreshCallbacks.add(this::refresh);
    }

    @Override
    public void setRequestResponse(HttpRequestResponse requestResponse) 
    {
        this.originalResponse = requestResponse.response();
        updateDisplay();
    }

    private void updateDisplay() 
    {
        if (originalResponse != null) 
        {
            String redacted = RedactionEngine.redact(originalResponse.toString(), rules);
            httpTextPane.setHttpText(redacted);
        }
    }

    private void refresh() 
    {
        if (originalResponse != null) 
        {
            updateDisplay();
        }
    }

    @Override
    public HttpResponse getResponse() 
    {
        return originalResponse;
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
        return requestResponse.response() != null;
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
