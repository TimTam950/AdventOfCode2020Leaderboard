package io.twinterf.AdventOfCode2020Leaderboard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@SpringBootApplication
public class AdventOfCode2020LeaderboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdventOfCode2020LeaderboardApplication.class, args);
	}

	@Bean
	public Function<String, String> uppercase() {
		return value -> {
			var client = HttpClient.newHttpClient();

			// get leaderboard
			var getLeaderboardRequest = HttpRequest.newBuilder()
					.uri(URI.create(System.getenv("ADVENT_LEADERBOARD_ADDRESS")))
					.header("cookie", System.getenv("ADVENT_SESSION_TOKEN"))
					.build();

			HttpResponse<String> response = null;
			try {
				response = client.send(getLeaderboardRequest, HttpResponse.BodyHandlers.ofString());
			} catch (IOException e) {
				e.printStackTrace();
				return "error";
			} catch (InterruptedException e) {
				e.printStackTrace();
				return "error";
			}

			JsonNode node = null;
			try {
				node = new ObjectMapper().readTree(response.body());
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

			var leaderboardEntries = new ArrayList<LeaderboardEntry>();
			for (Iterator<JsonNode> it = node.get("members").elements(); it.hasNext(); ) {
				JsonNode user = it.next();
				int localScore = user.get("local_score").intValue();
				String username = user.get("name").toString();
				leaderboardEntries.add(new LeaderboardEntry(username, localScore));
			}

			List<LeaderboardEntry> sortedEntries =  leaderboardEntries.stream()
					.sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
					.collect(Collectors.toList());

			var outputMessage = String.format("The current top 5 are:\n" +
					"1. %s with %s points\n" +
					"2. %s with %s points\n" +
					"3. %s with %s points\n" +
					"4. %s with %s points\n" +
					"5. %s with %s points\n",
					sortedEntries.get(0).getUsername(), sortedEntries.get(0).getScore(),
					sortedEntries.get(1).getUsername(), sortedEntries.get(1).getScore(),
					sortedEntries.get(2).getUsername(), sortedEntries.get(2).getScore(),
					sortedEntries.get(3).getUsername(), sortedEntries.get(3).getScore(),
					sortedEntries.get(4).getUsername(), sortedEntries.get(4).getScore());

			String message = String.format("{\"text\":\"%s\"}", outputMessage.replaceAll("\"", ""));
			System.out.println(message);

			// send to teams
			var postToTeamsRequest = HttpRequest.newBuilder()
					.uri(URI.create(System.getenv("OUTLOOK_WEBHOOK_ADDRESS")))
					.POST(HttpRequest.BodyPublishers.ofString(message))
					.build();

			try {
				client.send(postToTeamsRequest, HttpResponse.BodyHandlers.ofString());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return "ok";
		};
	}
}
