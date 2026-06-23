package com.fbr.invoice.upload.entity;





import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="fbr_item")
public class FbrItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "invoice_id")
	@JsonBackReference
	private FbrInvoiceRequest invoice;

	private String hsCode;

	@Column(columnDefinition = "TEXT")
	private String productDescription;
	private String rate;
	private String uoM;
	private double quantity;
	private double totalValues;
	private double valueSalesExcludingST;
	private double fixedNotifiedValueOrRetailPrice;
	private double salesTaxApplicable;
	private double salesTaxWithheldAtSource;
	private String extraTax;
	private double furtherTax;
	private String sroScheduleNo;
	private double fedPayable;
	private double discount;
	private String saleType;
	private String sroItemSerialNo;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public FbrInvoiceRequest getInvoice() {
	    return invoice;
	}

	public void setInvoice(FbrInvoiceRequest invoice) {
	    this.invoice = invoice;
	}

	public String getHsCode() {
		return hsCode;
	}

	public void setHsCode(String hsCode) {
		this.hsCode = hsCode;
	}

	public String getProductDescription() {
		return productDescription;
	}

	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public String getUoM() {
		return uoM;
	}

	public void setUoM(String uoM) {
		this.uoM = uoM;
	}

	public double getQuantity() {
		return quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public double getTotalValues() {
		return totalValues;
	}

	public void setTotalValues(double totalValues) {
		this.totalValues = totalValues;
	}

	public double getValueSalesExcludingST() {
		return valueSalesExcludingST;
	}

	public void setValueSalesExcludingST(double valueSalesExcludingST) {
		this.valueSalesExcludingST = valueSalesExcludingST;
	}

	public double getFixedNotifiedValueOrRetailPrice() {
		return fixedNotifiedValueOrRetailPrice;
	}

	public void setFixedNotifiedValueOrRetailPrice(double fixedNotifiedValueOrRetailPrice) {
		this.fixedNotifiedValueOrRetailPrice = fixedNotifiedValueOrRetailPrice;
	}

	public double getSalesTaxApplicable() {
		return salesTaxApplicable;
	}

	public void setSalesTaxApplicable(double salesTaxApplicable) {
		this.salesTaxApplicable = salesTaxApplicable;
	}

	public double getSalesTaxWithheldAtSource() {
		return salesTaxWithheldAtSource;
	}

	public void setSalesTaxWithheldAtSource(double salesTaxWithheldAtSource) {
		this.salesTaxWithheldAtSource = salesTaxWithheldAtSource;
	}

	public String getExtraTax() {
		return extraTax;
	}

	public void setExtraTax(String extraTax) {
		this.extraTax = extraTax;
	}

	public double getFurtherTax() {
		return furtherTax;
	}

	public void setFurtherTax(double furtherTax) {
		this.furtherTax = furtherTax;
	}

	public String getSroScheduleNo() {
		return sroScheduleNo;
	}

	public void setSroScheduleNo(String sroScheduleNo) {
		this.sroScheduleNo = sroScheduleNo;
	}

	public double getFedPayable() {
		return fedPayable;
	}

	public void setFedPayable(double fedPayable) {
		this.fedPayable = fedPayable;
	}

	public double getDiscount() {
		return discount;
	}

	public void setDiscount(double discount) {
		this.discount = discount;
	}

	public String getSaleType() {
		return saleType;
	}

	public void setSaleType(String saleType) {
		this.saleType = saleType;
	}

	public String getSroItemSerialNo() {
		return sroItemSerialNo;
	}

	public void setSroItemSerialNo(String sroItemSerialNo) {
		this.sroItemSerialNo = sroItemSerialNo;
	}

}
