package loader;

import lucene.Lucene;
import main.Helper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by adem on 11.03.2015.
 */
public class MysqlNewsLoader {

    protected final Connection con;
    protected final Lucene lucene = Lucene.getInstance();
    protected int done = 0;

    public MysqlNewsLoader() {
        this.con = Helper.getInstance().getConnection();
    }

    public void load() throws SQLException, IOException {

        String sql = "SELECT `id`,`title`,`desc`,`text_clean` FROM data";

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            Document doc = new Document();

            Field idFiled= new IntField("id", rs.getInt("id"), Field.Store.YES);
            Field titleField = new Field("title", rs.getString("title"), Lucene.getIndexedField());
            titleField.setBoost(3);
            Field descField = new Field("desc", rs.getString("desc"), Lucene.getIndexedField());
            descField.setBoost(2);
            Field textField = new Field("text", rs.getString("text_clean"), Lucene.getIndexedField());
            doc.add(idFiled);
            doc.add(titleField);
            doc.add(textField);

            lucene.getIndexWriter().addDocument(doc);
            done++;
            if(done%1000 == 0) {
                System.out.println("done: " + done);
            }
        }
        lucene.getIndexWriter().commit();

    }

}
