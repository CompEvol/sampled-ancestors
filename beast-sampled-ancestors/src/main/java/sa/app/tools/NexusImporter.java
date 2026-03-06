package sa.app.tools;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

/**
 * @author Alexandra Gavryushkina
 */

public class NexusImporter {

    private String[] trees;

    //private ArrayList<Integer> labels = new ArrayList<Integer>();

    private Reader reader;

    private int labelCount;

    public String[] getTrees() {
        return trees;
    }

    public int getLabelCount() {
        return labelCount;
    }

       public NexusImporter(Reader newReader) {
        reader = newReader;
        importFromReader();
    }

    public void importFromReader() {

        skipUntil(reader, ';');
        skipUntil(reader, '=');
        labelCount = readInteger(reader);
        for (int i=0; i < 4; i++)
            skipUntil(reader, ';');
        importTrees();


    }

    public void importTrees() {

        ArrayList<String> tmp = new ArrayList<String>();

        boolean finish;

        finish = skipUntilCharOrEnd(reader, '=');

        while (finish) {
            tmp.add(readStringBeforeSemicolon(reader));
            finish = skipUntilCharOrEnd(reader, '=');
        }

        trees = tmp.toArray(new String[0]);
    }

    public int readInteger(Reader read) {

        String str = readStringBeforeSemicolon(reader);

        return Integer.parseInt(str);
    }

    public void skipUntil(Reader reader, char lastCh) {

        int ch;

        do {
            try {
                ch = reader.read();
            } catch (IOException e) {
                return;
            }

        } while ((char)ch != lastCh && ch != -1);

    }

    public boolean skipUntilCharOrEnd(Reader reader, char lastCh) {

        int ch;

        do {
            try {
                ch = reader.read();
            } catch (IOException e) {
                return false;
            }

        } while ((char)ch != lastCh && ch != -1);

        return (ch != -1);
    }

    // Reads string before semicolon ignoring whitespaces
    public String readStringBeforeSemicolon(Reader reader) {

        String string = new String();

        int ch;
        do {
            try {
                ch = reader.read();
                if ((char)ch != ';')
                    string += (char)ch;
            } catch (IOException e) {
                return null;
            }
        } while ((char)ch != ';' && ch != -1);

        return string.trim();
    }
}
