package io.mosip.print.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import io.mosip.print.util.FontPdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.print.constant.PDFGeneratorExceptionCodeConstant;
import io.mosip.print.exception.PDFGeneratorException;
import io.mosip.print.logger.PrintLogger;
import io.mosip.print.util.EmptyCheckUtils;

/**
 * The PdfGeneratorImpl is the class you will use most when converting processed
 * Template to PDF. It contains a series of methods that accept processed
 * Template as a {@link String}, {@link File}, or {@link InputStream}, and
 * convert it to PDF in the form of an {@link OutputStream}, {@link File}
 * 
 * @author Urvil Joshi
 * @author Uday Kumar
 * @author Neha
 * 
 * @since 1.0.0
 *
 */
@Component
public class PDFGenerator  {
	private static final Logger LOGGER = PrintLogger.getLogger(PDFGenerator.class);
	
	private static final String SHA256 = "SHA256";

	private static final String OUTPUT_FILE_EXTENSION = ".pdf";

	@Value("${mosip.kernel.pdf_owner_password:\"\"}")
	private String pdfOwnerPassword;

	@Value("${mosip.kernel.pdfgenerator.ttf.file.path:classpath:/pdf-generator/*.ttf}")
	private String ttfFilePath;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator#generate(java.io.
	 * InputStream)
	 */
	public OutputStream generate(InputStream is) throws IOException {
		isValidInputStream(is);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			PdfRendererBuilder builder = FontPdfRendererBuilder.getBuilder(ttfFilePath);
			String wellFormedHtml = preprocessHtml(is);
			builder.withHtmlContent(wellFormedHtml, null); // Convert InputStream to String
			builder.toStream(os);
			builder.run();
		} catch (Exception e) {
			throw new io.mosip.kernel.core.pdfgenerator.exception.PDFGeneratorException(PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorCode(),
					e.getMessage(), e);
		}
		return os;
	}
	private String preprocessHtml(InputStream is) throws IOException {
		// Specify UTF-8 encoding explicitly when reading the input stream
		String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);

		// Parse the HTML content with Jsoup
		Document document = Jsoup.parse(html);

		// Preserve non-Latin characters by setting the correct output encoding
		document.outputSettings()
				.syntax(Document.OutputSettings.Syntax.xml)
				.charset(StandardCharsets.UTF_8); // Ensure UTF-8 encoding for output

		return document.html(); // Returns well-formed XHTML with proper encoding
	}

	private void isValidInputStream(InputStream dataInputStream) {
		if (EmptyCheckUtils.isNullEmpty(dataInputStream)) {
			throw new PDFGeneratorException(
					PDFGeneratorExceptionCodeConstant.INPUTSTREAM_NULL_EMPTY_EXCEPTION.getErrorCode(),
					PDFGeneratorExceptionCodeConstant.INPUTSTREAM_NULL_EMPTY_EXCEPTION.getErrorMessage());
		}
	}
}
