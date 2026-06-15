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

        PdfPCell companyCell = new PdfPCell(new Phrase(inv.getSellerBusinessName(), font(22, Font.BOLDITALIC)));
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
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{10, 15, 38, 9, 14, 14});

        for (String h : new String[]{"Quantity", "Item Code", "Description", "U/M", "Price Each", "Amount"}) {
            PdfPCell cell = new PdfPCell(new Phrase(h, font(9, Font.BOLD)));
            cell.setBackgroundColor(COLOR_HEADER_BG);
            cell.setBorderColor(COLOR_BORDER);
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        List<FbrItem> items = inv.getItems();
        if (items != null) {
            for (FbrItem item : items) {
                table.addCell(centerCell(formatQty(item.getQuantity())));
                table.addCell(centerCell(item.getHsCode()));
                table.addCell(descCell(item.getProductDescription()));
                table.addCell(centerCell(item.getUoM()));
                table.addCell(rightCell(item.getRate()));
                table.addCell(rightCell(AMOUNT_FMT.format(item.getTotalValues())));
            }
        }

        return table;
    }

    private PdfPTable buildFooterSection(FbrInvoiceRequest inv) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{40, 60});

        // Left: FBR invoice number + QR code
        PdfPTable fbrLeft = new PdfPTable(1);
        fbrLeft.setWidthPercentage(100);

        PdfPCell fbrLabel = new PdfPCell(new Phrase("FBR Invoice #", font(9, Font.BOLD)));
        fbrLabel.setBorder(Rectangle.NO_BORDER);
        fbrLeft.addCell(fbrLabel);

        String fbrRef = inv.getFbrInvRefNumber() != null ? inv.getFbrInvRefNumber() : "";
        PdfPCell fbrNum = new PdfPCell(new Phrase(fbrRef, font(9, Font.BOLD)));
        fbrNum.setBorder(Rectangle.NO_BORDER);
        fbrLeft.addCell(fbrNum);

        if (!fbrRef.isBlank()) {
            try {
                byte[] qrBytes = generateQrCode(fbrRef, 120);
                Image qrImage = Image.getInstance(qrBytes);
                qrImage.scaleToFit(100, 100);
                PdfPCell qrCell = new PdfPCell(qrImage);
                qrCell.setBorder(Rectangle.NO_BORDER);
                qrCell.setPaddingTop(6);
                fbrLeft.addCell(qrCell);
            } catch (Exception ignored) {}
        }

        PdfPCell leftWrapper = new PdfPCell(fbrLeft);
        leftWrapper.setBorder(Rectangle.NO_BORDER);
        leftWrapper.setPadding(4);
        table.addCell(leftWrapper);

        // Right: totals
        List<FbrItem> items = inv.getItems() != null ? inv.getItems() : List.of();
        double subtotal = items.stream().mapToDouble(FbrItem::getTotalValues).sum();
        double salesTax = items.stream().mapToDouble(FbrItem::getSalesTaxApplicable).sum();
        double total    = subtotal + salesTax;

        PdfPTable totals = new PdfPTable(2);
        totals.setWidthPercentage(100);
        addTotalRow(totals, "Subtotal",  "PKR " + AMOUNT_FMT.format(subtotal), false);
        addTotalRow(totals, "Sales Tax", "PKR " + AMOUNT_FMT.format(salesTax), false);
        addTotalRow(totals, "Total",     "PKR " + AMOUNT_FMT.format(total),    true);

        PdfPCell rightWrapper = new PdfPCell(totals);
        rightWrapper.setBorder(Rectangle.NO_BORDER);
        rightWrapper.setPadding(4);
        table.addCell(rightWrapper);

        return table;
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
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font(9, Font.NORMAL)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(COLOR_BORDER);
        cell.setPadding(4);
        return cell;
    }

    private PdfPCell rightCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font(9, Font.NORMAL)));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorderColor(COLOR_BORDER);
        cell.setPadding(4);
        return cell;
    }

    private PdfPCell descCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font(9, Font.NORMAL)));
        cell.setBorderColor(COLOR_BORDER);
        cell.setPadding(4);
        return cell;
    }

    private void addTotalRow(PdfPTable table, String label, String value, boolean bold) {
        int style = bold ? Font.BOLD : Font.NORMAL;
        PdfPCell l = new PdfPCell(new Phrase(label, font(9, style)));
        l.setBorderColor(COLOR_BORDER);
        l.setPadding(4);
        table.addCell(l);
        PdfPCell v = new PdfPCell(new Phrase(value, font(9, style)));
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);
        v.setBorderColor(COLOR_BORDER);
        v.setPadding(4);
        table.addCell(v);
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
