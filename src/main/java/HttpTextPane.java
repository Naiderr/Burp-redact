import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

public class HttpTextPane extends JPanel 
{

    private JTextPane textPane;
    private JTextArea lineNumbers;

    public HttpTextPane() 
    {
        setLayout(new BorderLayout());

        textPane = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() 
            {
                return true; 
            }
        };
        textPane.setEditable(false);
        textPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textPane.setMargin(new Insets(2, 6, 2, 6));

        lineNumbers = new JTextArea();
        lineNumbers.setEditable(false);
        lineNumbers.setFont(new Font("Monospaced", Font.PLAIN, 12));
        lineNumbers.setForeground(new Color(153, 153, 153));
        lineNumbers.setBackground(new Color(245, 245, 245));
        lineNumbers.setMargin(new Insets(2, 6, 2, 6));
        lineNumbers.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(221, 221, 221)));

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setRowHeaderView(lineNumbers);
        scrollPane.setBorder(null);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void setHttpText(String text) 
    {
        if (text == null || text.trim().isEmpty()) 
        {
            textPane.setText("");
            lineNumbers.setText("");
            return;
        }

        StyledDocument doc = textPane.getStyledDocument();

        try 
        {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException e) 
        {
            e.printStackTrace();
        }

        String[] lines = text.split("\n", -1);
        boolean isHeader = true;
        boolean isFirstLine = true;

        for (int i = 0; i < lines.length; i++) 
        {
            String currentLine = lines[i].replace("\r", ""); 

            if (isFirstLine == true) 
            {
                // [/] first line is special color
                addText(doc, currentLine, new Color(111, 66, 193), true);
                isFirstLine = false;
            } else if (isHeader == true && currentLine.equals("")) 
            {
                // [/] done with headers
                isHeader = false;
                addText(doc, currentLine, new Color(51, 51, 51), false);
            } else if (isHeader == true && currentLine.contains(":")) 
            {
                int colonPos = currentLine.indexOf(":");
                String name = currentLine.substring(0, colonPos + 1);
                String val = currentLine.substring(colonPos + 1);

                addText(doc, name, new Color(0, 86, 179), true);
                processRedactions(doc, val);
            } else 
            {
                processRedactions(doc, currentLine);
            }

            if (i < lines.length - 1) 
            {
                addText(doc, "\n", new Color(51, 51, 51), false);
            }
        }

        // [/] set line numbers
        StringBuilder sb = new StringBuilder();
        int lineCount = lines.length;
        int lengthOfDigits = String.valueOf(lineCount).length();

        for (int number = 1; number <= lineCount; number++) 
        {
            String formatStr = "%" + lengthOfDigits + "d";
            sb.append(String.format(formatStr, number));
            if (number < lineCount) 
            {
                sb.append("\n");
            }
        }
        lineNumbers.setText(sb.toString());

        textPane.setCaretPosition(0);
    }

    private void processRedactions(StyledDocument doc, String input) 
    {
        if (input.contains("****")) 
        {
            // [/] split it exactly by 4 stars
            String[] segments = input.split("\\*\\*\\*\\*", -1);
            for (int i = 0; i < segments.length; i++) 
            {
                addText(doc, segments[i], new Color(51, 51, 51), false);
                if (i < segments.length - 1) 
                {
                    addText(doc, "****", new Color(224, 62, 62), true);
                }
            }
        } else 
        {
            addText(doc, input, new Color(51, 51, 51), false);
        }
    }

    private void addText(StyledDocument doc, String s, Color c, boolean bold) {
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, c);
        StyleConstants.setFontFamily(style, "Monospaced");
        StyleConstants.setFontSize(style, 12);
        
        if (bold == true) 
        {
            StyleConstants.setBold(style, true);
        }

        try 
        {
            doc.insertString(doc.getLength(), s, style);
        } catch (BadLocationException e) 
        {
            e.printStackTrace();
        }
    }
}
