import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LuceneIBSynonym {
    // Создаем директорию для хранения индекса и анализатор
    public static Directory memoryIndex;
    public static SynonymAnalyzer standardAnalyzer;

    public LuceneIBSynonym(RAMDirectory ramDirectory, SynonymAnalyzer analyzer){
        memoryIndex = ramDirectory;
        standardAnalyzer = analyzer;
    }

    // Добавляем документы в индекс
    public void indexDocument(String title, String author, Long countPage, Long price, String publisher, Long year)
            throws IOException {
        // Создаем конфигурацию: добавляем анализатор
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(standardAnalyzer);
        // Создаем writer в директорию с конфигурацией
        IndexWriter writer = new IndexWriter(memoryIndex, indexWriterConfig);
        // Создаем документ
        Document doc = new Document();

        // Добавляем поле title как текст и поле для сортировки
        doc.add(new SortedDocValuesField("title", new BytesRef(title)));
        doc.add(new TextField("title", title, Field.Store.YES));
        // Добавляем поле author как текст и как store (поле будет возвращаться при поиске)
        doc.add(new TextField("author", author, Field.Store.YES));
        doc.add(new LongPoint("countPage", countPage));
        doc.add(new StoredField("countPage", countPage));
        doc.add(new LongPoint("price", price));
        doc.add(new StoredField("price", price));
        doc.add(new TextField("publisher", publisher, Field.Store.YES));
        doc.add(new LongPoint("year", year));
        doc.add(new StoredField("year", year));

        // Добавляем документ в индекс
        writer.addDocument(doc);
        writer.close();
    }

    // Поиск по индексу
    public List<Document> searchIndex(Query query) throws IOException {
        IndexReader indexReader = DirectoryReader.open(memoryIndex);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        // Берем первые 400 документов
        TopDocs topDocs = searcher.search(query, 400);
        List<Document> docs = new ArrayList<Document>();
        // извлекаем найденные документы в список
        for (ScoreDoc sDoc : topDocs.scoreDocs){
            docs.add(searcher.doc(sDoc.doc));
        }
        return docs;
    }
}
