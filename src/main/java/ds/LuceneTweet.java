package ds;

import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDateTime;

public class LuceneTweet {
    private Document myDoc;

    /**
     * Create a new ds.LuceneTweet document, with all the fields (possibly empty)
     * @param tweetId       the ID of the tweet
     * @param userId        the ID of the user who wrote the tweet
     * @param userName      the screen name of the author of the tweet
     * @param lang          the language of the tweet
     * @param inReplyId     the ID of the tweet of which this tweet is a reply
     * @param retweetId     the ID of the tweet of which this tweet is a retweet
     * @param dateTime      the timestamp of this tweet
     * @param url           the URL of the tweet
     * @param tags          the hashtags of the tweet
     * @param text          the text of the tweet
     */
    public LuceneTweet(Long tweetId, Long userId, String userName, String lang, Long inReplyId, Long retweetId, LocalDateTime dateTime,
                       String url, String[] tags, String text) {
        long date = 0;
        try {
            DateTools.stringToTime(dateTime.toLocalDate().toString());
        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        myDoc = new Document();
        myDoc.add(new StringField("TweetId", String.valueOf(tweetId), Field.Store.YES));
        myDoc.add(new StringField("UserId", String.valueOf(userId), Field.Store.NO));
        myDoc.add(new StringField("UserName", String.valueOf(userName), Field.Store.YES));
        myDoc.add(new StringField("Lang", lang, Field.Store.NO));
        myDoc.add(new StringField("InReplyId", String.valueOf(inReplyId), Field.Store.NO));
        myDoc.add(new StringField("RetweetId", String.valueOf(retweetId), Field.Store.NO));
        myDoc.add(new LongPoint("Date", date));
        myDoc.add(new StringField("Url", url, Field.Store.YES));
        myDoc.add(new TextField("Tags", Utils.getTags(tags), Field.Store.NO));
        myDoc.add(new TextField("Text", text, Field.Store.NO));
    }

    LuceneTweet(Document doc) {
        myDoc = doc;
    }

    public Document getDoc() {
        return myDoc;
    }

    public void writeDoc(String dirName) {
        Path path = Paths.get(dirName);

        try {
            Directory dir = new SimpleFSDirectory(path);

            IndexWriterConfig cfg = new IndexWriterConfig();
            IndexWriter writer = new IndexWriter(dir, cfg);

            writer.addDocument(myDoc);
            writer.commit();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        String tweet = "";
//        String text = myDoc.get("Text").replace("\n", " \\ ");

        tweet += "TweetId: " + myDoc.get("TweetId") + "\n";
//        tweet += "UserId: " + myDoc.get("UserId") + "\n";
        tweet += "UserName: " + myDoc.get("UserName") + "\n";
//        tweet += "Lang: " + myDoc.get("Lang") + "\n";
//        tweet += "InReplyId: " + myDoc.get("InReplyId") + "\n";
//        tweet += "RetweetId: " + myDoc.get("RetweetId") + "\n";
//        tweet += "Date: " + myDoc.get("Date") + "\n";
        tweet += "Url: " + myDoc.get("Url") + "\n";
//        tweet += "Tags: " + myDoc.get("Tags") + "\n";
//        tweet += "Text: " + text;

        return tweet;
    }
}
