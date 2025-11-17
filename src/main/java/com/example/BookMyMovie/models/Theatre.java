package com.example.BookMyMovie.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity(name = "theatres")
public class Theatre extends BaseEntity {

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String address;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
}
