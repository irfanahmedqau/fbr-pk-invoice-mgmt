package com.fbr.invoice.upload.service;

import com.fbr.invoice.upload.entity.FbrInvoiceRequest;
import com.fbr.invoice.upload.entity.FbrItem;
import com.fbr.invoice.upload.repository.FbrInvoiceRequestRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.List;

@Service
public class PdfService {

    @Autowired
    private FbrInvoiceRequestRepository invoiceRepo;

    private static final DecimalFormat AMOUNT_FMT = new DecimalFormat("#,##0.00");

    private static final Color COLOR_HEADER_BG = new Color(240, 240, 240);
    private static final Color COLOR_BORDER    = new Color(180, 180, 180);

    // -----------------------------------------------------------------------

    public byte[] generateInvoicePdf(Long id) {
        FbrInvoiceRequest inv = invoiceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + id));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            doc.add(buildHeaderTable(inv));
            doc.add(Chunk.NEWLINE);
            doc.add(buildPartyTable(inv));
            doc.add(Chunk.NEWLINE);
            doc.add(buildItemsTable(inv));
            doc.add(Chunk.NEWLINE);
            doc.add(buildFooterSection(inv));
            doc.add(Chunk.NEWLINE);
            doc.add(buildBottomNote(inv));

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        } finally {
            doc.close();
        }

        return out.toByteArray();
    }

    // -----------------------------------------------------------------------
    // Section builders
    // -----------------------------------------------------------------------

    private PdfPTable buildHeaderTable(FbrInvoiceRequest inv) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{50, 50});

        // Left: company name + invoice/date meta
        PdfPTable leftInner = new PdfPTable(1);
        leftInner.setWidthPercentage(100);

        PdfPCell companyCell;
        try {
            Image basfaLogo = loadClasspathImage("/static/basfa-logo.png");
            basfaLogo.scaleToFit(160, 50);
            companyCell = new PdfPCell(basfaLogo);
        } catch (Exception e) {
            companyCell = new PdfPCell(new Phrase("Basfa", font(22, Font.BOLDITALIC)));
        }
        companyCell.setBorder(Rectangle.NO_BORDER);
        companyCell.setPaddingBottom(8);
        leftInner.addCell(companyCell);

        PdfPTable metaRow = new PdfPTable(new float[]{30, 40, 15, 30});
        metaRow.setWidthPercentage(100);
        metaRow.addCell(labelCell("Invoice #"));
        metaRow.addCell(valueCell(inv.getInvoiceRefNo()));
        metaRow.addCell(labelCell("Date"));
        metaRow.addCell(valueCell(inv.getInvoiceDate() != null ? inv.getInvoiceDate().toString() : ""));
        PdfPCell metaWrapper = new PdfPCell(metaRow);
        metaWrapper.setBorder(Rectangle.NO_BORDER);
        metaWrapper.setPadding(0);
        leftInner.addCell(metaWrapper);

        PdfPCell leftOuter = new PdfPCell(leftInner);
        leftOuter.setBorder(Rectangle.NO_BORDER);
        leftOuter.setPadding(4);
        table.addCell(leftOuter);

        // Right: SALES TAX INVOICE box
        PdfPCell titleCell = new PdfPCell(new Phrase("SALES TAX INVOICE", font(16, Font.BOLD)));
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setBorderColor(COLOR_BORDER);
        titleCell.setPadding(14);
        titleCell.setMinimumHeight(60);
        table.addCell(titleCell);

        return table;
    }

    private PdfPTable buildPartyTable(FbrInvoiceRequest inv) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{50, 50});
        table.addCell(partyBox("SELLER", inv.getSellerBusinessName(), inv.getSellerAddress(), inv.getSellerNTNCNIC()));
        table.addCell(partyBox("BUYER",  inv.getBuyerBusinessName(),  inv.getBuyerAddress(),  inv.getBuyerNTNCNIC()));
        return table;
    }

    private PdfPCell partyBox(String label, String name, String address, String ntn) {
        PdfPTable inner = new PdfPTable(1);
        inner.setWidthPercentage(100);

        PdfPCell header = new PdfPCell(new Phrase(label, font(11, Font.BOLD)));
        header.setBackgroundColor(COLOR_HEADER_BG);
        header.setBorderColor(COLOR_BORDER);
        header.setPadding(5);
        inner.addCell(header);

        inner.addCell(bodyLine(name, font(9, Font.BOLD)));
        if (address != null && !address.isBlank()) {
            for (String line : address.split(",")) {
                inner.addCell(bodyLine(line.trim(), font(9, Font.NORMAL)));
            }
        }
        inner.addCell(bodyLine("NTN # " + (ntn != null ? ntn : ""), font(9, Font.NORMAL)));

        PdfPCell wrapper = new PdfPCell(inner);
        wrapper.setBorderColor(COLOR_BORDER);
        wrapper.setPadding(0);
        return wrapper;
    }

    private PdfPTable buildItemsTable(FbrInvoiceRequest inv) throws DocumentException {
        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4, 10, 18, 6, 6, 12, 12, 7, 11, 14});

        for (String h : new String[]{"Sr#", "Item Code", "Description", "Unit", "Qty", "Rate", "Value", "GST%", "GST", "Total"}) {
            PdfPCell cell = new PdfPCell(new Phrase(h, font(9, Font.BOLD)));
            cell.setBackgroundColor(COLOR_HEADER_BG);
            cell.setBorderColor(COLOR_BORDER);
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        List<FbrItem> items = inv.getItems() != null ? inv.getItems() : List.of();
        int sr = 1;
        for (FbrItem item : items) {
            double unitRate = item.getQuantity() != 0
                    ? item.getValueSalesExcludingST() / item.getQuantity()
                    : 0;

            table.addCell(centerCell(String.valueOf(sr++)));
            table.addCell(centerCell(item.getHsCode()));
            table.addCell(descCell(item.getProductDescription()));
            table.addCell(centerCell(item.getUoM()));
            table.addCell(centerCell(formatQty(item.getQuantity())));
            table.addCell(rightCell(AMOUNT_FMT.format(unitRate)));
            table.addCell(rightCell(AMOUNT_FMT.format(item.getValueSalesExcludingST())));
            table.addCell(centerCell(item.getRate()));
            table.addCell(rightCell(AMOUNT_FMT.format(item.getSalesTaxApplicable())));
            table.addCell(rightCell(AMOUNT_FMT.format(item.getTotalValues())));
        }

        double subtotal = items.stream().mapToDouble(FbrItem::getValueSalesExcludingST).sum();
        double salesTax = items.stream().mapToDouble(FbrItem::getSalesTaxApplicable).sum();
        double total    = items.stream().mapToDouble(FbrItem::getTotalValues).sum();

        PdfPCell totalLabel = new PdfPCell(new Phrase("Total", font(9, Font.BOLD)));
        totalLabel.setColspan(6);
        totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabel.setBorderColor(COLOR_BORDER);
        totalLabel.setPadding(4);
        table.addCell(totalLabel);
        table.addCell(rightCell(AMOUNT_FMT.format(subtotal)));
        table.addCell(centerCell(""));
        table.addCell(rightCell(AMOUNT_FMT.format(salesTax)));
        table.addCell(rightCell(AMOUNT_FMT.format(total)));

        return table;
    }

    private PdfPTable buildFooterSection(FbrInvoiceRequest inv) throws DocumentException {
        // FBR invoice number + QR code + FBR logo
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell fbrLabel = new PdfPCell(new Phrase("FBR Invoice #", font(9, Font.BOLD)));
        fbrLabel.setBorder(Rectangle.NO_BORDER);
        table.addCell(fbrLabel);

        String fbrRef = inv.getFbrInvRefNumber() != null ? inv.getFbrInvRefNumber() : "";
        PdfPCell fbrNum = new PdfPCell(new Phrase(fbrRef, font(9, Font.BOLD)));
        fbrNum.setBorder(Rectangle.NO_BORDER);
        table.addCell(fbrNum);

        PdfPTable badgeRow = new PdfPTable(new float[]{90, 90});
        badgeRow.setTotalWidth(190);
        badgeRow.setLockedWidth(true);
        badgeRow.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        try {
            Image fbrLogo = loadClasspathImage("/static/fbr-logo.png");
            fbrLogo.scaleToFit(85, 85);
            PdfPCell logoCell = new PdfPCell(fbrLogo);
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setPadding(2);
            badgeRow.addCell(logoCell);
        } catch (Exception ignored) {
            badgeRow.addCell(emptyCell());
        }

        if (!fbrRef.isBlank()) {
            try {
                byte[] qrBytes = generateQrCode(fbrRef, 120);
                Image qrImage = Image.getInstance(qrBytes);
                qrImage.scaleToFit(85, 85);
                PdfPCell qrCell = new PdfPCell(qrImage);
                qrCell.setBorder(Rectangle.NO_BORDER);
                qrCell.setPadding(2);
                badgeRow.addCell(qrCell);
            } catch (Exception ignored) {
                badgeRow.addCell(emptyCell());
            }
        } else {
            badgeRow.addCell(emptyCell());
        }

        PdfPCell badgeWrapper = new PdfPCell(badgeRow);
        badgeWrapper.setBorder(Rectangle.NO_BORDER);
        badgeWrapper.setPadding(0);
        badgeWrapper.setPaddingTop(6);
        table.addCell(badgeWrapper);

        return table;
    }

    private PdfPCell emptyCell() {
        PdfPCell cell = new PdfPCell(new Phrase(""));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private Image loadClasspathImage(String path) throws Exception {
        try (var in = getClass().getResourceAsStream(path)) {
            if (in == null) throw new IllegalStateException("Resource not found: " + path);
            return Image.getInstance(in.readAllBytes());
        }
    }

    private PdfPTable buildBottomNote(FbrInvoiceRequest inv) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        String note = String.format("%s  Dated: %s  is system generated invoice and need no signature.",
                inv.getInvoiceRefNo(),
                inv.getInvoiceDate() != null ? inv.getInvoiceDate().toString() : "");

        PdfPCell noteCell = new PdfPCell(new Phrase(note, font(8, Font.NORMAL, Color.DARK_GRAY)));
        noteCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        noteCell.setBorder(Rectangle.NO_BORDER);
        noteCell.setPaddingBottom(6);
        table.addCell(noteCell);

        String contact = inv.getSellerBusinessName()
                + (inv.getSellerAddress() != null ? "  |  " + inv.getSellerAddress() : "");
        PdfPCell contactCell = new PdfPCell(new Phrase(contact, font(10, Font.BOLD)));
        contactCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        contactCell.setBackgroundColor(COLOR_HEADER_BG);
        contactCell.setBorderColor(COLOR_BORDER);
        contactCell.setPadding(8);
        table.addCell(contactCell);

        return table;
    }

    // -----------------------------------------------------------------------
    // Cell helpers
    // -----------------------------------------------------------------------

    private PdfPCell labelCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font(9, Font.BOLD)));
        cell.setBorderColor(COLOR_BORDER);
        cell.setPadding(3);
        return cell;
    }

    private PdfPCell valueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font(9, Font.NORMAL)));
        cell.setBorderColor(COLOR_BORDER);
        cell.setPadding(3);
        return cell;
    }

    private PdfPCell bodyLine(String text, Font f) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", f));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        return cell;
    }

    private PdfPCell centerCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font(8, Font.NORMAL)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(COLOR_BORDER);
        cell.setPadding(3);
        cell.setNoWrap(true);
        return cell;
    }

    private PdfPCell rightCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font(8, Font.NORMAL)));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorderColor(COLOR_BORDER);
        cell.setPadding(3);
        cell.setNoWrap(true);
        return cell;
    }

    private PdfPCell descCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font(9, Font.NORMAL)));
        cell.setBorderColor(COLOR_BORDER);
        cell.setPadding(4);
        return cell;
    }

    // -----------------------------------------------------------------------
    // Font factory — avoids static field init order issues
    // -----------------------------------------------------------------------

    private static Font font(float size, int style) {
        return new Font(Font.HELVETICA, size, style, Color.BLACK);
    }

    private static Font font(float size, int style, Color color) {
        return new Font(Font.HELVETICA, size, style, color);
    }

    // -----------------------------------------------------------------------
    // QR code
    // -----------------------------------------------------------------------

    private byte[] generateQrCode(String content, int size) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);
        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    // -----------------------------------------------------------------------

    private String formatQty(double qty) {
        return qty == Math.floor(qty) ? String.valueOf((long) qty) : String.valueOf(qty);
    }
}
