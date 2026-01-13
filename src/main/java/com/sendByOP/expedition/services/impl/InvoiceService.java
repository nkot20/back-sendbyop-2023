package com.sendByOP.expedition.services.impl;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.sendByOP.expedition.models.entities.Booking;
import com.sendByOP.expedition.models.entities.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Service de génération de factures PDF
 * SIMULATION - À remplacer par une vraie génération PDF (iText, Apache PDFBox, etc.)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceService {
    
    /**
     * Génère une facture PDF pour une transaction
     * 
     * @param transaction Transaction pour laquelle générer la facture
     * @return Bytes du PDF généré
     */
    public byte[] generateInvoice(Transaction transaction) {
        log.info("Génération de la facture pour la transaction: {}", 
                transaction.getTransactionReference());
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Couleurs SendByOp
            DeviceRgb primaryColor = new DeviceRgb(255, 107, 53); // #FF6B35
            DeviceRgb secondaryColor = new DeviceRgb(249, 168, 38); // #F9A826
            
            // En-tête avec logo et titre
            Paragraph header = new Paragraph("FACTURE SENDBYOP")
                    .setFontSize(24)
                    .setBold()
                    .setFontColor(primaryColor)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(header);
            
            // Ligne de séparation
            document.add(new Paragraph("_".repeat(100))
                    .setFontColor(ColorConstants.LIGHT_GRAY)
                    .setMarginBottom(20));
            
            // Informations de la facture
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            Table infoTable = new Table(2);
            infoTable.setWidth(UnitValue.createPercentValue(100));
            
            addInfoRow(infoTable, "Référence de transaction:", transaction.getTransactionReference());
            addInfoRow(infoTable, "Date de paiement:", 
                    transaction.getCompletedAt() != null ? 
                    transaction.getCompletedAt().format(formatter) : "En attente");
            addInfoRow(infoTable, "Statut:", transaction.getStatus().getDisplayName());
            
            document.add(infoTable);
            document.add(new Paragraph("\n"));
            
            // Informations client
            Paragraph clientTitle = new Paragraph("INFORMATIONS CLIENT")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(secondaryColor)
                    .setMarginTop(10);
            document.add(clientTitle);
            
            Table clientTable = new Table(2);
            clientTable.setWidth(UnitValue.createPercentValue(100));
            
            addInfoRow(clientTable, "Nom:", 
                    transaction.getCustomer().getFirstName() + " " + 
                    transaction.getCustomer().getLastName());
            addInfoRow(clientTable, "Email:", transaction.getCustomer().getEmail());
            if (transaction.getPhoneNumber() != null) {
                addInfoRow(clientTable, "Téléphone:", transaction.getPhoneNumber());
            }
            
            document.add(clientTable);
            document.add(new Paragraph("\n"));
            
            // Détails de la réservation
            Paragraph bookingTitle = new Paragraph("DÉTAILS DE LA RÉSERVATION")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(secondaryColor)
                    .setMarginTop(10);
            document.add(bookingTitle);
            
            Table bookingTable = new Table(2);
            bookingTable.setWidth(UnitValue.createPercentValue(100));
            
            addInfoRow(bookingTable, "Numéro de réservation:", "#" + transaction.getBooking().getId());
            
            String departure = transaction.getBooking().getFlight().getDepartureAirport() != null && 
                    transaction.getBooking().getFlight().getDepartureAirport().getCity() != null ? 
                    transaction.getBooking().getFlight().getDepartureAirport().getCity().getName() : "N/A";
            String arrival = transaction.getBooking().getFlight().getArrivalAirport() != null &&
                    transaction.getBooking().getFlight().getArrivalAirport().getCity() != null ?
                    transaction.getBooking().getFlight().getArrivalAirport().getCity().getName() : "N/A";
            addInfoRow(bookingTable, "Itinéraire:", departure + " → " + arrival);
            
            if (transaction.getBooking().getFlight().getDepartureDate() != null) {
                addInfoRow(bookingTable, "Date du vol:", 
                        transaction.getBooking().getFlight().getDepartureDate().toString());
            }
            
            addInfoRow(bookingTable, "Poids du colis:", calculateTotalWeight(transaction.getBooking()));
            
            document.add(bookingTable);
            document.add(new Paragraph("\n"));
            
            // Détails du paiement - Tableau avec fond coloré
            Paragraph paymentTitle = new Paragraph("DÉTAILS DU PAIEMENT")
                    .setFontSize(14)
                    .setBold()
                    .setFontColor(secondaryColor)
                    .setMarginTop(10);
            document.add(paymentTitle);
            
            Table paymentTable = new Table(2);
            paymentTable.setWidth(UnitValue.createPercentValue(100));
            
            addInfoRow(paymentTable, "Montant:", transaction.getAmount() + " FCFA");
            addInfoRow(paymentTable, "Méthode de paiement:", 
                    transaction.getPaymentMethod().getDisplayName());
            
            document.add(paymentTable);
            
            // Total avec fond coloré
            Table totalTable = new Table(2);
            totalTable.setWidth(UnitValue.createPercentValue(100));
            totalTable.setMarginTop(20);
            
            Cell totalLabelCell = new Cell()
                    .add(new Paragraph("TOTAL PAYÉ").setBold().setFontSize(16))
                    .setBackgroundColor(new DeviceRgb(240, 240, 240))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setPadding(10);
            
            Cell totalValueCell = new Cell()
                    .add(new Paragraph(transaction.getAmount() + " FCFA")
                            .setBold()
                            .setFontSize(16)
                            .setFontColor(primaryColor))
                    .setBackgroundColor(new DeviceRgb(240, 240, 240))
                    .setTextAlignment(TextAlignment.LEFT)
                    .setPadding(10);
            
            totalTable.addCell(totalLabelCell);
            totalTable.addCell(totalValueCell);
            
            document.add(totalTable);
            
            // Pied de page
            document.add(new Paragraph("\n\n"));
            document.add(new Paragraph("Merci d'avoir choisi SendByOp pour vos envois !")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12)
                    .setItalic());
            
            document.add(new Paragraph("Pour toute question, contactez-nous à support@sendbyop.com")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY));
            
            document.add(new Paragraph("© 2026 SendByOp - Tous droits réservés")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginTop(20));
            
            document.close();
            
            log.info("Facture PDF générée avec succès");
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Erreur lors de la génération de la facture: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la génération de la facture", e);
        }
    }
    
    /**
     * Ajoute une ligne d'information dans un tableau
     */
    private void addInfoRow(Table table, String label, String value) {
        Cell labelCell = new Cell()
                .add(new Paragraph(label).setBold())
                .setBorder(null)
                .setPaddingBottom(5)
                .setPaddingTop(5);
        
        Cell valueCell = new Cell()
                .add(new Paragraph(value))
                .setBorder(null)
                .setPaddingBottom(5)
                .setPaddingTop(5);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
    
    
    /**
     * Calcule le poids total des colis d'une réservation
     */
    private String calculateTotalWeight(Booking booking) {
        if (booking.getParcels() == null || booking.getParcels().isEmpty()) {
            return "0 kg";
        }
        
        float totalWeight = booking.getParcels().stream()
            .filter(parcel -> parcel.getWeightKg() != null)
            .map(parcel -> parcel.getWeightKg())
            .reduce(0f, Float::sum);
            
        return String.format("%.2f kg", totalWeight);
    }
}
