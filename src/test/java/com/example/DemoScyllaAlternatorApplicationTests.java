package com.example;

import java.util.List;

import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.IdGenerator;
import org.springframework.util.SimpleIdGenerator;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureJsonTesters
@TestMethodOrder(OrderAnnotation.class)
@Testcontainers(disabledWithoutDocker = true)
class DemoScyllaAlternatorApplicationTests {

	@Container
	static GenericContainer<?> scylladb = new GenericContainer<>("scylladb/scylla")
		.withCommand("--smp 1 --alternator-port 8000 --alternator-write-isolation only_rmw_uses_lwt")
		.withExposedPorts(8000);

	@LocalServerPort
	int port;

	@Autowired
	RestClient.Builder restClientBuilder;

	RestClient restClient;

	@Autowired
	JacksonTester<Movie> movieTester;

	@Autowired
	JacksonTester<List<Movie>> listTester;

	@BeforeEach
	void setUp() {
		this.restClient = this.restClientBuilder.baseUrl("http://localhost:" + port)
			.defaultStatusHandler(new DefaultResponseErrorHandler() {
				@Override
				public void handleError(ClientHttpResponse response) {
					// NO-OP
				}
			})
			.build();
	}

	@DynamicPropertySource
	static void dynamoDbProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.cloud.aws.dynamodb.endpoint",
				() -> "http://localhost:%d".formatted(scylladb.getMappedPort(8000)));
	}

	@Test
	@Order(1)
	void getMovies() throws Exception {
		ResponseEntity<List<Movie>> response = this.restClient.get()
			.uri("/movies")
			.retrieve()
			.toEntity(new ParameterizedTypeReference<>() {
			});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(this.listTester.write(response.getBody())).isEqualToJson("""
				[
				  {
				    "movieId": "00000000-0000-0000-0000-000000000003",
				    "title": "Interstellar",
				    "releaseYear": 2014,
				    "genre": "Adventure",
				    "rating": 8.6,
				    "director": "Christopher Nolan"
				  },
				  {
				    "movieId": "00000000-0000-0000-0000-000000000001",
				    "title": "Inception",
				    "releaseYear": 2010,
				    "genre": "Science Fiction",
				    "rating": 8.8,
				    "director": "Christopher Nolan"
				  },
				  {
				    "movieId": "00000000-0000-0000-0000-000000000002",
				    "title": "The Matrix",
				    "releaseYear": 1999,
				    "genre": "Action",
				    "rating": 8.7,
				    "director": "The Wachowskis"
				  }
				]
				""");
	}

	@Test
	@Order(2)
	void postMovies() throws Exception {
		ResponseEntity<Movie> response = this.restClient.post()
			.uri("/movies")
			.contentType(MediaType.APPLICATION_JSON)
			.body("""
					{
					    "title": "The Dark Knight",
					    "releaseYear": 2008,
					    "genre": "Action",
					    "rating": 9.0,
					    "director": "Christopher Nolan"
					}
					""")
			.retrieve()
			.toEntity(new ParameterizedTypeReference<>() {
			});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(this.movieTester.write(response.getBody())).isEqualToJson("""
				{
				  "movieId": "00000000-0000-0000-0000-000000000004",
				  "title": "The Dark Knight",
				  "releaseYear": 2008,
				  "genre": "Action",
				  "rating": 9.0,
				  "director": "Christopher Nolan"
				}
				""");
	}

	@Test
	@Order(2)
	void getMovie() throws Exception {
		ResponseEntity<Movie> response = this.restClient.get()
			.uri("/movies/00000000-0000-0000-0000-000000000004")
			.retrieve()
			.toEntity(new ParameterizedTypeReference<>() {
			});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(this.movieTester.write(response.getBody())).isEqualToJson("""
				{
				  "movieId": "00000000-0000-0000-0000-000000000004",
				  "title": "The Dark Knight",
				  "releaseYear": 2008,
				  "genre": "Action",
				  "rating": 9.0,
				  "director": "Christopher Nolan"
				}
				""");
	}

	@Test
	@Order(2)
	void getMoviesByTitle() throws Exception {
		ResponseEntity<List<Movie>> response = this.restClient.get()
			.uri("/movies?title=Inception")
			.retrieve()
			.toEntity(new ParameterizedTypeReference<>() {
			});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(this.listTester.write(response.getBody())).isEqualToJson("""
				[
				  {
				    "movieId": "00000000-0000-0000-0000-000000000001",
				    "title": "Inception",
				    "releaseYear": 2010,
				    "genre": "Science Fiction",
				    "rating": 8.8,
				    "director": "Christopher Nolan"
				  }
				]
				""");
	}

	@Test
	@Order(3)
	void getMoviesByGenre() throws Exception {
		ResponseEntity<List<Movie>> response = this.restClient.get()
			.uri("/movies?genre=Action")
			.retrieve()
			.toEntity(new ParameterizedTypeReference<>() {
			});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(this.listTester.write(response.getBody())).isEqualToJson("""
				[
				  {
				    "movieId": "00000000-0000-0000-0000-000000000002",
				    "title": "The Matrix",
				    "releaseYear": 1999,
				    "genre": "Action",
				    "rating": 8.7,
				    "director": "The Wachowskis"
				  },
				  {
				    "movieId": "00000000-0000-0000-0000-000000000004",
				    "title": "The Dark Knight",
				    "releaseYear": 2008,
				    "genre": "Action",
				    "rating": 9.0,
				    "director": "Christopher Nolan"
				  }
				]
				""");
	}

	@Test
	@Order(4)
	void putMovie() throws Exception {
		ResponseEntity<Movie> response = this.restClient.put()
			.uri("/movies/00000000-0000-0000-0000-000000000004")
			.contentType(MediaType.APPLICATION_JSON)
			.body("""
					{
					    "title": "The Dark Knight",
					    "releaseYear": 2008,
					    "genre": "Action",
					    "rating": 8.8,
					    "director": "Christopher Nolan"
					}
					""")
			.retrieve()
			.toEntity(new ParameterizedTypeReference<>() {
			});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(this.movieTester.write(response.getBody())).isEqualToJson("""
				{
				  "movieId": "00000000-0000-0000-0000-000000000004",
				  "title": "The Dark Knight",
				  "releaseYear": 2008,
				  "genre": "Action",
				  "rating": 8.8,
				  "director": "Christopher Nolan"
				}
				""");
	}

	@Test
	@Order(5)
	void deleteMovie() throws Exception {
		ResponseEntity<Void> deleted = this.restClient.delete()
			.uri("/movies/00000000-0000-0000-0000-000000000003")
			.retrieve()
			.toBodilessEntity();
		assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<List<Movie>> response = this.restClient.get()
			.uri("/movies")
			.retrieve()
			.toEntity(new ParameterizedTypeReference<>() {
			});

		assertThat(this.listTester.write(response.getBody())).isEqualToJson("""
				[
				  {
				    "movieId": "00000000-0000-0000-0000-000000000001",
				    "title": "Inception",
				    "releaseYear": 2010,
				    "genre": "Science Fiction",
				    "rating": 8.8,
				    "director": "Christopher Nolan"
				  },
				  {
				    "movieId": "00000000-0000-0000-0000-000000000002",
				    "title": "The Matrix",
				    "releaseYear": 1999,
				    "genre": "Action",
				    "rating": 8.7,
				    "director": "The Wachowskis"
				  },
				  {
				    "movieId": "00000000-0000-0000-0000-000000000004",
				    "title": "The Dark Knight",
				    "releaseYear": 2008,
				    "genre": "Action",
				    "rating": 8.8,
				    "director": "Christopher Nolan"
				  }
				]
				""");
	}

	@TestConfiguration
	static class Config {

		@Bean
		@Primary
		public IdGenerator simpleIdGenerator() {
			return new SimpleIdGenerator();
		}

		@Bean
		public CommandLineRunner clr(DynamoDbEnhancedClient dynamoDbEnhancedClient, DynamoDbTemplate dynamoDbTemplate,
				IdGenerator idGenerator) {
			return args -> {
				dynamoDbEnhancedClient.table("movie", TableSchema.fromBean(Movie.class)).createTable();
				Movie movie1 = new Movie();
				movie1.setMovieId(idGenerator.generateId());
				movie1.setTitle("Inception");
				movie1.setReleaseYear(2010);
				movie1.setGenre("Science Fiction");
				movie1.setRating(8.8);
				movie1.setDirector("Christopher Nolan");
				dynamoDbTemplate.save(movie1);
				Movie movie2 = new Movie();
				movie2.setMovieId(idGenerator.generateId());
				movie2.setTitle("The Matrix");
				movie2.setReleaseYear(1999);
				movie2.setGenre("Action");
				movie2.setRating(8.7);
				movie2.setDirector("The Wachowskis");
				dynamoDbTemplate.save(movie2);
				Movie movie3 = new Movie();
				movie3.setMovieId(idGenerator.generateId());
				movie3.setTitle("Interstellar");
				movie3.setReleaseYear(2014);
				movie3.setGenre("Adventure");
				movie3.setRating(8.6);
				movie3.setDirector("Christopher Nolan");
				dynamoDbTemplate.save(movie3);
			};
		}

	}

}
