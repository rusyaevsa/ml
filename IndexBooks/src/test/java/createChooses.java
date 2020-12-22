import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.RAMDirectory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class createChooses {
    public static final String FILE_NAME = ".\\readertown.json";

    // индекс по автору
    public static void termQuery(LuceneIBSynonym index, String author, String filename) throws IOException, ParseException {
        RussianAnalyzer analyzer = new RussianAnalyzer();
        Term term = new Term("author", author);
        QueryParser qp = new QueryParser(author, analyzer);
        Query q = new TermQuery(term);
        List<Document> docs = index.searchIndex(qp.parse(q.toString()));
        for (Document doc : docs) {
            System.out.println(doc.get("title") + " == " + doc.get("author"));
        }
        writeJsonFile(author, filename, docs);
    }

    // индекс по названию
    public static void termQueryTitle(LuceneIBSynonym index, String title, String filename) throws IOException, ParseException {
        RussianAnalyzer analyzer = new RussianAnalyzer();
        Term term = new Term("title", title);
        QueryParser qp = new QueryParser(title, analyzer);
        Query q = new TermQuery(term);
        List<Document> docs = index.searchIndex(qp.parse(q.toString()));
        for (Document doc : docs) {
            System.out.println(doc.get("title") + " == " + doc.get("author"));
        }
        writeJsonFile(title, filename, docs);
    }

    // индекс по издательству
    public static void termQueryPublisher(LuceneIBSynonym index, String publ, String filename) throws IOException, ParseException {
        RussianAnalyzer analyzer = new RussianAnalyzer();
        Term term = new Term("publisher", publ);
        QueryParser qp = new QueryParser(publ, analyzer);
        Query q = new TermQuery(term);
        List<Document> docs = index.searchIndex(qp.parse(q.toString()));
        for (Document doc : docs) {
            System.out.println(doc.get("title") + " == " + doc.get("author") + " == " + doc.get("publisher"));
        }
        writeJsonFile(publ, filename, docs);
    }

    // индекс по нескольким полям
    public static void termQueryNew(LuceneIBSynonym index, String word, String filename) throws IOException {
        Query q = new TermQuery(new Term("publisher", word));
        Query q2 = new TermQuery(new Term("title", word));
        Query q3 = new TermQuery(new Term("author", word));
        BooleanQuery booleanQuery = new BooleanQuery.Builder()
                .add(q, BooleanClause.Occur.SHOULD)
                .add(q2, BooleanClause.Occur.SHOULD)
                .add(q3, BooleanClause.Occur.SHOULD)
                .build();
        List<Document> docs = index.searchIndex(booleanQuery);
        for (Document doc : docs) {
            System.out.println(doc.get("title") + " == " + doc.get("author") + " == " + doc.get("publisher"));
        }
        writeJsonFile(word, filename, docs);
    }

    // запись результата в json
    public static void writeJsonFile(String term, String filename, List<Document> docs){
        filename += ".json";
        JSONObject mainObj = new JSONObject();
        mainObj.put("query", term);
        JSONArray messages = new JSONArray();
        for (Document doc : docs){
            JSONObject object = new JSONObject();
            object.put("title", doc.get("title"));
            object.put("author",  doc.get("author"));
            object.put("publisher", doc.get("publisher"));
            messages.add(object);
        }
        mainObj.put("answer", messages);
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(mainObj.toJSONString());
            writer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            LuceneIBSynonym LuceneIBSynonym = new LuceneIBSynonym(new RAMDirectory(), new SynonymAnalyzer());
            ParserJsonWithInfo.parserDataForIndex(FILE_NAME, LuceneIBSynonym);
            while(true){
                System.out.println("Выберите действие:\n1 - Поиск по автору\n" +
                        "2 - Поиск по названию\n3 - Поиск по издательству\n4 - Поиск по всем трем полям\n0 - Выход");
                Scanner inp = new Scanner (System.in);
                String input = inp.nextLine();
                if (input.equals("1")){
                    System.out.print("Введите автора: ");
                    String aut = inp.nextLine();
                    System.out.print("Введите имя файла для сохранения результата поиска: ");
                    String filename = inp.nextLine();
                    System.out.println("Поиск по автору \"" + aut + "\":\n");
                    createChooses.termQuery(LuceneIBSynonym, aut.toLowerCase(), filename);
                }
                else if (input.equals("2")){
                    System.out.print("Введите название: ");
                    String name = inp.nextLine();
                    System.out.print("Введите имя файла для сохранения результата поиска: ");
                    String filename = inp.nextLine();
                    System.out.println("Поиск по названию \"" + name + "\":\n");
                    createChooses.termQueryTitle(LuceneIBSynonym, name.toLowerCase(), filename);
                }
                else if (input.equals("3")){
                    System.out.print("Введите издательство: ");
                    String publisher = inp.nextLine();
                    System.out.print("Введите имя файла для сохранения результата поиска: ");
                    String filename = inp.nextLine();
                    System.out.println("\nПоиск по издательству \"" + publisher + "\":\n");
                    createChooses.termQueryPublisher(LuceneIBSynonym, publisher.toLowerCase(), filename);
                }
                else if (input.equals("4")) {
                    System.out.print("Введите слово: ");
                    String name = inp.nextLine();
                    System.out.print("Введите имя файла для сохранения результата поиска: ");
                    String filename = inp.nextLine();
                    System.out.println("Поиск\"" + name + "\":\n");
                    createChooses.termQueryNew(LuceneIBSynonym, name.toLowerCase(), filename);
                }
                else if (input.equals("0")){
                    break;
                }
            }

        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("Error");
        }
    }
}
