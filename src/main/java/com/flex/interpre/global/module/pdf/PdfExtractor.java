package com.flex.interpre.global.module.pdf;


import com.flex.interpre.global.module.pdf.exception.PdfExtractorExceptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Component
public class PdfExtractor {
    public String extract(MultipartFile file){
        try (PDDocument document = PDDocument.load(file.getInputStream())) {

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("PDF 텍스트 추출 완료 ({} bytes)", text.length());

            return text;

        } catch (IOException e) {
            log.info("PDF 파일 텍스트 추출 실패: {}", e);
            throw PdfExtractorExceptions.FILE_EXTRACT_FAILED.toException();
        }
    }
}