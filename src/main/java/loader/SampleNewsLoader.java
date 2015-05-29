package loader;

import lucene.Lucene;
import main.Application;
import org.apache.lucene.document.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by adem on 10.03.2015.
 */
public class SampleNewsLoader {

    final File folder = new File(Application.config.getSampleNewsPath());
    final Lucene lucene = Lucene.getInstance();
    protected int done = 0;


    public SampleNewsLoader() {

    }

    public void load() throws IOException {

        loadFilesRecursively(folder);

        lucene.getIndexWriter().commit();
    }



    protected void loadFilesRecursively(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                System.out.println(fileEntry);
                loadFilesRecursively(fileEntry);
            } else {
                try {
                    BufferedReader br = new BufferedReader(new FileReader(fileEntry));
                    Document doc = new Document();

                    Field titleField = null;
                    Field textField;

                    try {
                        StringBuilder sb = new StringBuilder();
                        String line = br.readLine();
                        titleField = new Field("title", line, Lucene.getIndexedField());
                        boolean firstline = true;
                        while (line != null) {
                            if(firstline) {
                                firstline = false;
                            } else {
                                sb.append(line);
                                sb.append(System.lineSeparator());
                            }

                            line = br.readLine();
                        }
                        textField = new Field("text", sb.toString(), Lucene.getIndexedField());

                        doc.add(titleField);
                        doc.add(textField);

                        lucene.getIndexWriter().addDocument(doc);

                        done++;
                        if(done%1000 == 0) {
                            System.out.println("done: " + done);
                        }

                    } finally {
                        br.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
