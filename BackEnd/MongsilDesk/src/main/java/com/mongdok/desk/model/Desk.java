package com.mongdok.desk.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Entity(name = "desk")
public class Desk {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int deskId;
	
	private String promise;
	
	private String userId;
}
