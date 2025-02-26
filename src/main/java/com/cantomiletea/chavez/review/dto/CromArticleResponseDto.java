package com.cantomiletea.chavez.review.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class CromArticleResponseDto {

    @JsonProperty("data")
    private Data data;

    @Getter
    public static class Data {
            @JsonProperty("page")
            private Page page;

    }

    @Getter
    public static class Page {
        @JsonProperty("alternateTitles")
        private List<AlternateTitle> alternateTitles;

        @JsonProperty("wikidotInfo")
        private WikidotInfo wikidotInfo;

    }

    @Getter
    public static class AlternateTitle {
        @JsonProperty("type")
        private String type;

        @JsonProperty("title")
        private String title;

    }

    @Getter
    public static class WikidotInfo {
        @JsonProperty("title")
        private String title;

        @JsonProperty("createdBy")
        private CreatedBy createdBy;

        @JsonProperty("tags")
        private List<String> tags;

    }

    @Getter
    public static class CreatedBy {
        @JsonProperty("name")
        private String name;

    }

}
