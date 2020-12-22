import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;


public class checkIndexSearch {
    public static final String FILE_NAME = ".\\readertown.json";

    // поиск по автору
    public static void termQuery(LuceneIBSynonym index, String author) throws IOException, ParseException {
        RussianAnalyzer analyzer = new RussianAnalyzer();
        Term term = new Term("author", author);
        QueryParser qp = new QueryParser(author, analyzer);
        Query q = new TermQuery(term);
        List<Document> docs = index.searchIndex(qp.parse(q.toString()));
        for (Document doc : docs) {
            System.out.println(doc.get("title") + " == " + doc.get("author"));
        }
    }

    // поиск по названию
    public static void termQueryTitle(LuceneIBSynonym index, String title) throws IOException, ParseException {
        RussianAnalyzer analyzer = new RussianAnalyzer();
        Term term = new Term("title", title);
        QueryParser qp = new QueryParser(title, analyzer);
        Query q = new TermQuery(term);
        List<Document> docs = index.searchIndex(qp.parse(q.toString()));
        for (Document doc : docs) {
            System.out.println(doc.get("title") + " == " + doc.get("author"));
        }
    }

    // поиск по диапазону цен
    public static void termQueryNumber(LuceneIBSynonym index, int start, int end) throws IOException {
        Query query = LongPoint.newRangeQuery("price", start, end);
        List<Document> docs = index.searchIndex(query);
        for (Document doc : docs) {
            System.out.println(doc.get("title") + " == " + doc.get("author") + " == " + doc.get("price"));
        }
    }

    public static void main(String[] args) {
        try {
            LuceneIBSynonym luceneIndexBook = new LuceneIBSynonym(new RAMDirectory(), new SynonymAnalyzer());
            ParserJsonWithInfo.parserDataForIndex(FILE_NAME, luceneIndexBook);
            while(true){
                System.out.println("Выберите действие:\n1 - Поиск по автору\n" +
                        "2 - Поиск по названию\n3 - Поиск по цене\n0 - Выход");
                Scanner inp = new Scanner (System.in);
                String input = inp.nextLine();
                if (input.equals("1")){
                    System.out.print("Введите автора: ");
                    String aut = inp.nextLine();
                    System.out.println("Поиск по автору \"" + aut + "\":\n");
                    checkIndexSearch.termQuery(luceneIndexBook, aut.toLowerCase());
                }
                else if (input.equals("2")){
                    System.out.print("Введите название: ");
                    String name = inp.nextLine();
                    System.out.println("Поиск по названию \"" + name + "\":\n");
                    checkIndexSearch.termQueryTitle(luceneIndexBook, name.toLowerCase());
                }
                else if (input.equals("3")){
                    System.out.print("Введите минимальную цену: ");
                    int start = inp.nextInt();
                    System.out.print("Введите максимальную цену: ");
                    int end = inp.nextInt();
                    System.out.println("\nПоиск по цене от " + start + " до " + end + ":\n");
                    checkIndexSearch.termQueryNumber(luceneIndexBook, start, end);
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
