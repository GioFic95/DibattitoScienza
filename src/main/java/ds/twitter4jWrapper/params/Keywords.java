package ds.twitter4jWrapper.params;

import java.util.List;

/**
 * The array of keywords to use in a filtered query.
 */
public class Keywords {
    private String[] keywords;

    public Keywords(String...keywords)
    {
        this.keywords = keywords;
    }
    
    public Keywords(List<String> keywords) {
        this.keywords = keywords.toArray(new String[0]);
    }

    public String[] getKeywords()
    {
        return keywords;
    }
}
