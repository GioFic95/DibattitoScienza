package ds;

import java.time.LocalDateTime;
import java.util.*;

import static ds.LeuceneTweetSearch.*;
import static ds.Utils.*;

public class Main {
    private long[] polititiansIds = new long[] {753342422, 13294452, 289400495, 930822249707376640L, 13514762, 1001344387102728194L, 147543162, 1024976264, 1115192245861404672L, 28528873, 215699843, 821945424, 706107345571594240L, 2199795114L, 2313913262L, 1103307543978364930L, 371150146, 194619416, 1026762111987449856L, 4747308149L, 2174124768L, 4256851395L, 47076831, 3064926892L, 2228430626L, 2726636070L};

    private static void init(String dirPath) {
        LuceneTweet lt1 = new LuceneTweet(1234L, 753342422L, "GioFic95", "it", null, null,
                LocalDateTime.parse("2018-12-03T10:15:30"), "www.boh1.com", new String[] {"scioglilingua", "Proverbio", "uno"},
                "Sopra la panca la capra campa.\nTrentatré trentini entrarono a trento tutti e trentatré trotterellando.");
        lt1.writeDoc(dirPath);

        LuceneTweet lt2 = new LuceneTweet(5678L, 958300384279351297L, "Gab42435823", "it", null,
                753342422L, LocalDateTime.parse("2018-12-07T11:15:30"), "www.boh2.com", new String[] {"scioglilingua", "Proverbio", "due"},
                "Chi rompe paga, e i cocci sono suoi.");
        lt2.writeDoc(dirPath);

        LuceneTweet lt3 = new LuceneTweet(9641L, 753342422L, "GioFic95", "it", null, null,
                LocalDateTime.parse("2018-11-05T09:15:30"),"www.boh3.com", new String[] {"m5s"}, "Ho comprato una nuova panca.");
        lt3.writeDoc(dirPath);

        LuceneTweet lt4 = new LuceneTweet(9641L, 753342422L, "GioFic95", "it", 753342422L,
                753342422L, LocalDateTime.parse("2018-12-03T09:15:30"), "www.boh3.com", new String[] {},"PIRIPIRIPIRI.");
        lt4.writeDoc(dirPath);
    }

    private static void testSearch(String dirPath) {
        print("Search by keyword");
        List<LuceneTweet> keywordTweets = searchByKeyword(dirPath, "ambiente");
        print("");
        printList(keywordTweets);

        print("\nSearch by hashtag");
        List<LuceneTweet> hashtagTweets = searchByHashtag(dirPath, "m5s");
        print("");
        printList(hashtagTweets);

        print("\nSearch by author ID");
        List<LuceneTweet> authorTweets = searchByAuthor(dirPath, "270839361");
        print("");
        printList(authorTweets);

        print("\nSearch by author name");
        List<LuceneTweet> authorTweets2 = searchByAuthor(dirPath, "GioFic95");
        print("");
        printList(authorTweets2);

        print("\nSearch by date");
        authorTweets = searchByDate(dirPath, "2018-03-02");
        print("");
        printList(authorTweets);

        print("Search by keyword");
        List<LuceneTweet> keywordTweetsOr = searchByKeyword(dirPath, "gparagone OR Fornaro");
        print("");
        printList(keywordTweetsOr);
    }

    public static void main(String... args) {
        String dirPath = "D:\\Downloads\\DibattitoScienzaPro";  // directory where the Lucene index is located

//        init(dirPath);

//        testSearch(dirPath);

//        Date date = new GregorianCalendar(2019, Calendar.JANUARY, 1).getTime();
//        TweetDB.populateDB(date);

//        TweetDB.dbToLucene(dirPath, TweetDB.getDBProperties("postgres_db"));

        findScience(dirPath, TweetDB.getDBProperties("postgres_db"));
    }
}
