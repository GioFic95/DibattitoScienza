package ds.twitter4jWrapper.params;

import ds.Utils;
import twitter4j.Twitter;
import twitter4j.User;

import java.util.ArrayList;
import java.util.List;

import static ds.twitter4jWrapper.Twitter4jUtils.getUserFromId;
import static ds.twitter4jWrapper.Twitter4jUtils.getUserFromName;

/**
 * The array of users to use in a filtered query.
 */
public class Users
{
    private User[] users;

    public Users(User... users)
    {
        this.users = users;
    }

    /**
     * Build the array starting from the users' names.
     * @param twitter    the {@link Twitter} instance to use for building the {@link User}.
     * @param usersNames the names of the users.
     */
    public Users(Twitter twitter, String... usersNames)
    {
        List<User> usersList = new ArrayList<>();
        for (String user : usersNames) {
            usersList.add(getUserFromName(twitter, user));
        }
        this.users = (usersList.toArray(new User[0]));
//        print(Arrays.toString(this.users));
    }

    /**
     * Build the array starting from the users' ids.
     * @param twitter    the {@link Twitter} instance to use for building the {@link User}.
     * @param usersIds the ids of the users.
     */
    public Users(Twitter twitter, long... usersIds)
    {
        List<User> usersList = new ArrayList<>();
        for (long user : usersIds) {
            usersList.add(getUserFromId(twitter, user));
        }
        this.users = usersList.toArray(new User[0]);
    }

    public User[] getUsers()
    {
        return users;
    }

    /**
     * Get the ids of the users in the array.
     * @return the ids of the users.
     */
    public long[] getIds() {
        long[] ids = new long[users.length];
        for (int i=0; i<users.length; i++) {
            ids[i] = users[i].getId();
        }
//        print(Arrays.toString(ids));
        return ids;
    }

    /**
     * Get the names of the users in the array.
     * @return the names of the users.
     */
    public String[] getNames() {
        String[] names = new String[users.length];
        for (int i=0; i<users.length; i++) {
            names[i] = users[i].getScreenName();
        }
        return names;
    }
}
