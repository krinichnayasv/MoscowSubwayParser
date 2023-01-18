import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;


public class ParseHTML {

    private  String nameLine = "";
    private  String numberLine = "";
    private  String stationName = "";
    private  String stationConnect = "";
    private  String lineConnect = "";
    private String fileName;

    public ParseHTML() {

    }

    private JSONArray linesArray = new JSONArray();
    private JSONObject stationObject = new JSONObject();
    private JSONArray connectArrays = new JSONArray();
    private JSONArray stationArray = new JSONArray();
    private JSONObject connectObject = new JSONObject();
    private JSONArray connectArray = new JSONArray();

    public  void parseHTMLToJSon(String fileName, String url ) throws Exception {
        String html = "";

        try {
            Document document = (Document) Jsoup.connect(url).maxBodySize(0).get();
            html = document.toString();
        } catch (IOException ex) {
            ex.getStackTrace();
        }

        Document doc = Jsoup.parse(html);
        //lines
        String line = doc.select("*.js-metro-line*").toString();
        String[] lines = line.split("\n");



        for(int i = 0; i < lines.length; i++) {
            JSONObject linesObject = new JSONObject();
            String nameLine = "";
            String numberLine = "";
            String ch = "data-line=";
            int startNum = lines[i].indexOf(ch) + ch.length();
            int endNum = lines[i].indexOf(">", startNum);
            numberLine = lines[i].substring(startNum + 1, endNum - 1);
            int end = lines[i].lastIndexOf("<");
            int start = lines[i].lastIndexOf(">", end);
            nameLine = lines[i].substring(start + 1, end);
            linesObject.put("number", numberLine);
            linesObject.put("name", nameLine);
            linesArray.add(linesObject);

        }

        // stations
        String station = doc.select("*.js-metro-stations*").toString();
        String[] stations = station.split("\n");

        String numLine = "";
        for(int i = 0; i < stations.length; i++) {

            String reg = "<span class=\"name\">";
            String regexp = "<span class=\"t-icon-metroln ln-";
            if (stations[i].contains("js-metro-stations t-metrostation-list-table")) {
                String ch = "data-line=";
                int startNum = stations[i].indexOf(ch) + ch.length();
                int endNum = stations[i].indexOf(" ", startNum);
                numLine = stations[i].substring(startNum + 1, endNum - 1);
            }
            if (stations[i].contains(reg)) {
                int startSt = stations[i].indexOf(reg) + reg.length();
                int endSt = stations[i].indexOf("<", startSt);
                stationName = stations[i].substring(startSt, endSt);
              //  System.out.println("Stait: " + numLine + " - " + stationName);
                stationArray.add(stationName);

                // connections
                if (i < stations.length && stations[i].contains("title=")) {
                    int n = 0;
                    do {
                        int startNumCon = stations[i].indexOf(regexp) + regexp.length();
                        int endNumCon = stations[i].indexOf("\"", startNumCon);
                        lineConnect = stations[i].substring(startNumCon, endNumCon);
                        int startName = stations[i].indexOf("«");
                        int endName = stations[i].indexOf("»", startName);
                        stationConnect = stations[i].substring(startName + 1, endName);

                        if (n == 0) {
                           // System.out.println( "Conn_from: " + numLine + " " + stationName + ": ");
                            connectObject.put("line", numLine);
                            connectObject.put("station", stationName);
                                connectArray.add(connectObject);
                           // System.out.println(connectObject);
                          //  System.out.println(connectArray);
                        }
                        connectObject = new JSONObject();
                       // connectObject.clear();
                            connectObject.put("line", lineConnect);
                            connectObject.put("station", stationConnect);
                                connectArray.add(connectObject);
                      //  System.out.println(connectObject);
                      //  System.out.println(connectArray);
                        connectObject = new JSONObject();
                           // System.out.println( "Conn_to: " + lineConnect + " " + stationConnect);
                            n = n + 1;

                        stations[i] = stations[i].substring(endName);
                    } while (stations[i].contains("title="));
                    connectArrays.add(connectArray);
                  //  System.out.println(connectArray);
                }
                connectArray = new JSONArray();
            }

            if ((i != 0 && stations[i].contains("</div>")) || i == stations.length -1) {

                stationObject.put(numLine, stationArray);
             //   System.out.println(numLine + stationArray);
                stationArray = new JSONArray();

            }
        }


        JSONObject sampleObject = new JSONObject();
        sampleObject.put("stations", stationObject);
        sampleObject.put("connections", connectArrays);
        sampleObject.put("lines", linesArray);



      //  Files.write(Paths.get(filename), sampleObject.toJSONString().getBytes());

        try {
            FileWriter writer = new FileWriter(fileName);
            writer.write(sampleObject.toJSONString());
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void parseJsonFile() {

        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonFile = (JSONObject) parser.parse(readJsonFile("data/mapMoscow.json"));

            JSONArray linesArray = (JSONArray) jsonFile.get("lines");
            //   linesArray.forEach(System.out::println);

            JSONArray connectionsArray = (JSONArray) jsonFile.get("connections");
            //   connectionsArray.forEach(System.out::println);

            System.out.println("Количество переходов в метро: " + connectionsArray.size());
            System.out.println("\n" + "Stations: ");

            JSONObject stationsObject = (JSONObject) jsonFile.get("stations");
            stationsObject.keySet().stream().sorted(Comparator.comparingInt(s -> Integer.parseInt(((String)s)
                    .replaceAll("[^\\d]", "")))).forEach(lineNumberObject ->
            {
                JSONArray stationsArray = (JSONArray) stationsObject.get(lineNumberObject);
                int stationsCount = stationsArray.size();
                System.out.println("Номер линиии " + lineNumberObject + " - колличество станций : " + stationsCount);
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    private static String readJsonFile(String fileName) {
        StringBuilder builder = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(Paths.get(fileName));
            for (String line: lines) {
                builder.append(line);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return builder.toString();
    }



}
