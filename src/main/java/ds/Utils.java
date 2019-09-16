package ds;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.jetbrains.annotations.NotNull;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ds.twitter4jWrapper.Twitter4jUtils.*;


public class Utils {

    public final static Configuration CONF0 = setAuth(getTwitterProperties("conf0")).build();
    public final static Configuration CONF1 = setAuth(getTwitterProperties("conf1")).build();

    public static void print(Object x) {
        System.out.println(x);
    }

    public static void printList(Iterable iter) {
        for (Object o : iter) {
            System.out.println(o);
            System.out.println();
        }
    }

    public static List<String> getUsersList()  throws IOException {
        Path usersPath = new File(Utils.class.getResource("users.txt").getFile()).toPath();
        return Files.lines(usersPath).collect(Collectors.toList());
    }

    public static List<Long> getIdsList()  throws IOException {
        Twitter twitter0 = new TwitterFactory(CONF0).getInstance();

        List<Long> politicians = new ArrayList<>();
        try {
            getUsersList().forEach(x -> politicians.add(getUserFromName(twitter0, x).getId()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return politicians;
    }

    public static List<String> getTopicsList()  throws IOException {
        Path topicsPath = new File(Utils.class.getResource("topics.txt").getFile()).toPath();
        return Files.lines(topicsPath).collect(Collectors.toList());
    }

    static String getTags(String[] tags) {
        StringBuilder sb = new StringBuilder();
        for (String t : tags) {
            String newTag = t + " ";
            sb.append(newTag);
        }
        return sb.toString();
    }

}
