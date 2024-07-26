package com.example;

import java.util.UUID;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@DynamoDbBean
public class Movie {

	private UUID movieId;

	private String title;

	private int releaseYear;

	private String genre;

	private double rating;

	private String director;

	@DynamoDbPartitionKey
	public UUID getMovieId() {
		return movieId;
	}

	public void setMovieId(UUID movieId) {
		this.movieId = movieId;
	}

	@DynamoDbSecondaryPartitionKey(indexNames = "title-index")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@DynamoDbSecondaryPartitionKey(indexNames = "genre-index")
	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public int getReleaseYear() {
		return releaseYear;
	}

	public void setReleaseYear(int releaseYear) {
		this.releaseYear = releaseYear;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public String getDirector() {
		return director;
	}

	public void setDirector(String director) {
		this.director = director;
	}

	@Override
	public String toString() {
		return "Movie{" + "movieId=" + movieId + ", title='" + title + '\'' + ", releaseYear=" + releaseYear
				+ ", genre='" + genre + '\'' + ", rating=" + rating + ", director='" + director + '\'' + '}';
	}

}
