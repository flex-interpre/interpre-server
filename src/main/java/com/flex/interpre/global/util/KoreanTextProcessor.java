package com.flex.interpre.global.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.analysis.ko.KoreanTokenizer;
import org.apache.lucene.analysis.ko.POS;
import org.apache.lucene.analysis.ko.tokenattributes.PartOfSpeechAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.util.*;

@Component
@Slf4j
public class KoreanTextProcessor {

    private final KoreanAnalyzer analyzer;

    public KoreanTextProcessor() {
        this.analyzer = new KoreanAnalyzer(
                null,  // userDict - 사용자 사전 없음
                KoreanTokenizer.DecompoundMode.NONE,  // 복합명사 분해하지 않음
                Collections.emptySet(),  // stopTags - 빈 Set으로 모든 품사 유지
                false  // outputUnknownUnigrams
        );
    }

    public String correctSpacing(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        try {
            text = text.replaceAll(" {2,}", " ").trim();
            return postProcess(reconstructText(analyzeWithNori(text)));

        } catch (Exception e) {
            log.warn("Nori 처리 실패, 기본 정리로 폴백: {}", e.getMessage(), e);
            return text.replaceAll(" {2,}", " ").trim();
        }
    }

    private List<Token> analyzeWithNori(String text) throws Exception {
        List<Token> tokens = new ArrayList<>();

        try (TokenStream tokenStream = analyzer.tokenStream("", new StringReader(text))) {
            CharTermAttribute termAttr = tokenStream.addAttribute(CharTermAttribute.class);
            PartOfSpeechAttribute posAttr = tokenStream.addAttribute(PartOfSpeechAttribute.class);

            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                String term = termAttr.toString();
                POS.Tag pos = posAttr.getLeftPOS(); // 품사 태그

                tokens.add(new Token(term, pos));
            }
            tokenStream.end();
        }

        return tokens;
    }

    private String reconstructText(List<Token> tokens) {
        if (tokens.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        Token prevToken = null;

        for (Token token : tokens) {
            if (prevToken == null) {
                result.append(token.term);
            } else {
                // 띄어쓰기 규칙 적용
                if (shouldAddSpace(prevToken, token)) {
                    result.append(" ");
                }
                result.append(token.term);
            }
            prevToken = token;
        }

        return result.toString();
    }

    private boolean shouldAddSpace(Token prev, Token current) {
        String currPosStr = current.pos.name();
        String prevPosStr = prev.pos.name();

        if (currPosStr.startsWith("J")) {
            return false;
        }

        if (currPosStr.startsWith("E")) {
            return false;
        }

        if (currPosStr.startsWith("XS")) {
            return false;
        }

        if (currPosStr.startsWith("VX") || currPosStr.equals("VCP") || currPosStr.equals("VCN")) {
            return false;
        }

        if (currPosStr.startsWith("S") && (currPosStr.equals("SF") || currPosStr.equals("SP") ||
            currPosStr.equals("SS") || currPosStr.equals("SE") || currPosStr.equals("SO"))) {
            return false;
        }

        if (prevPosStr.equals("SN") && (currPosStr.equals("NNB") || isUnitNoun(current.term))) {
            return false;
        }

        if (currPosStr.equals("NNB")) {
            return false;
        }

        if ((prevPosStr.startsWith("NN") || prevPosStr.equals("NR")) &&
            (currPosStr.startsWith("NN") || currPosStr.equals("NR"))) {
            return true;
        }

        return true;
    }

    private boolean isUnitNoun(String term) {
        return term.matches("[개명분초시일월년원]") ||
               term.equals("달러") || term.equals("센트") ||
               term.equals("킬로그램") || term.equals("미터") || term.equals("리터") ||
               term.equals("kg") || term.equals("m") || term.equals("l");
    }

    private String postProcess(String text) {
        text = text.replaceAll(" {2,}", " ");

        text = text.replaceAll("\\s+([,.!?;:])", "$1");

        text = text.replaceAll("(\\d+)\\s+([개월년일시분초명])", "$1$2");

        return text.trim();
    }

    private static class Token {
        String term;
        POS.Tag pos;

        Token(String term, POS.Tag pos) {
            this.term = term;
            this.pos = pos;
        }
    }
}
