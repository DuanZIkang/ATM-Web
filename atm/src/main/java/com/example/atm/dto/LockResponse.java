package com.example.atm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LockResponse {
	private String card;
	private String name;
	private String status;
}
