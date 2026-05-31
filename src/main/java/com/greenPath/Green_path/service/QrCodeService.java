package com.greenPath.Green_path.service;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

@Service
public class QrCodeService {

	private static final int SIZE = 280;

	public byte[] pngForText(String text) {
		try {
			Map<EncodeHintType, Object> hints = new HashMap<>();
			hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
			hints.put(EncodeHintType.MARGIN, 1);
			QRCodeWriter writer = new QRCodeWriter();
			BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, SIZE, SIZE, hints);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			MatrixToImageWriter.writeToStream(matrix, "PNG", out);
			return out.toByteArray();
		}
		catch (Exception e) {
			throw new IllegalStateException("QR generation failed", e);
		}
	}
}
