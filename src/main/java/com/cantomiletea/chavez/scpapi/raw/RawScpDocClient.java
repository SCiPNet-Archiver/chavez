package com.cantomiletea.chavez.scpapi.raw;

import com.cantomiletea.chavez.review.dto.ScpAcsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class RawScpDocClient {

    private final String BASE_URL = "https://scp-wiki.wikidot.com/";

    public Document getDocBySlug(String slug) throws IOException {
        return Jsoup.connect(BASE_URL + slug).get();
    }

    public ScpAcsDto getScpAcsDto(String slug) throws IOException {
        Document doc = getDocBySlug(slug);
        String containmentClass = "none";
        String secondaryClass = "none";
        String disruptionClass = "none";
        String riskClass = "none";

        // Containment class
        Elements els = doc.getElementsByClass("contain-class");
        if (els.first() != null && !els.first().attr("style").contains("display: none")) {
            els = els.first().getElementsByClass("class-text");
            if (els.first() != null) {
                containmentClass = els.first().text();
            }
        }

        // Secondary class
        els = doc.getElementsByClass("second-class");
        log.info("[RawScpDocClient::getScpAcsDto] second-class styles: " + els.first().attr("style"));
        if (els.first() != null && !els.first().attr("style").contains("display: none")) {
            els = els.first().getElementsByClass("class-text");
            if (els.first() != null) {
                secondaryClass = els.first().text();
            }
        }

        // Disruption class
        els = doc.getElementsByClass("disrupt-class");
        if (els.first() != null && !els.first().attr("style").contains("display: none")) {
            els = els.first().getElementsByClass("class-text");
            if (els.first() != null) {
                disruptionClass = els.first().text();
            }
        }

        // Risk class
        els = doc.getElementsByClass("risk-class");
        if (els.first() != null && !els.first().attr("style").contains("display: none")) {
            els = els.first().getElementsByClass("class-text");
            if (els.first() != null) {
                riskClass = els.first().text();
            }
        }

        return new ScpAcsDto(
                containmentClass,
                riskClass,
                disruptionClass,
                secondaryClass
        );
    }

}
