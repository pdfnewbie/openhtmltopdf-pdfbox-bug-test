package com.openhtmltopdf.pdfboxout.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Html2PdfTest {

    @Test
    public void testAccessibility() throws IOException {
        test(true);
    }

    @Test
    public void testRegular() throws IOException {
        test(false);
    }

    private void test(boolean withAccessiblity) throws IOException {
        File outDir = new File("temp/pdf");
        if (!outDir.exists()) {
            if (outDir.mkdirs()) {
                System.out.println("successfully created " + outDir.getAbsolutePath());
            } else  {
                System.err.println("Failed to create " + outDir.getAbsolutePath());
            }
        }
        String outFileName = "temp/pdf/convert_" + (withAccessiblity ? "Accessibility" : "Regular") + "_" + currentTs() + ".pdf";
        File outFile = new File(outFileName);
        Path filePath = Paths.get("src/test/java/com/openhtmltopdf/pdfboxout/test/html2pdfTest.html");
        byte[] bytes = Files.readAllBytes(filePath);
        String htmlString = new String (bytes);

        final ByteArrayOutputStream bos = generatePdfOpenHtml(htmlString, withAccessiblity);
        final byte[] result = bos.toByteArray();
        
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(result);
        }
        System.out.println("PDF file is generated:" + outFile.getAbsolutePath());
    }

    private static ByteArrayOutputStream generatePdfOpenHtml(String htmlString, boolean withAccessiblity) {
        org.jsoup.nodes.Document document = Jsoup.parse(htmlString, "UTF-8");
        document.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        PdfRendererBuilder builder = new PdfRendererBuilder();
        System.out.println("withAccessiblity=" + withAccessiblity);
        if (withAccessiblity) {
            builder.usePdfUaAccessbility(true);
            builder.usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_1_A);
        }
        File fontsDir = new File("src/test/java/com/openhtmltopdf/pdfboxout/test/fonts");
        File arial = new File(fontsDir, "arial.ttf");
        builder.useFont(arial, "arial");

        File arial2 = new File(fontsDir, "arial.ttf");
        builder.useFont(arial2, "Arial");


        File ArialNarrow = new File(fontsDir, "Arial Narrow.ttf");
        builder.useFont(ArialNarrow, "Arial Narrow");

        File sansserif = new File(fontsDir, "OpenSans-Regular.ttf");
        builder.useFont(sansserif, "sans-serif");
        
        File courier = new File(fontsDir, "09809_COURIER.ttf");
        builder.useFont(courier, "Courier");

        //builder.withUri(outputPdf);
        builder.toStream(os);
        builder.withW3cDocument(new W3CDom().fromJsoup(document), "/");
        try {
            builder.run();
        } catch (IOException e) {
            System.out.println("Error generating pdf");
            throw new RuntimeException("Error generating pdf");
        }
        return os;
    }

    private static String currentTs() {
        DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmSS");
        LocalDateTime now = LocalDateTime.now();
        String s = now.format(DTF);
        return s;
    }
 }
