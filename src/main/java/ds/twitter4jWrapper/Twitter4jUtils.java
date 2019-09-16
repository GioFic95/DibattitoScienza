package ds.twitter4jWrapper;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ds.Utils;
import ds.twitter4jWrapper.params.*;
import org.jetbrains.annotations.NotNull;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

import static ds.Utils.*;

/**
 * Static methods to simplify the use of Twitter4J API.
 */
public class Twitter4jUtils {

    /**
     * A class to hold access keys for Twitter
     */
    static class TwitterProperty {
        private String oack;
        private String oacs;
        private String oaat;
        private String oats;

        public TwitterProperty(){}

        @Override
        public String toString() {
            return "TwitterProperty{" +
                    "oack='" + oack + '\'' +
                    ", oacs='" + oacs + '\'' +
                    ", oaat='" + oaat + '\'' +
                    ", oats='" + oats + '\'' +
                    '}';
        }
    }

    /**
     * Create the listener to make the query.
     * @return a new configured listener.
     */
    public static StatusListener getListener() {

        StatusListener listener = new StatusListener() {
            public void onStatus(Status status) {
                print("onStatus:");
                print("@" + status.getUser().getScreenName() + ":\t" + status.getText() + "\n");
            }

            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            }

            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                print("onTrackLimitationNotice");
                print(numberOfLimitedStatuses);
            }

            public void onException(Exception ex) {
                ex.printStackTrace();
            }

            public void onScrubGeo(long arg0, long arg1) {
            }

