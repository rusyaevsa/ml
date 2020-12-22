import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

// Парсим Json с данными сайта
public class ParserJsonWithInfo {
    public static void parserDataForIndex(String fileName, LuceneIBSynonym luceneIndexBook)
        throws IOException, ParseException {
        Object obj = new JSONParser().parse(new FileReader(fileName));
        JSONArray jo = (JSONArray) obj;
        Iterator joIterator = jo.iterator();
        while (joIterator.hasNext()) {
            JSONObject book = (JSONObject) joIterator.next();
            String title = book.get("title") == null ? "null" : (String) book.get("title");
            String author = book.get("author") == null ? "null" : (String) book.get("author");
            Long countPage = book.get("count_page") == null ? -1 : (Long) book.get("count_page");
            Long price = book.get("price") == null ? -1 : (Long) book.get("price");
            String publisher = book.get("publisher") == null ? "null" : (String) book.get("publisher");
            Long year = book.get("year") == null ? 0 : (Long) book.get("year");
            luceneIndexBook.indexDocument(title, author, countPage, price, publisher, year);
        }
    }
}
