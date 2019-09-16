package ds;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Date;

import static ds.twitter4jWrapper.Twitter4jUtils.*;
import static ds.Utils.*;

public class TweetDB {

    /**
     * A class to hold parameters needed to access to a DB
     */
    static class DBProperty{
        String dbURL;
        String dbUser;
        String dbPsw;

        public DBProperty(){}

        @Override
        public String toString() {
            return "DBProperty{" +
                    "dbURL='" + dbURL + '\'' +
                    ", dbUser='" + dbUser + '\'' +
                    ", dbPsw='" + dbPsw + '\'' +
                    '}';
        }
    }

    /**
     * Read a {@link DBProperty} from the file 'properties.json'.
     * @param id which of the database property in the file to read
     * @return the {@link DBProperty} from the file 'properties.json' that corresponds to the key {@param id}
     */
    public static DBProperty getDBProperties(String id) {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(Utils.class.getResourceAsStream("properties.json")));

        JsonObject json = new JsonParser().parse(inputReader).getAsJsonObject();
        JsonElement jsonElement = json.get(id);

        Gson gson = new Gson();
        DBProperty dbp = gson.fromJson(jsonElement, DBProperty.class);
        print(dbp);
        return dbp;
    }

    public static void storeHistory(List<Status> tweets) {
        String sqlTweet = "INSERT INTO tweet (id, user_id, lang, in_reply_id, retweet_id, datetime, text)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlHashtag = "INSERT INTO hashtag_tweet (tweet_id, hashtag_text)" +
                "VALUES (?, ?)";
        long count = 0;

        DBProperty dbp = getDBProperties("postgres_db");

        try (Connection conn = DriverManager.getConnection(dbp.dbURL, dbp.dbUser, dbp.dbPsw)) {
            try (PreparedStatement tweetStmt = conn.prepareStatement(sqlTweet)) {

                for (Status t: tweets) {
                    count++;
                    // print("storing tweet #" + count);

                    long id = t.getId();
                    long userId = t.getUser().getId();
                    String lang = (t.getLang() == null ? "it" : t.getLang()); // nullable
                    HashtagEntity[] hashtags = t.getHashtagEntities();
                    long inReplyId = t.getInReplyToStatusId(); // nullable
                    Status retweetIdStatus = t.getRetweetedStatus(); // nullable
                    Date dateTime = t.getCreatedAt();
                    String text = t.getText();

                    tweetStmt.setLong(1, id);
                    tweetStmt.setLong(2, userId);
                    tweetStmt.setString(3, lang);
                    if (inReplyId == -1)
                        tweetStmt.setNull(4, Types.BIGINT);
                    else
                        tweetStmt.setLong(4, inReplyId);
                    if (retweetIdStatus == null)
                        tweetStmt.setNull(5, Types.BIGINT);
                    else
                        tweetStmt.setLong(5, retweetIdStatus.getId());
                    tweetStmt.setObject(6, dateTime, Types.TIMESTAMP);
                    tweetStmt.setString(7, text);
                    tweetStmt.executeUpdate();

                    try (PreparedStatement hashtagStmt = conn.prepareStatement(sqlHashtag)) {
                        for (HashtagEntity he : hashtags) {
                            hashtagStmt.setLong(1, id);
                            hashtagStmt.setString(2, he.getText());
                            hashtagStmt.executeUpdate();
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    static void populateDB(Date date) {
        List<Status> tweets;
        List<String> politicians = new ArrayList<>();
        try {
             politicians.addAll(getUsersList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String politician : politicians) {
            tweets = getHistory(politician, date);
            // printHistory(tweets);
            print("storing history of " + politician);
            storeHistory(tweets);
            print("stored");
        }

    }

    private static void convertTweet(String dirName, Connection conn, ResultSet tweet, String userName) throws SQLException {
        long row = tweet.getRow();
        long id = tweet.getLong("id");
//        print(id);
        long userId = tweet.getLong("user_id");
        String lang = tweet.getString("lang");
        long inReplyId = tweet.getLong("in_reply_id");
        long retweetId = tweet.getLong("retweet_id");
        LocalDateTime dateTime = tweet.getTimestamp("datetime").toLocalDateTime();
        String text = tweet.getString("text").replace("\n", " \\ ");
        List<String> hashtagList = new ArrayList<>();

        // get the hastags related to the tweet
        try (Statement hashStmt = conn.createStatement()) {
            try (ResultSet hashtag = hashStmt.executeQuery(
                    "select hashtag_text from tweet, hashtag_tweet where id = " + id + " and id = tweet_id;")) {
                while (hashtag.next()) {
                    hashtagList.add(hashtag.getString(1));
                }
            } catch (SQLException e) {
                print("Hashtag query raised SQLException");
                e.printStackTrace();
            }
        }

        // get the url of the tweet
        String url = "https://twitter.com/" + userName + "/status/" + id;

        // print the tweet
//        print(row + " - " + id + ", " + userId + ", " + userName + ", " + lang + ", " + inReplyId + ", " + retweetId + ", " +
//                dateTime + ", " + text + ", " + url + ", " + hashtagList);
//        print("");

        // create and print the lucene tweet
        LuceneTweet luceneTweet = new LuceneTweet(id, userId, userName, lang, inReplyId, retweetId, dateTime,
                url, hashtagList.toArray(new String[0]), text);
        luceneTweet.writeDoc(dirName);
//        print(luceneTweet);
//        print("");
//        print("");
    }

    public static void dbToLucene(String dirName, String dbURL, String dbUser, String dbPsw) {
        Twitter twitter0 = new TwitterFactory(CONF0).getInstance();

        Map<String, Long> politicians = new HashMap<>();
        try {
            getUsersList().forEach(x -> politicians.put(x, getUserFromName(twitter0, x).getId()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (Connection conn = DriverManager.getConnection(dbURL, dbUser, dbPsw)) {
            try (Statement stmt = conn.createStatement()) {
                // get all the tweet from each user
                for (Map.Entry<String, Long> politician : politicians.entrySet()) {
                    String polName = politician.getKey();
                    Long polID = politician.getValue();
                    print(polName);
                    try (ResultSet tweet = stmt.executeQuery("select * from tweet where user_id = " + polID + ";")) {
                        while (tweet.next()) {
                            if (tweet.getRow() % 2 == 0)
                                convertTweet(dirName, conn, tweet, polName);
                            else
                                convertTweet(dirName, conn, tweet, polName);
                        }
                    } catch (SQLException e) {
                        print("Tweet query raised SQLException");
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                print("Statement raised SQLException");
                e.printStackTrace();
            }
        } catch (SQLException e) {
            print("Connection raised SQLException");
            e.printStackTrace();
        }
    }

    public static void dbToLucene(String dirName, DBProperty dbp) {
        dbToLucene(dirName, dbp.dbURL, dbp.dbUser, dbp.dbPsw);
    }
}