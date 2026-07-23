package com.david.matchcv.service;

import java.io.IOException;

import com.david.matchcv.exception.InvalidPdfException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

/**
 * Extrai texto de um PDF a partir de bytes em memória.
 * Nunca grava em disco — garante conformidade com a decisão de LGPD do projeto.
 */
@Component
public class PdfTextExtractor {

    public String extrairTexto(byte[] bytes) {
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            return new PDFTextStripper().getText(doc);
        } catch (IOException ex) {
            throw new InvalidPdfException("Arquivo PDF inválido ou corrompido.", ex);
        }
    }
}
