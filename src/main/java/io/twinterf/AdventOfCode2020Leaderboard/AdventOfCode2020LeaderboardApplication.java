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
import java.time.LocalDate;
import java.time.ZoneId;
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
		return timeFromInvoker -> {
			timeFromInvoker = timeFromInvoker.replace("_", ":");
			var client = HttpClient.newHttpClient();

			var getLeaderboardRequest = constructAdventRequest();

			HttpResponse<String> response = getAdventLeaderboard(client, getLeaderboardRequest);

			if (response == null) return "error";

			JsonNode node = convertResponseToJsonTree(response);

			var leaderboardEntries = constructLeaderboardEntries(node);

			List<LeaderboardEntry> sortedEntries = sortEntriesByScore(leaderboardEntries);

			String message = createMessage(sortedEntries, timeFromInvoker);

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
			var response = client.send(postToTeamsRequest, HttpResponse.BodyHandlers.ofString());
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

	private String createMessage(List<LeaderboardEntry> sortedEntries, String time) {
		ZoneId zonedId = ZoneId.of( "America/New_York" );
		LocalDate today = LocalDate.now( zonedId );
		var template = "{ \"type\": \"message\", \"attachments\":[ { \"contentType\":\"application/vnd.microsoft.card.adaptive\", \"contentUrl\":null, \"content\":{ \"$schema\":\"http://adaptivecards.io/schemas/adaptive-card.json\", \"type\":\"AdaptiveCard\", \"version\":\"1.2\", \"body\": [ { \"type\": \"TextBlock\", \"size\": \"Medium\", \"weight\": \"Bolder\", \"text\": \"Leaderboard as of: TIME_PLACEHOLDER DATE_PLACEHOLDER\" }, { \"type\": \"ColumnSet\", \"columns\": [ { \"type\": \"Column\", \"items\": [ { \"type\": \"Image\", \"url\": \"https://banner2.cleanpng.com/20171127/fa1/gold-number-one-png-clipart-image-5a1bd3372a8220.6763212615117729831741.jpg\", \"size\": \"Small\", \"isVisible\": false } ], \"width\": \"auto\" }, { \"type\": \"Column\", \"items\": [ { \"type\": \"TextBlock\", \"weight\": \"Bolder\", \"text\": \"LEADER_NAME_PLACEHOLDER\", \"wrap\": true }, { \"type\": \"TextBlock\", \"spacing\": \"None\", \"text\": \"LEADER_SCORE_PLACEHOLDER\", \"isSubtle\": true, \"wrap\": true } ], \"width\": \"stretch\" } ] }, { \"type\": \"ColumnSet\", \"columns\": [ { \"type\": \"Column\", \"items\": [ { \"type\": \"Image\", \"url\": \"https://banner2.cleanpng.com/20171127/7c9/gold-number-two-png-clipart-image-5a1bd332aceb10.0628622515117729787083.jpg\", \"size\": \"Small\", \"isVisible\": false } ], \"width\": \"auto\" }, { \"type\": \"Column\", \"items\": [ { \"type\": \"TextBlock\", \"weight\": \"Bolder\", \"text\": \"LEADER_NAME_PLACEHOLDER\", \"wrap\": true }, { \"type\": \"TextBlock\", \"spacing\": \"None\", \"text\": \"LEADER_SCORE_PLACEHOLDER\", \"isSubtle\": true, \"wrap\": true } ], \"width\": \"stretch\" } ] }, { \"type\": \"ColumnSet\", \"columns\": [ { \"type\": \"Column\", \"items\": [ { \"type\": \"Image\", \"url\": \"https://banner2.cleanpng.com/20171127/7c9/gold-number-two-png-clipart-image-5a1bd332aceb10.0628622515117729787083.jpg\", \"size\": \"Small\", \"isVisible\": false } ], \"width\": \"auto\" }, { \"type\": \"Column\", \"items\": [ { \"type\": \"TextBlock\", \"weight\": \"Bolder\", \"text\": \"LEADER_NAME_PLACEHOLDER\", \"wrap\": true }, { \"type\": \"TextBlock\", \"spacing\": \"None\", \"text\": \"LEADER_SCORE_PLACEHOLDER\", \"isSubtle\": true, \"wrap\": true } ], \"width\": \"stretch\" } ] }, { \"type\": \"ColumnSet\", \"columns\": [ { \"type\": \"Column\", \"items\": [ { \"type\": \"Image\", \"url\": \"https://banner2.cleanpng.com/20171127/7c9/gold-number-two-png-clipart-image-5a1bd332aceb10.0628622515117729787083.jpg\", \"size\": \"Small\", \"isVisible\": false } ], \"width\": \"auto\" }, { \"type\": \"Column\", \"items\": [ { \"type\": \"TextBlock\", \"weight\": \"Bolder\", \"text\": \"LEADER_NAME_PLACEHOLDER\", \"wrap\": true }, { \"type\": \"TextBlock\", \"spacing\": \"None\", \"text\": \"LEADER_SCORE_PLACEHOLDER\", \"isSubtle\": true, \"wrap\": true } ], \"width\": \"stretch\" } ] }, { \"type\": \"ColumnSet\", \"columns\": [ { \"type\": \"Column\", \"items\": [ { \"type\": \"Image\", \"url\": \"https://banner2.cleanpng.com/20171127/7c9/gold-number-two-png-clipart-image-5a1bd332aceb10.0628622515117729787083.jpg\", \"size\": \"Small\", \"isVisible\": false } ], \"width\": \"auto\" }, { \"type\": \"Column\", \"items\": [ { \"type\": \"TextBlock\", \"weight\": \"Bolder\", \"text\": \"LEADER_NAME_PLACEHOLDER\", \"wrap\": true }, { \"type\": \"TextBlock\", \"spacing\": \"None\", \"text\": \"LEADER_SCORE_PLACEHOLDER\", \"isSubtle\": true, \"wrap\": true } ], \"width\": \"stretch\" } ] } ], \"backgroundImage\": { \"url\": \"https://miro.medium.com/max/1200/1*XtCMwEXZe2VcH-jfcHwCBQ.jpeg\" } } } ] }";
		for(LeaderboardEntry entry: sortedEntries.subList(0, 5)) {
			template = template.replaceFirst("TIME_PLACEHOLDER", time)
					.replaceFirst("DATE_PLACEHOLDER", today.toString())
					.replaceFirst("LEADER_NAME_PLACEHOLDER", entry.getUsername())
					.replaceFirst("LEADER_SCORE_PLACEHOLDER", Integer.toString(entry.getScore())).replace("\"\"", "\"");
		}
		return template;
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
