package ds.twitter4jWrapper.params;

import java.util.List;

/**
 * The array of languages to use in a filtered query.
 */
public class Languages {
    private String[] languages;

    public Languages(String...languages)
    {
        this.languages = languages;
    }

    public Languages(List<String> languages) {
        this.languages = languages.toArray(new String[0]);
    }

    public String[] getLanguages()
    {
        return languages;
    }
}
