package com.ach.dto;

import lombok.Data;

@Data
public class PaymentInstruction {
	private String transactionCode;
    private String receivingDFIRouting;
    private String receivingDFIAccount;
    private long amount;
    private String individualIdNumber;
    private String individualName;

}