            public void onStallWarning(StallWarning warning) {
                print("onStallWarning");
                print(warning);
            }
        };

        return listener;
    }

    /**
     * Create and setup a {@link ConfigurationBuilder} with the keys passed as parameters.
     * @param oack the OAuth Consumer Key
     * @param oacs the OAuth Consumer Secret
     * @param oaat the OAuth Access Token
     * @param oats the OAuth Access Token Secret
     * @return the {@link ConfigurationBuilder} set up with the parameter keys.
     */
    public static ConfigurationBuilder setAuth(String oack, String oacs, String oaat, String oats) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(false)
                .setOAuthConsumerKey(oack)
                .setOAuthConsumerSecret(oacs)
                .setOAuthAccessToken(oaat)
                .setOAuthAccessTokenSecret(oats);
        return cb;
    }

    /**
     * Create and setup a {@link ConfigurationBuilder} with the keys contained in the parameter.
     * @param tp the {@link TwitterProperty} that contains the keys for this {@link ConfigurationBuilder},
     *           as obtained with {@link #getTwitterProperties}
     * @return the {@link ConfigurationBuilder} set up with the keys in {@param tp}.
     */
    public static ConfigurationBuilder setAuth(TwitterProperty tp) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(false)
                .setOAuthConsumerKey(tp.oack)
                .setOAuthConsumerSecret(tp.oacs)
                .setOAuthAccessToken(tp.oaat)
                .setOAuthAccessTokenSecret(tp.oats);
        return cb;
    }

    /**
     * Read a {@link TwitterProperty} from the file 'properties.json'.
     * @param id which of the twitter property in the file to read
     * @return the {@link TwitterProperty} from the file 'properties.json' that corresponds to the key {@param id}
     */
    public static TwitterProperty getTwitterProperties(String id) {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(Utils.class.getResourceAsStream("properties.json")));

        JsonObject json = new JsonParser().parse(inputReader).getAsJsonObject();
        JsonElement jsonElement = json.get(id);

        Gson gson = new Gson();
        TwitterProperty tp = gson.fromJson(jsonElement, TwitterProperty.class);
        print(tp);
        return tp;
    }

    /**
     * Execute any possible query, but is called only by other methods of this class, not directly by the user.
     * Note that the parameters are put in OR by the Twitter API.
     * @param ts       the {@link TwitterStream} to listen.
     * @param sl       the {@link StatusListener} to use.
     * @param langs    the languages to filter.
     * @param places   the places to filter.
     * @param users    the users to filter.
     * @param kws      the keywords to filter.
     * @param endPoint determines whether to sample or to filter.
     */
    private static void makeCall(TwitterStream ts, StatusListener sl, Languages langs, Locations places, Users users,
                                 Keywords kws, @NotNull Endpoint endPoint) {
        switch (endPoint) {
            case FILTER: {
                ts.addListener(sl);

                FilterQuery fq = new FilterQuery();
                if (langs != null) {
                    fq.language(langs.getLanguages());
                }
                if (users != null) {
                    fq.follow(users.getIds());
                }
                if (places != null) {
                    fq.locations(places.getLocations());
                }
                if (kws != null) {
                    fq.track(kws.getKeywords());
                }
                print(fq);
                ts.filter(fq);
                break;
            }
            case SAMPLE:
                print("sample");
                ts.sample();
                break;
        }
    }

    /**
     * Make a filtered query with all the parameters.
     * @param ts       the {@link TwitterStream} to listen.
     * @param sl       the {@link StatusListener} to use.
     * @param langs    the languages to filter.
     * @param places   the places to filter.
     * @param users    the users to filter.
     * @param kws      the keywords to filter.
     */
    public static void makeCall(TwitterStream ts, StatusListener sl, Languages langs, Locations places, Users users,
                                Keywords kws) {
        makeCall(ts, sl, langs, places, users, kws, Endpoint.FILTER);
    }

    /**
     * Make a filtered query with all the parameters.
     * @param ts       the {@link TwitterStream} to listen.
     * @param sl       the {@link StatusListener} to use.
     * @param langs    the languages to filter.
     * @param places   the places to filter.
     * @param users    the users to filter.
     */
    public static void makeCall(TwitterStream ts, StatusListener sl, Languages langs, Locations places, Users users) {
        makeCall(ts, sl, langs, places, users, null, Endpoint.FILTER);
    }

    /**
     * Make a filtered query with all the parameters.
     * @param ts       the {@link TwitterStream} to listen.
     * @param sl       the {@link StatusListener} to use.
     * @param langs    the languages to filter.
     * @param places   the places to filter.
     * @param kws      the keywords to filter.
     */
    public static void makeCall(TwitterStream ts, StatusListener sl, Languages langs, Locations places, Keywords kws) {
        makeCall(ts, sl, langs, places, null, kws, Endpoint.FILTER);
    }

    /**
     * Make a filtered query with all the parameters.
     * @param ts       the {@link TwitterStream} to listen.
     * @param sl       the {@link StatusListener} to use.
     * @param langs    the languages to filter.
     * @param users    the users to filter.
     * @param kws      the keywords to filter.
     */
    public static void makeCall(TwitterStream ts, StatusListener sl, Languages langs, Users users, Keywords kws) {
        makeCall(ts, sl, langs, null, users, kws, Endpoint.FILTER);
    }

    /**
     * Make a filtered query with all the parameters.
     * @param ts       the {@link TwitterStream} to listen.
     * @param sl       the {@link StatusListener} to use.
     * @param places   the places to filter.
     * @param users    the users to filter.
     * @param kws      the keywords to filter.
     */
    public static void makeCall(TwitterStream ts, StatusListener sl, Locations places, Users users, Keywords kws) {
        makeCall(ts, sl, null, places, users, kws, Endpoint.FILTER);
    }

    /**
     * Execute a filtered query with languages and keywords.
     * @param ts    the {@link TwitterStream} to listen.
     * @param sl    the {@link StatusListener} to use.
     * @param langs the languages to filter.
     * @param kws   the keywords to filter.
     */
    public static void makeCall(TwitterStream ts, StatusListener sl, Languages langs, Keywords kws) {
        makeCall(ts, sl, langs, null, null, kws, Endpoint.FILTER);
    }

    /**
     * Execute a filtered query with languages and users.
     * @param ts       the {@link TwitterStream} to listen.
     * @param sl       the {@link StatusListener} to use.
     * @param langs    the languages to filter.
     * @param users    the users to filter.
     */
    public static void makeCall(TwitterStream ts, StatusListener sl, Languages langs, Users users) {
        makeCall(ts, sl, langs, null, users, null, Endpoint.FILTER);
    }

    /**
     * Execute a filtered query with places and users.
     * @param ts       the {@link TwitterStream} to listen.
     * @param sl       the {@link StatusListener} to use.
     * @param places   the places to filter.
     * @param users    the users to filter.
     */
    public static void makeCall(TwitterStream ts, StatusListener sl, Locations places, Users users) {
        makeCall(ts, sl, null, places, users, null, Endpoint.FILTER);
    }

    /**
     * Execute a filtered query with places and keywords.
     * @param ts       the {@link TwitterStream} to listen.
     * @param sl       the {@link StatusListener} to use.
     * @param places   the places to filter.
     * @param kws      the keywords to filter.
     */
    public static void makeCall(TwitterStream ts, StatusListener sl, Locations places, Keywords kws) {
        makeCall(ts, sl, null, places, null, kws, Endpoint.FILTER);
    }

    /**
     * Execute a filtered query with users and keywords.
     * @param ts       the {@link TwitterStream} to listen.
     * @param sl       the {@link StatusListener} to use.
     * @param users    the users to filter.
     * @param kws      the keywords to filter.
     */
    public static void makeCall(TwitterStream ts, StatusListener sl, Users users, Keywords kws) {
        makeCall(ts, sl, null, null, users, kws, Endpoint.FILTER);
    }

    /**
     * Execute a filtered query with users.
     * @param ts       the {@link TwitterStream} to listen.
     * @param sl       the {@link StatusListener} to use.
     * @param users    the users to filter.
     */
    public static void makeCall(TwitterStream ts, StatusListener sl, Users users) {
        makeCall(ts, sl, null, null, users, null, Endpoint.FILTER);
    }

    /**
     * Execute a filtered query with keywords.
     * @param ts       the {@link TwitterStream} to listen.
     * @param sl       the {@link StatusListener} to use.
     * @param kws      the keywords to filter.
     */
    public static void makeCall(TwitterStream ts, StatusListener sl, Keywords kws) {
        makeCall(ts, sl, null, null, null, kws, Endpoint.FILTER);
    }

    /**
     * Execute a sample.
     * @param ts       the {@link TwitterStream} to listen.
     * @param sl       the {@link StatusListener} to use.
     */
    public static void makeCall(TwitterStream ts, StatusListener sl) {
        makeCall(ts, sl, null, null, null, null, Endpoint.SAMPLE);
    }

    public static void printFriendsInfo(Twitter twitter, String name) {
        long userId = getUserFromName(twitter, name).getId();
        try {
            long [] ids = twitter.getFriendsIDs(userId, -1).getIDs();

            for (long id : ids) {
                User u = twitter.showUser(id);
                print(u.getScreenName());
                print(u.getName());
                print(u.getDescription());
                print(u.getLang());
                print(u.getLocation());
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    public static List<Status> getHistory(String name, Date since) {
        print("\ngetting history of " + name);

        Twitter twitter0 = new TwitterFactory(CONF0).getInstance();
        Twitter twitter1 = new TwitterFactory(CONF1).getInstance();
        long userId = getUserFromName(twitter1, name).getId();
        List<Status> tweets = new ArrayList<>();
        Paging pg = new Paging();
        long lastID = Long.MAX_VALUE;
        boolean stop = false;
        int count = 0;
        List<Status> tempTweets;

        while (! stop) {
            print("#" + count + " - list size: " + tweets.size());
            try {
                if (count % 2 == 0)
                    tempTweets = twitter1.getUserTimeline(userId, pg);
                else
                    tempTweets = twitter0.getUserTimeline(userId, pg);

                print("#" + count + " - temp size: " + tempTweets.size() + "\n");
                if (tempTweets.isEmpty())
                    stop = true;

                for (Status t: tempTweets) {
                    if (t.getId() < lastID) lastID = t.getId();
                    if (t.getCreatedAt().before(since)) {
                        stop = true;
                        break;
                    }
                    tweets.add(t);
                }
            } catch (TwitterException e) {
                e.printStackTrace();
                try {
                    Thread.sleep(1000 * 60 * 5);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }
            }
            pg.setMaxId(lastID - 1);
            count++;
        }
        print("get history done\n");
        return tweets;
    }

    public static void printHistory(List<Status> tweets) {
        long count = 0;
        for (Status t: tweets) {
            count++;
            print("printing tweet #" + count);
            print("id: " + t.getId());
            print("user_id: " + t.getUser().getId());
            print("lang: " + (t.getLang() == null ? "it" : t.getLang())); // nullable
            print("in_reply_id: " + (t.getInReplyToStatusId() == -1 ? "" : t.getInReplyToStatusId())); // nullable
            print("retweet_id: " + (t.getRetweetedStatus() == null ? "" : t.getRetweetedStatus().getId())); // nullable
            print("date_time: " + t.getCreatedAt());
            print("hashtags: " + Arrays.toString(t.getHashtagEntities()));
            print("text: " + t.getText());
            print("");
        }
    }

    public static User getUserFromName(@NotNull Twitter twitter, String screenName) {
        User user = null;
        try {
            user = twitter.showUser(screenName);
        } catch (TwitterException e) {
            e.printStackTrace();
            return user;
        }
        return user;
    }

    public static User getUserFromId(@NotNull Twitter twitter, long userId) {
        User user = null;
        try {
            user = twitter.showUser(userId);
        } catch (TwitterException e) {
            e.printStackTrace();
            return user;
        }
        return user;
    }

}
