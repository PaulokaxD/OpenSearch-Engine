package os.exercise.models;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;
import java.util.Objects;

/**
 * Article Domain Entity.
 */

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Article {
    private String id;
    private String articleTitle;
    private String abstractText;
    private List<String> language;
    private String pubDate;
    private List<String> filterSources;
    private List<String> keywords;
    private List<String> pubTypes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public void setArticleTitle(String title) {
        this.articleTitle = title;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public List<String> getLanguage() {
        return language;
    }

    public void setLanguage(List<String> language) {
        this.language = language;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public List<String> getFilterSources() {
        return filterSources;
    }

    public void setFilterSources(List<String> filterResources) {
        this.filterSources = filterResources;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getPubTypes() {
        return pubTypes;
    }

    public void setPubTypes(List<String> pubTypes) {
        this.pubTypes = pubTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Article article = (Article) o;
        return Objects.equals(id, article.id) &&
                Objects.equals(articleTitle, article.articleTitle) &&
                Objects.equals(abstractText, article.abstractText) &&
                Objects.equals(language, article.language) &&
                Objects.equals(pubDate, article.pubDate) &&
                Objects.equals(filterSources, article.filterSources) &&
                Objects.equals(keywords, article.keywords) &&
                Objects.equals(pubTypes, article.pubTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, articleTitle, abstractText, language, pubDate, filterSources, keywords, pubTypes);
    }
}
