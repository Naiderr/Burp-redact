/*[*]
 - represents a single redaction rule. 
 - aach rule has:
 - name (the field to match, ex: authorization,password...)
 - type (what kind of field it is, ex: header,json...)
 */
public class RedactionRule 
{

    /*[*]
     - the 7 types of fields we can redact.
     - each type tells the engine HOW to search for the field in the HTTP text.
     */
    public enum RuleType 
    {
        HEADER,        // [/] hTTP header (e.g., Authorization: Bearer xyz)
        QUERY_PARAM,   // [/] uRL query parameter (e.g., ?api_key=abc)
        JSON_KEY,      // [/] jSON key-value pair (e.g., "password": "secret")
        ENDPOINT,      // [/] uRL path segment (e.g., /api/secret/data)
        COOKIE,        // [/] cookie value (e.g., Cookie: session=abc123)
        FORM_PARAM,    // [/] uRL-encoded body param (e.g., password=secret)
        CUSTOM_REGEX   // [/] user-defined regex pattern (matches anything)
    }

    private final String name;     // [/] the field name or regex pattern
    private final RuleType type;   // [/] what kind of field it is ( enum type )

    public RedactionRule(String name, RuleType type) 
    {
        this.name = name;
        this.type = type;
    }

    public String getName() 
    {
        return name;
    }

    public RuleType getType() 
    {
        return type;
    }

    /*[*]
     - displayed in the rules table in the popup.
     - ex: "password (COOKIE)"
     */
    @Override
    public String toString() 
    {
        return name + " (" + type.name() + ")";
    }
}
