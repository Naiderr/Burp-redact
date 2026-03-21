import java.util.List;
import java.util.regex.Pattern;

/*
 - the redaction engine => HTTP text + list of rules,
 - returns a redacted version with matching values replaced by "****".
 - this class is used by BOTH the request and response editor tabs.
 - it never modifies the original request/response objects — only works on strings.
 */
public class RedactionEngine {

    private static final String REPLACEMENT = "****";

    /* [*]
     - apply all redaction rules to the text (in our case, HTTP request or response).
     - @param text  Raw HTTP text (request or response)
     - @param rules List of redaction rules
     - @return      The text with matching values replaced by ****
     */
    public static String redact(String text, List<RedactionRule> rules) {
        if (text == null || text.isEmpty() || rules == null || rules.isEmpty()) {
            return text;
        }

        String result = text;
        for (RedactionRule rule : rules) {
            result = applyRule(result, rule);
        }
        return result;
    }

    /* [*]
     - apply a single rule based on its type.
     - each type uses a different regex pattern to find the value.
     */
    private static String applyRule(String text, RedactionRule rule) 
    {
        String name = Pattern.quote(rule.getName()); 
        String pattern;

        switch (rule.getType()) {

            case HEADER:    
                // [/] matches:  HeaderName: any value until end of line
                // [/] example:  Authorization: eyJhbG...  =>  Authorization: ****
                pattern = "(?i)(" + name + ":\\s*)(.+)";
                return text.replaceAll(pattern, "$1" + REPLACEMENT);

            case QUERY_PARAM:
                // [/] matches:  paramName=value  (value ends at & or whitespace or end of line)
                // [/] example:  ?api_key=abc123&bro=bro  =>  ?api_key=****&bro=****
                pattern = "(?i)(" + name + "=)([^&\\s\\r\\n]*)";
                return text.replaceAll(pattern, "$1" + REPLACEMENT);

            case JSON_KEY:
                // [/] matches:  "keyName": "value"  or  "keyName":"value"
                // [/] also matches:  "keyName": 12345  (numeric values)
                // [/] example:  "password": "secret123"  =>  "password": "****"
                pattern = "(?i)(\"" + rule.getName() + "\"\\s*:\\s*)\"[^\"]*\"";
                String result = text.replaceAll(pattern, "$1\"" + REPLACEMENT + "\"");
                // [/] also handle numeric/boolean values: "key": 123 or "key": true
                pattern = "(?i)(\"" + rule.getName() + "\"\\s*:\\s*)([0-9]+\\.?[0-9]*|true|false|null)";
                result = result.replaceAll(pattern, "$1" + REPLACEMENT);
                return result;

            case ENDPOINT:
                // [/] replaces the path segment that matches the name
                // [/] example: name="/api/secret" in "GET /api/secret/data HTTP/1.1"
                // [/]          =>  "gET /****/data HTTP/1.1"
                pattern = "(?i)" + name;
                return text.replaceAll(pattern, REPLACEMENT);

            case COOKIE:
                // [/] matches:  cookieName=value  within Cookie header (value ends at ; or end of line)
                // [/] example:  Cookie: session=abc123; other=x  =>  Cookie: session=****; other=x
                pattern = "(?i)(" + name + "=)([^;\\s\\r\\n]*)";
                return text.replaceAll(pattern, "$1" + REPLACEMENT);

            case FORM_PARAM:
                // [/] matches:  paramName=value  (value ends at & or end of line)
                // [/] example:  username=admin&password=bro  =>  username=admin&password=****
                pattern = "(?i)(" + name + "=)([^&\\r\\n]*)";
                return text.replaceAll(pattern, "$1" + REPLACEMENT);

            case CUSTOM_REGEX:
                // [/] the name IS the regex pattern — no escaping needed
                // [/] replaces anything matching the user's regex with ****
                try {
                    return text.replaceAll(rule.getName(), REPLACEMENT);
                } catch (Exception e) {
                    // [/] if the regex is invalid, return original text 
                    return text;
                }

            default:
                return text;
        }
    }
}
