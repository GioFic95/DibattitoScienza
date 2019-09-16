package ds;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import twitter4j.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ds.Utils.*;

public class LeuceneTweetSearch {
    private final static String WEBPATH = new File(LeuceneTweetSearch.class.getResource("web").getFile()).toPath().toString();

    private static void executeQuery(String dirName, List<LuceneTweet> results, Query q) throws IOException {
        print(q);

        Path path = Paths.get(dirName);
        Directory dir = new SimpleFSDirectory(path);
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        TotalHitCountCollector collector = new TotalHitCountCollector();

        searcher.search(q, collector);
        int tot = collector.getTotalHits();
        print(tot);
        TopDocs docs = searcher.search(q, Math.max(1, tot));
        ScoreDoc[] hits = docs.scoreDocs;

        for (ScoreDoc hit : hits) {
            Document doc = searcher.doc(hit.doc);
            results.add(new LuceneTweet(doc));
        }
    }

    // TODO to be improved
    public static List<LuceneTweet> searchByKeyword(String dirName, String keyword) {
        List<LuceneTweet> results = new ArrayList<>();

        try {
            Analyzer analyzer = new SimpleAnalyzer();
            QueryParser qp = new QueryParser("Text", analyzer);
            Query q = qp.parse(keyword);
            executeQuery(dirName, results, q);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return results;
    }

    public static List<LuceneTweet> searchByHashtag(String dirName, String hashtag) {
        List<LuceneTweet> results = new ArrayList<>();

        try {
            Analyzer analyzer = new WhitespaceAnalyzer();
            QueryParser qp = new QueryParser("Tags", analyzer);
            Query q = qp.parse(hashtag);
            executeQuery(dirName, results, q);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return results;
    }

    public static List<LuceneTweet> searchByAuthor(String dirName, long author) {
        List<LuceneTweet> results = new ArrayList<>();

        try {
            Analyzer analyzer = new WhitespaceAnalyzer();
            QueryParser qp = new QueryParser("UserId", analyzer);
            Query q = qp.parse(String.valueOf(author));
            executeQuery(dirName, results, q);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return results;
    }

    public static List<LuceneTweet> searchByAuthor(String dirName, String user) {
        String[] fields = new String[] {"UserId", "UserName"};
        BooleanClause.Occur[] flags = new BooleanClause.Occur[fields.length];
        Arrays.fill(flags, BooleanClause.Occur.SHOULD);
        List<LuceneTweet> results = new ArrayList<>();

        try {
            Analyzer analyzer = new WhitespaceAnalyzer();
            Query q = MultiFieldQueryParser.parse(user, fields, flags, analyzer);
            executeQuery(dirName, results, q);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return results;
    }

    // TODO to be corrected
    public static List<LuceneTweet> searchByDate(String dirName, String date) {
        List<LuceneTweet> results = new ArrayList<>();

        try {
            Analyzer analyzer = new WhitespaceAnalyzer();
            QueryParser qp = new QueryParser("Date", analyzer);
            Query q = qp.parse(date);
            executeQuery(dirName, results, q);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return results;
    }

    // TODO split exception handling
    // TODO improve connections with DB
    public static void findScience(String dirPath, String dbURL, String dbUser, String dbPsw) {
        Twitter twitter0 = new TwitterFactory(CONF0).getInstance();
        Twitter twitter1 = new TwitterFactory(CONF1).getInstance();
        long counter = 0;

        try {
            String templatePath = Utils.class.getResource("template.html").getFile();
            File htmlFile = new File(templatePath);
            List<String> topics = getTopicsList();

            for (String keyword : topics) {
                org.jsoup.nodes.Document page = Jsoup.parse(htmlFile, "utf-8");
                Element table = page.selectFirst("#my-table");

                List<LuceneTweet> results = searchByKeyword(dirPath, keyword);

                for (LuceneTweet lt : results) {
                    long id = Long.valueOf(lt.getDoc().get("TweetId"));
                    String userName = lt.getDoc().get("UserName");
                    long inReplyId = -2;
                    long retweetId = -2;
                    Timestamp timestamp = new Timestamp(0);
//                    String url = "<a href=\"" + lt.getDoc().get("Url") + "\">" + lt.getDoc().get("Url") + "</a>";
//                    String text = "";
                    String url = "";
                    OEmbedRequest oEmbedRequest = new OEmbedRequest(id, url);
                    OEmbed embed = null;
                    while (embed == null) {
                        try {
                            if (counter % 2 == 0)
                                embed = twitter0.getOEmbed(oEmbedRequest);
                            else
                                embed = twitter1.getOEmbed(oEmbedRequest);
                        } catch (TwitterException ex) {
                            ex.printStackTrace();
                            try {
                                Thread.sleep(1000 * 60 * 5);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                System.exit(1);
                            }
                        }
                    }
                    String html = embed.getHtml();
                    counter++;

                    try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPsw)) {
                        try (Statement stmt = conn.createStatement()) {
                            try (ResultSet DBtweet = stmt.executeQuery(
                                    "select * from tweet where id = " + id + ";")) {
                                DBtweet.next();
                                inReplyId = DBtweet.getLong("in_reply_id");
                                retweetId = DBtweet.getLong("retweet_id");
//                                text = DBtweet.getString("text");
                                timestamp = DBtweet.getTimestamp("datetime");
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    String row = "<tr>\n" +
                            "            <td class=\"tg-0pky\">" + id + "</td>\n" +             // index
                            "            <td class=\"tg-phtq\">" + userName + "</td>\n" +       //index
                            "            <td class=\"tg-0pky\">" + inReplyId + "</td>\n" +      // DB
                            "            <td class=\"tg-phtq\">" + retweetId + "</td>\n" +      // DB
                            "            <td class=\"tg-0pky\">" + timestamp + "</td>\n" +      // DB
//                            "            <td class=\"tg-phtq\">" + url + "</td>\n" +            // index
//                            "            <td class=\"tg-0pky\">" + keyword + "</td>\n" +        // search
//                            "            <td class=\"tg-phtq\">" + text + "</td>\n" +           // DB
                            "            <td class=\"tg-phtq\">" + html + "</td>\n" +           // DB
                            "        </tr>";

                    table.append(row);
                }

                String outPage = page.toString();
                Files.write(Paths.get(WEBPATH, keyword + ".html"),
                        Arrays.asList(outPage.split("\n")));
            }

            File index = Paths.get(WEBPATH, "index.html").toFile();
            org.jsoup.nodes.Document indexPage = Jsoup.parse(index, "utf-8");
            Element list = indexPage.selectFirst("#my-list");
            list.empty();

            Files.list(Paths.get(WEBPATH))
                    .forEach(path -> {
                        String f = path.getFileName().toString();
                        if (! f.equals("index.html")) {
                            String res = "<li><a href=\"" + f + "\">" + f + "</a></li>";
                            list.append(res);
                            System.out.println(f);
                        }
                    });

            String outPage = indexPage.toString();
            Files.write(Paths.get(WEBPATH, "index.html"),
                    Arrays.asList(outPage.split("\n")));
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public static void findScience(String dirPath, TweetDB.DBProperty dbp) {
        findScience(dirPath, dbp.dbURL, dbp.dbUser, dbp.dbPsw);
    }
}
