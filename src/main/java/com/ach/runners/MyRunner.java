package com.ach.runners;

import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ach.component.SftpUploader;


@Component
public class MyRunner implements CommandLineRunner {
	
	record Payment(String receiverName, String receiverBank, String receiverAcc, int amount, String referenceId) {}
	
	//private RemoteFileService service;

	@Override
	public void run(String... args) throws Exception {
		 String routingNumber = "021000021"; // Origin FI IFSC or routing number
	        String fileName = "FI_" + routingNumber + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".ach";

	        List<Payment> payments = List.of(
	                new Payment("GANESH REDDY", "SBI BANK", "998887000001", 10000, "PAY00001"),
	                new Payment("RAHUL SHARMA", "HDFC BANK", "888777600000", 5000, "PAY00002")
	        );

	        StringBuilder sb = new StringBuilder();

	        // --- Header Record (prefix 1) ---
	        String header = "1" +
	                padRight(routingNumber, 15) +
	                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")) +
	                padRight("SAMPLE_HEADER", 72); // rest padded
	        sb.append(fixLength(header, 94, ' ')).append("\n");

	        int totalAmount = 0;

	        // --- Entry Detail Records (prefix 6) ---
	        for (Payment payment : payments) {
	            String record = "6" +
	                    padRight(payment.receiverAcc(), 12) +
	                    padLeft(String.valueOf(payment.amount()), 10, '0') +
	                    padRight(payment.receiverName(), 20) +
	                    padRight(payment.receiverBank(), 15) +
	                    padRight(payment.referenceId(), 10);
	            sb.append(fixLength(record, 94, ' ')).append("\n");
	            totalAmount += payment.amount();
	        }

	        // --- File Control Record (prefix 9) ---
	        String control = "9" +
	                padLeft(String.valueOf(payments.size()), 6, '0') +
	                padLeft(String.valueOf(totalAmount), 10, '0');
	        sb.append(fixLength(control, 94, '9')).append("\n");

	        // --- Write to file ---
	        try (FileWriter fw = new FileWriter(fileName)) {
	            fw.write(sb.toString());
	        }

	        System.out.println("ACH file created: " + fileName);
	        SftpUploader.upload(
	                fileName,
	                "/test", //remote directory                         
	                "eu-central-1.sftpcloud.io",   //host      
	                22,//port
	                "bc88f065c4c64fa8bee775fb86387dbe", //username                  
	                "yjqsQNkrQgfaCQEnrn4unjR2vIAalRSu"     //password                
	            );
                //service.logFileUpload(fileName,"/test","PUSH");
	    }

	    private String padRight(String input, int length) {
	        return String.format("%-" + length + "s", input == null ? "" : input);
	    }

	    private String padLeft(String input, int length, char padChar) {
	        return String.format("%" + length + "s", input == null ? "" : input).replace(' ', padChar);
	    }

	    private String fixLength(String input, int requiredLength, char padChar) {
	        if (input.length() >= requiredLength) return input.substring(0, requiredLength);
	        return input + String.valueOf(padChar).repeat(requiredLength - input.length());
	    }
	    }
	

