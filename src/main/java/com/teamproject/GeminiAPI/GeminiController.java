package com.teamproject.GeminiAPI;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class GeminiController {

    private final GeminiService geminiService;

    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping("/gemini")
    public String callGemini(@RequestParam String prompt){
        return geminiService.generateText(prompt);
    }

    @GetMapping("/gemini/summarize")
    public ResponseEntity<String> summarizeNews(@RequestParam String url) {
        try {
            String articleContent = extractArticleContent(url);

            String prompt = """
                아래는 뉴스 기사 내용이야.
                이 기사의 핵심 내용을 한국어로 세 줄로 요약해줘.

                뉴스 내용:
                %s
                """.formatted(articleContent);

            String summary = geminiService.generateText(prompt);

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("요약 중 오류 발생: " + e.getMessage());
        }
    }

    private String extractArticleContent(String url){
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                            + "(KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36")
                    .timeout(5000)
                    .get();

            Element content = doc.select("#newsct_article").first();

            if(content != null) {
                return content.text();
            } else {
                return "본문을 찾을 수 없습니다. URL이 올바른 뉴스 링크인지 학인하세요.";
            }
        } catch (Exception e) {
            return "기사 내용을 불러오지 못했습니다." + e.getMessage();
        }
    }


}
