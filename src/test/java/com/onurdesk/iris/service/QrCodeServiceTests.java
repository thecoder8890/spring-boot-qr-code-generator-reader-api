package com.onurdesk.iris.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.onurdesk.iris.dto.QrCodeGenerationRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import jakarta.servlet.ServletOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QrCodeServiceTests {

    @InjectMocks
    private QrCodeService qrCodeService;

    private QrCodeGenerationRequestDto sampleDto;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        sampleDto = QrCodeGenerationRequestDto.builder()
                .title("Test QR")
                .message("This is a test payload") // Assuming 'payload' maps to 'message' in DTO based on schema
                .generatedByName("JUnit") // Assuming 'generatedBy' maps to 'generatedByName'
                .generatedForName("Test Target") // Adding a value for this field
                .build();
    }

    @Test
    void testGenerateQrCode_success() throws IOException, WriterException {
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        qrCodeService.generate(sampleDto, mockResponse);

        assertEquals("attachment;filename=Test_QR.png", mockResponse.getHeader(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals("image/png", mockResponse.getContentType()); // MatrixToImageWriter sets this implicitly

        // Verify the output stream contains PNG data (basic check for non-empty)
        assertTrue(mockResponse.getContentAsByteArray().length > 0);

        // Further verification could involve trying to read the byte array as an image
        // and potentially decoding it, but that might be too much for a unit test.
        // For now, we trust that if MatrixToImageWriter.writeToStream ran without error
        // and produced bytes, it's likely correct.

        // We can also try to verify the content of the QR code if we mock the writer part.
        // Let's try to capture the string passed to the QRCodeWriter.encode
        // This requires QRCodeWriter to be a mock or using a static mock for MatrixToImageWriter
        // For simplicity, the current check on headers and non-empty output is a good start.
    }

    @Test
    void testGenerateQrCode_nullDto() {
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        assertThrows(NullPointerException.class, () -> {
            // The ObjectMapper().writeValueAsString(null) will throw NullPointerException
            qrCodeService.generate(null, mockResponse);
        });
    }

    @Test
    void testGenerateQrCode_emptyTitleInDto() throws IOException, WriterException {
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        QrCodeGenerationRequestDto dtoWithEmptyTitle = QrCodeGenerationRequestDto.builder()
                .title("") // Empty title
                .message("Some payload")
                .generatedByName("JUnit")
                .generatedForName("Test Target")
                .build();

        qrCodeService.generate(dtoWithEmptyTitle, mockResponse);

        // Expecting "attachment;filename=.png" or similar, depending on implementation logic for empty title
        assertEquals("attachment;filename=.png", mockResponse.getHeader(HttpHeaders.CONTENT_DISPOSITION));
        assertTrue(mockResponse.getContentAsByteArray().length > 0);
    }


    @Test
    void testReadQrCode_success() throws Exception {
        // 1. Prepare a valid QR code image as byte array
        String originalContent = objectMapper.writeValueAsString(sampleDto);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(originalContent, BarcodeFormat.QR_CODE, 200, 200);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] qrCodeBytes = pngOutputStream.toByteArray();

        // 2. Mock MultipartFile
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(qrCodeBytes));

        // 3. Call read method
        ResponseEntity<?> responseEntity = qrCodeService.read(multipartFile);

        // 4. Assertions
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof QrCodeGenerationRequestDto);
        QrCodeGenerationRequestDto resultDto = (QrCodeGenerationRequestDto) responseEntity.getBody();
        assertEquals(sampleDto.getTitle(), resultDto.getTitle());
        assertEquals(sampleDto.getMessage(), resultDto.getMessage()); // Changed from getPayload to getMessage
        assertEquals(sampleDto.getGeneratedByName(), resultDto.getGeneratedByName()); // Changed from getGeneratedBy
    }

    @Test
    void testReadQrCode_invalidImageFormat() throws IOException {
        MultipartFile multipartFile = mock(MultipartFile.class);
        // Simulate a file that is not a valid image (e.g., random bytes)
        byte[] invalidImageBytes = "This is not an image".getBytes();
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(invalidImageBytes));

        // ImageIO.read is expected to return null for non-image formats it doesn't understand
        // which will then cause NullPointerException in BufferedImageLuminanceSource constructor
        assertThrows(NullPointerException.class, () -> {
            qrCodeService.read(multipartFile);
        }, "Should throw NullPointerException when ImageIO.read returns null for invalid image format that is not decodable by ImageIO");
    }

    @Test
    void testReadQrCode_notAQrCode() throws IOException {
        // Create a valid PNG image, but not a QR code (e.g., a blank image)
        BufferedImage blankImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(blankImage, "png", baos);
        byte[] blankImageBytes = baos.toByteArray();

        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(blankImageBytes));

        // Expect NotFoundException because MultiFormatReader won't find a QR code
        assertThrows(com.google.zxing.NotFoundException.class, () -> {
            qrCodeService.read(multipartFile);
        });
    }

    @Test
    void testReadQrCode_ioExceptionOnInputStream() throws IOException {
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getInputStream()).thenThrow(new IOException("Failed to read input stream"));

        assertThrows(IOException.class, () -> {
            qrCodeService.read(multipartFile);
        });
    }

    @Test
    void testReadQrCode_unexpectedContent() throws Exception {
        // 1. Prepare a QR code with content that is not a valid JSON for QrCodeGenerationRequestDto
        String nonJsonContent = "Just some plain text, not JSON";
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(nonJsonContent, BarcodeFormat.QR_CODE, 200, 200);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] qrCodeBytes = pngOutputStream.toByteArray();

        // 2. Mock MultipartFile
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(qrCodeBytes));

        // 3. Call read method and expect a Jackson mapping/parsing exception
        // The service tries to map result.getText() to QrCodeGenerationRequestDto.class
        // This will fail if the text is not a JSON representation of that DTO.
        assertThrows(com.fasterxml.jackson.core.JsonProcessingException.class, () -> { // Changed to broader JsonProcessingException
            qrCodeService.read(multipartFile);
        });
    }
}
