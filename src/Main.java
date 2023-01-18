
public class Main {

    public static void main(String[] args) throws Exception {

        // создание файла JSON
                ParseHTML parsing = new ParseHTML();
                parsing.parseHTMLToJSon("data/mapMoscow.json", "https://skillbox-java.github.io/");

                // парсинг файла JSON
              parsing.parseJsonFile();

    }

}
