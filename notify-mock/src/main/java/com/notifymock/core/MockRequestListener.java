package com.notifymock.core;

import com.ea.eadp.antelope.rtm.models.RTMMessage;
import com.ea.eadp.antelope.rtm.models.notifications.NotificationRequest;
import com.ea.eadp.antelope.service.metrics.StatsDMetrics;
import com.ea.eadp.antelope.service.metrics.model.Metrics;
import com.ea.eadp.notify.models.friends.FriendEvent;
import com.ea.eadp.notify.models.groups.GroupEvent;
import com.ea.eadp.notify.models.responses.MessageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.Response;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import lombok.SneakyThrows;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MockRequestListener implements RequestListener{

	private final StatsDMetrics metrics;

	private final ObjectMapper objectMapper;

	private final Pattern getUrlPattern;

	private final Pattern acknowledgeReadNextUrlPattern;

	private final Pattern messagesUrlPattern;

	private final Map<String, Metrics.Time> timerMap;

	@Inject
	public MockRequestListener(final StatsDMetrics metrics,
							   final ObjectMapper objectMapper,
							   final Config config) {
		this.metrics = metrics;
		this.objectMapper = objectMapper;
		this.getUrlPattern = Pattern.compile(config.getString("wiremock.notify.getUrl"));
		this.acknowledgeReadNextUrlPattern = Pattern.compile(config.getString("wiremock.notify.acknowledgeReadNextUrl"));
		this.messagesUrlPattern = Pattern.compile(config.getString("wiremock.rtm.messagesUrl"));
		this.timerMap = new HashMap<>();
	}

	@Override
	@SneakyThrows
	public void requestReceived(Request request, Response response) {

		final String url = request.getUrl();

		final Matcher acknowledgeReadNextUrlMatcher = acknowledgeReadNextUrlPattern.matcher(url);

		if (acknowledgeReadNextUrlMatcher.matches()) {
			final MessageResponse messageResponse = objectMapper.readValue(response.getBody(), MessageResponse.class);
			if (url.contains("group")) {
				messageResponse.getMessages().stream()
					.map(map -> objectMapper.convertValue(map, GroupEvent.class))
					.forEach(event -> timerMap.put(createMetricKey(event), createMetricTime(new String[] { "group", event.getType() })));
			} else if (url.contains("friendship")) {
				messageResponse.getMessages().stream()
					.map(map -> objectMapper.convertValue(map, FriendEvent.class))
					.forEach(event -> timerMap.put(createMetricKey(event), createMetricTime(new String[] { "friend", event.getType() })));
			}
			return;
		}

		final Matcher getUrlMatcher = getUrlPattern.matcher(url);

		if (getUrlMatcher.matches()) {
			final MessageResponse messageResponse = objectMapper.readValue(response.getBody(), MessageResponse.class);
			if (url.contains("group")) {
				messageResponse.getMessages().stream()
					.map(map -> objectMapper.convertValue(map, GroupEvent.class))
					.forEach(event -> timerMap.put(createMetricKey(event), createMetricTime(new String[] { "group", event.getType() })));

			} else if (url.contains("friendship")) {
				messageResponse.getMessages().stream()
					.map(map -> objectMapper.convertValue(map, FriendEvent.class))
					.forEach(event -> timerMap.put(createMetricKey(event), createMetricTime(new String[] { "friend", event.getType() })));
			}
			return;
		}

		final Matcher messagesUrlMatcher = messagesUrlPattern.matcher(url);

		if (messagesUrlMatcher.matches()) {
			final RTMMessage rtmMessage = objectMapper.readValue(request.getBody(), RTMMessage.class);
			final String toId = rtmMessage.getTo().getId();
			final Metrics.Time time = timerMap.remove(toId);
			if (time != null) {
				metrics.time(time.withEnd(DateTime.now(DateTimeZone.UTC)));
			}
			metrics.increment(Metrics.Increment.builder()
				.tags(new String[] { "rtm", "notification" })
				.build());
			return;
		}

	}

	private String createMetricKey(final GroupEvent groupEvent) {
		return groupEvent.getGroupId();
	}

	private String createMetricKey(final FriendEvent friendEvent) {
		return friendEvent.getTo();
	}

	private Metrics.Time createMetricTime(final String[] tags) {
		final Metrics.Time time = Metrics.Time.builder()
			.start(DateTime.now(DateTimeZone.UTC))
			.tags(tags)
			.build();
		return time;
	}

}
