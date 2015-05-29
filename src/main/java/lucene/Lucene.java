package lucene;

import data.CrawlerObject;
import data.Data;
import config.LocalConfig;
import main.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.tr.TurkishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LazyDocument;
import org.apache.lucene.index.*;
import org.apache.lucene.queries.function.valuesource.TermFreqValueSource;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.bson.types.ObjectId;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public class Lucene {

    private static final Logger logger = LogManager.getLogger(Lucene.class);
    private static Lucene instance = new Lucene();


    protected LinkedList<IIndexListener> listeners = new LinkedList<IIndexListener>();
    protected float similarityThreshold;

    protected String indexFilePath;
    protected Directory indexDirectory;

    protected Analyzer analyzer;
    protected IndexWriter indexWriter;

    protected boolean created = true;


    public Lucene()  {

        this.similarityThreshold = Application.config.getSimilarityThreshold();
        this.indexFilePath = Application.config.getLuceneIndexPath();
        try {
            this.createIndexWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.debug("Lucene instance initiated");
    }

    public void addListener(IIndexListener listener) {
        listeners.add(listener);
    }

    public LinkedList<Data> neighbourSearch(ObjectId document_id) throws Exception {

        LinkedList<Data> docs = new LinkedList<Data>();
        IndexSearcher searcher =this.getIndexSearcher();

        Query query = new TermQuery(new Term("_id", document_id.toHexString()));
        TopDocs search = searcher.search(query, 1);

        logger.trace(search);

        if(search.totalHits < 1)
            throw new Exception("Document could not found! _id:" + document_id.toHexString());

        MoreLikeThis mlt = new MoreLikeThis(searcher.getIndexReader());
        mlt.setMinTermFreq(1);
        mlt.setMinDocFreq(1);
        mlt.setMaxQueryTerms(25);
        mlt.setAnalyzer(analyzer);
        mlt.setFieldNames(new String[] {"title","desc","text"});

        query = mlt.like(search.scoreDocs[0].doc);

        TopDocs similarDocs = searcher.search(query, 500);

        if(similarDocs.totalHits < 1) {
            logger.debug("no similar documents found for: " + document_id.toHexString());
            return null;
        }


        for(ScoreDoc scoreDoc : similarDocs.scoreDocs) {

            if(scoreDoc.score < similarityThreshold)
                break;

            // get document from searcher
            Document document = searcher.doc(scoreDoc.doc);

            // create data object
            Data d = new Data();
            d.similarity = scoreDoc.score;
            String _id = document.get("_id");

            // if _id is empty then its just a normalization data
            if(_id == null)
                continue;

            d._id = new ObjectId(_id);

            // if document itself founded
            if(d._id.equals(document_id))
                continue;

            d.title = document.get("title");
            d.desc = document.get("desc");



            // insert to docs
            docs.add(d);

        }
        return docs;

    }

    public CrawlerObject indexCrawlerObject(CrawlerObject d) throws IOException {
        return indexCrawlerObject(d, true);
    }

    protected CrawlerObject indexCrawlerObject(CrawlerObject d , boolean commit) {

        try {
            Document doc = new Document();

            FieldType type = new FieldType();
            type.setIndexed(true);
            type.setTokenized(false);
            type.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
            type.setStored(true);

            Field idFiled= new Field("_id", d._id.toHexString(), type);
            Field titleField = new Field("title",d.title, Lucene.getIndexedField());
            titleField.setBoost(3);
            Field descField = new Field("desc",d.desc, Lucene.getIndexedField());
            descField.setBoost(2);
            Field textField = new Field("text", d.text_clean, Lucene.getIndexedField());

            // combined text field for easy vector search on lucene
            StringBuilder sb = new StringBuilder(d.title).append("\n")
                    .append(d.desc).append("\n")
                    .append(d.text_clean).append("\n");
            Field combined = new Field("combined_text", sb.toString(), Lucene.getIndexedField());

            doc.add(idFiled);
            doc.add(titleField);
            doc.add(descField);
            doc.add(textField);
            doc.add(combined);

            this.indexWriter.addDocument(doc);

            if(commit) {
                this.indexWriter.commit();

                logger.trace("onIndexed event triggered _id:" + d._id.toHexString() + " title:" + d.title);
                for(IIndexListener l : listeners)
                    l.onIndexed(d);
            }

            return  d;
        } catch (IOException ioException) {
            logger.error(ioException.getMessage(), ioException);
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }


    }

    /**
     * Returns Lucene document of given data
     * @param _id _id of data
     * @return Lucene Document of given id
     */
    public Document getDocument(ObjectId _id) {
        try {
            IndexSearcher searcher = this.getIndexSearcher();
            Query query = new TermQuery(new Term("_id", _id.toHexString()));
            TopDocs search = searcher.search(query, 1);

            if(search.totalHits==0)
                return null;

            return searcher.getIndexReader().document(search.scoreDocs[0].doc);
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
            return null;
        }
    }

    public Map<String,Integer> getTerms(ObjectId _id) {
        try {
            IndexSearcher searcher = this.getIndexSearcher();
            Query query = new TermQuery(new Term("_id", _id.toHexString()));
            TopDocs search = searcher.search(query, 1);

            if (search.totalHits == 0) {
                logger.error("document not found: " + _id.toHexString());
                return null;
            }


            Map<String,Integer> map = new HashMap<>();

            Terms terms = getIndexReader().getTermVector(search.scoreDocs[0].doc, "combined_text");

            if(terms==null) {
                logger.error("terms not found: " + _id.toHexString());
                return null;
            }

            TermsEnum termsEnum = terms.iterator(null);
            BytesRef text = null;
            while ((text = termsEnum.next()) != null) {

                map.put(text.utf8ToString(), (int)termsEnum.totalTermFreq());
                // System.out.println("\t" + text.utf8ToString() + " " + termsEnum.docFreq() + " " + termsEnum.totalTermFreq());

            }
            return map;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

        /**
     * Returns Lucene instance
     * @return
     */
    public static Lucene getInstance() {
        return instance;
    }

    /**
     * Returns index writer object
     * @return
     */
    public IndexWriter getIndexWriter() {
        return indexWriter;
    }

    /**
     * Returns index reader object
     * @return
     */
    public IndexReader getIndexReader() throws IOException {

        return DirectoryReader.open(indexDirectory);
    }

    /**
     * Returns index searcher object
     * @return
     */
    public IndexSearcher getIndexSearcher () throws IOException {

        return new IndexSearcher(getIndexReader());
    }

    /**
     * return whether index in given directory created or not.
     * @return
     */
    public boolean isIndexRecreated() {
        return created;
    }

    /**
     * Invokes indexWriter commit.
     * @throws IOException
     */
    public void commit() throws IOException {
        indexWriter.commit();
    }

    /**
     * Initiates indexWriter for indexing documents.
     * @throws IOException
     */
    protected void createIndexWriter() throws IOException {

        analyzer=new TurkishAnalyzer();
        indexDirectory = SimpleFSDirectory.open(new File(indexFilePath));
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_4_10_4,analyzer);


        if(DirectoryReader.indexExists(indexDirectory)) {
            logger.trace("index found opening in append mode");
            created = false;
            conf.setOpenMode(IndexWriterConfig.OpenMode.APPEND);
        } else {
            logger.trace("index not found opening in create mode");
            conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        }
        this.indexWriter = new IndexWriter(indexDirectory, conf);
        this.indexWriter.commit();
    }

    /**
     * Closes lucene index gracefully.
     * @throws IOException
     */
    public void close() {
        try {
            this.indexWriter.close();
            this.indexDirectory.close();
        } catch (IOException e) {
            logger.debug("close error: " + e.getMessage());
        }


    }

    /**
     * Creates stored - indexed - analyzed field for lucene.
     * @return
     */
    public static FieldType getIndexedField() {

        FieldType type = new FieldType();
        type.setIndexed(true);
        type.setIndexOptions(FieldInfo.IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        type.setStored(true);
        type.setStoreTermVectors(true);
        type.setTokenized(true);
        return type;
    }

}
