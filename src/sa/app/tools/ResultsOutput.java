package sa.app.tools;

import java.io.PrintStream;

/**
 * An interface for output of a table to various formats including for example HTML, plain text and Markdown.
 */
public interface ResultsOutput {

    public void beginTableOutput(String caption, String[] columnNames);

    public void outputRow(String[] values);

    public void endTableOutput();

    public void line(String line);

    public class HTML implements ResultsOutput {

        PrintStream out;

        public HTML(PrintStream out) {
            this.out = out;
        }

        @Override
        public void beginTableOutput(String caption, String[] columnNames) {
            out.append("<table>\n");
            out.append("  <caption>");
            out.append(caption);
            out.append("</caption>\n");

            out.append("  <tr>");
            for (int i = 0; i < columnNames.length; i++) {
                out.append("<th>");
                out.append(columnNames[i]);
                out.append("</th>");
            }
            out.append("</tr>\n");
        }

        @Override
        public void outputRow(String[] values) {
            out.append("  <tr>");
            for (String value : values) {

                value = value.replaceAll("<", "&lt;");
                value = value.replaceAll(">", "&gt;");

                out.append("<td>");
                out.append(value);
                out.append("</td>");
            }
        }

        @Override
        public void endTableOutput() {
            out.append("</table>\n");
        }

        @Override
        public void line(String line) {
            out.append(line);
            out.append("<br>\n");
        }
    }

    public class TabDelimitedPlainText implements ResultsOutput {

        PrintStream out;

        public TabDelimitedPlainText(PrintStream out) {
            this.out = out;
        }

        @Override
        public void beginTableOutput(String caption, String[] columnNames) {
            out.append(caption);
            out.append("\n");
            outputRow(columnNames);
        }

        @Override
        public void outputRow(String[] values) {
            out.append(values[0]);
            for (int i = 1; i < values.length; i++) {
                out.append("\t");
                out.append(values[i]);
            }
            out.append("\n");
        }

        @Override
        public void endTableOutput() {}

        @Override
        public void line(String line) {
            out.append(line);
            out.append("\n");
        }
    }
}