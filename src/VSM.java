import java.io.*;
import java.util.*;

import org.tartarus.snowball.ext.englishStemmer;

/**
 * Created by jinfenglin on 4/3/17.
 */
public class VSM {
    Map<String, List<String>> documents;
    Set<String> stopWords;
    TFIDFCalculator tfidfCalculator;

    VSM(Map<String, String> documents) throws IOException {
        stopWords = getStopWords();
        this.documents = tokenize(documents);
        preProcess();
        tfidfCalculator = new TFIDFCalculator();


    }

    private void preProcess() {
        removeStopWords();
        stemming();
    }

    private Map<String, List<String>> tokenize(Map<String, String> doc) {
        Map<String, List<String>> res = new HashMap<>();
        for (String name : doc.keySet()) {
            res.put(name, tokenzie(doc.get(name)));
        }
        return res;
    }

    private List<String> tokenzie(String str) {
        StringTokenizer st = new StringTokenizer(str);
        List<String> tokens = new ArrayList<>();
        while (st.hasMoreTokens()) {
            tokens.add(st.nextToken().toLowerCase());
        }
        return tokens;
    }

    private void removeStopWords() {
        for (String name : documents.keySet()) {
            List<String> tokens = documents.get(name);
            documents.put(name, removeStopWord(tokens));
        }
    }

    private List<String> removeStopWord(List<String> tokens) {
        List<String> noStopWords = new ArrayList<>();
        for (String token : tokens) {
            if (!stopWords.contains(tokens)) {
                noStopWords.add(token);
            }
        }
        return noStopWords;
    }

    private void stemming() {
        for (String name : documents.keySet()) {
            List<String> tokens = documents.get(name);
            documents.put(name, stemming(tokens));
        }
    }

    private List<String> stemming(List<String> tokens) {
        List<String> stem = new ArrayList<>();
        englishStemmer stemmer = new englishStemmer();
        for (String token : tokens) {
            stemmer.setCurrent(token);
            stemmer.stem();
            stem.add(stemmer.getCurrent());
        }
        return stem;
    }

    private Map<String, Double> vectorize(List<String> doc) {
        Map<String, Double> vec = new HashMap<>();
        for (String token : doc) {
            vec.put(token, tfidfCalculator.tfIdf(doc, new ArrayList<>(documents.values()), token));
        }
        return vec;
    }

    private Set<String> getStopWords() throws IOException {
        InputStream in = this.getClass().getResourceAsStream("/wordList/JavaKeyWord.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        Set<String> res = new HashSet<>();
        String line = "";
        while ((line = reader.readLine()) != null) {
            res.add(line);
        }
        return res;
    }

    private double cosine_similarity(Map<String, Double> v1, Map<String, Double> v2) {
        Set<String> both = new HashSet<>(v1.keySet());
        both.retainAll(v2.keySet());
        double sclar = 0, norm1 = 0, norm2 = 0;
        for (String k : both) sclar += v1.get(k) * v2.get(k);
        for (String k : v1.keySet()) norm1 += v1.get(k) * v1.get(k);
        for (String k : v2.keySet()) norm2 += v2.get(k) * v2.get(k);
        return sclar / Math.sqrt(norm1 * norm2);
    }

    public double getSimilarity(String doc1, String doc2) {
        Map<String, Double> v1 = vectorize(documents.get(doc1));
        Map<String, Double> v2 = vectorize(documents.get(doc2));
        return cosine_similarity(v1, v2);
    }

    public static void main(String[] args) throws IOException {
        FileReader fr1 = new FileReader("/Users/jinfenglin/Downloads/TLE_Scenario_Checker/src/Added_Class_Scenario.java");
        FileReader fr2 = new FileReader("/Users/jinfenglin/Downloads/TLE_Scenario_Checker/src/Added_Class_Scenario.java");
        BufferedReader b1 = new BufferedReader(fr1);
        BufferedReader b2 = new BufferedReader(fr2);
        Map<String, String> docs = new HashMap<>();
        String file1 = "";
        String file2 = "";
        String tmp = "";
        while ((tmp = b1.readLine()) != null) {
            file1 += tmp;
        }
        while ((tmp = b2.readLine()) != null) {
            file2 += tmp;
        }
        docs.put("file1", file1);
        docs.put("file2", file2);
        VSM vsm = new VSM(docs);
        double sim = vsm.getSimilarity("file1", "file2");
        System.out.println(sim);
    }
}
