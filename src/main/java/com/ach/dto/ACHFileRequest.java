package com.ach.dto;

import java.util.List;

import lombok.Data;

@Data
public class ACHFileRequest {
	private String immediateDestination;
	private String immediateOrigin;
	private String destinationName;
	private String originName;
	private String companyName;
	private String companyId;
	private String entryDescription;
	private List<PaymentInstruction> paymentInstructions;
	private String clientKey;
	private String fileName;
}
