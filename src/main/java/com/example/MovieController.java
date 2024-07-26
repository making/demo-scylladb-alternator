package com.example;

import java.util.List;
import java.util.UUID;

import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import org.springframework.http.HttpStatus;
import org.springframework.util.IdGenerator;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/movies")
public class MovieController {

	private final DynamoDbTemplate dynamoDbTemplate;

	private final IdGenerator idGenerator;

	public MovieController(DynamoDbTemplate dynamoDbTemplate, IdGenerator idGenerator) {
		this.dynamoDbTemplate = dynamoDbTemplate;
		this.idGenerator = idGenerator;
	}

	@PostMapping
	public Movie postMovie(@RequestBody Movie movie) {
		movie.setMovieId(this.idGenerator.generateId());
		return this.dynamoDbTemplate.save(movie);
	}

	@GetMapping("/{id}")
	public Movie getMovie(@PathVariable UUID id) {
		Key key = Key.builder().partitionValue(id.toString()).build();
		return this.dynamoDbTemplate.load(key, Movie.class);
	}

	@GetMapping
	public List<Movie> listMovies(@RequestParam(required = false) String title,
			@RequestParam(required = false) String genre) {
		PageIterable<Movie> pages;
		if (StringUtils.hasText(title)) {
			pages = this.dynamoDbTemplate.query(QueryEnhancedRequest.builder()
				.queryConditional(QueryConditional.keyEqualTo(key -> key.partitionValue(title)))
				.build(), Movie.class, "title-index");
		}
		else if (StringUtils.hasText(genre)) {
			pages = this.dynamoDbTemplate.query(QueryEnhancedRequest.builder()
				.queryConditional(QueryConditional.keyEqualTo(key -> key.partitionValue(genre)))
				.build(), Movie.class, "genre-index");
		}
		else {
			pages = this.dynamoDbTemplate.scanAll(Movie.class);
		}
		return pages.items().stream().toList();
	}

	@PutMapping("/{id}")
	public Movie updateMovie(@PathVariable UUID id, @RequestBody Movie movie) {
		movie.setMovieId(id);
		return this.dynamoDbTemplate.save(movie);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteMovie(@PathVariable UUID id) {
		Key key = Key.builder().partitionValue(id.toString()).build();
		this.dynamoDbTemplate.delete(key, Movie.class);
	}

}
