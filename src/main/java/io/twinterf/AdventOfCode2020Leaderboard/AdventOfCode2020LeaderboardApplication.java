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

			var getLeaderboardRequest = constructAdventRequest();

			HttpResponse<String> response = getAdventLeaderboard(client, getLeaderboardRequest);

			if (response == null) return "error";

			JsonNode node = convertResponseToJsonTree(response);

			var leaderboardEntries = constructLeaderboardEntries(node);

			List<LeaderboardEntry> sortedEntries = sortEntriesByScore(leaderboardEntries);

			String messageText = createMessageText(sortedEntries);

			String message = String.format("{\"text\":\"%s\"}", messageText);

			var postToTeamsRequest = constructTeamsRequest(message);

			postMessageToTeams(client, postToTeamsRequest);
			return "ok";
		};
	}

	private List<LeaderboardEntry> sortEntriesByScore(List<LeaderboardEntry> leaderboardEntries) {
		return leaderboardEntries.stream()
				.sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
				.collect(Collectors.toList());
	}

	private void postMessageToTeams(HttpClient client, HttpRequest postToTeamsRequest) {
		try {
			client.send(postToTeamsRequest, HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private HttpRequest constructTeamsRequest(String message) {
		return HttpRequest.newBuilder()
				.uri(URI.create(System.getenv("OUTLOOK_WEBHOOK_ADDRESS")))
				.POST(HttpRequest.BodyPublishers.ofString(message))
				.build();
	}

	private String createMessageText(List<LeaderboardEntry> sortedEntries) {
		StringBuilder sb = new StringBuilder("The current top 5 are:");
		int index = 1;
		for(LeaderboardEntry entry: sortedEntries.subList(0, 5)) {
			sb.append(String.format(" %s. %s with %s points", index, entry.getUsername(), entry.getScore()));
			index++;
		}
		return sb.toString().replaceAll("\"", "");
	}

	private List<LeaderboardEntry> constructLeaderboardEntries(JsonNode node) {
		ArrayList<LeaderboardEntry> leaderboardEntries = new ArrayList<>();
		for (Iterator<JsonNode> it = node.get("members").elements(); it.hasNext(); ) {
			JsonNode user = it.next();
			int localScore = user.get("local_score").intValue();
			String username = user.get("name").toString();
			leaderboardEntries.add(new LeaderboardEntry(username, localScore));
		}
		return leaderboardEntries;
	}

	private JsonNode convertResponseToJsonTree(HttpResponse<String> response) {
		JsonNode node = null;
		try {
			node = new ObjectMapper().readTree(response.body());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return node;
	}

	private HttpResponse<String> getAdventLeaderboard(HttpClient client, HttpRequest getLeaderboardRequest) {
		HttpResponse<String> response;
		try {
			response = client.send(getLeaderboardRequest, HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		return response;
	}

	private HttpRequest constructAdventRequest() {
		return HttpRequest.newBuilder()
				.uri(URI.create(System.getenv("ADVENT_LEADERBOARD_ADDRESS")))
				.header("cookie", System.getenv("ADVENT_SESSION_TOKEN"))
				.build();
	}
}
