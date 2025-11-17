package com.example.BookMyMovie.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity(name = "movies")
public class Movie extends BaseEntity {

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private int durationMinutes;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getDurationMinutes() {
		return durationMinutes;
	}

	public void setDurationMinutes(int durationMinutes) {
		this.durationMinutes = durationMinutes;
	}
}


