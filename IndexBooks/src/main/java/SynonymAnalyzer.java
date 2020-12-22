import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.ru.RussianLightStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.CharsRef;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


// Анализатор с синонимами
public class SynonymAnalyzer extends Analyzer {
    // Создаем мап для синонимов
    private SynonymMap myMap;
    // Русские стоп-слова
    public static final CharArraySet RUS_STOP_WORDS_SET;

    public SynonymAnalyzer(){
        createSynMap();
    }

    // Создаем мап на основе файла с синонимами
    public void createSynMap(){
        try {
            // Создаем билдер для синонимов
            SynonymMap.Builder builder = new SynonymMap.Builder(true);
            // Считываем файл
            FileReader file = new FileReader(".\\synonym1.txt");
            BufferedReader reader = new BufferedReader(file);
            // считаем сначала первую строку
            String line = reader.readLine();
            while (line != null) {
                // Слова, для которого описаны синонимы
                String[] masStr = line.split("\\|");
                // Сами синонимы
                String[] tmp = masStr[1].split(" ");
                if (tmp.length > 0) {
                    // Добавляем пары слово - синоним в билдер
                    for (String elem : tmp){
                        builder.add(new CharsRef(masStr[0]), new CharsRef(elem), true);
                        builder.add(new CharsRef(elem), new CharsRef(masStr[0]), true);
                    }
                }
                line = reader.readLine();
            }
            // Создаем этот мап с синонимами
            myMap = builder.build();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    // Создание компонентов
    protected TokenStreamComponents createComponents(String s) {
        Tokenizer source = new StandardTokenizer(); // токинайзер
        TokenStream filter = new StandardFilter(source); // фильтр
        filter = new LowerCaseFilter(filter); // нижний регистр
        filter = new StopFilter(filter, RUS_STOP_WORDS_SET); // исключение стоп-слов
        filter = new RussianLightStemFilter(filter); // приведение к начальной форме, отсечение окончаний и пр.
        filter = new SynonymGraphFilter(filter, myMap, true); // добавляем еще и синонимы
        return new TokenStreamComponents(source, filter);
    }

    static {
        List<String> stopWords = Arrays.asList("и", "как", "но", "для", "если", "в", "на", "это", "эти", "нет", "не",
                "из", "от", "или", "такой", "чтобы", "этот", "этих", "когда", "тогда", "так", "вот", "ох", "до",
                "эх", "ах", "с", "со", "где", "бы", "кто", "зачем", "а", "да", "за");
        CharArraySet stopSet = new CharArraySet(stopWords, false);
        RUS_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet);
    }

}