package com.notifymock.core;

import com.notifymock.service.FriendBodyService;
import com.notifymock.service.GroupBodyService;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MockBodyTransformer extends ResponseTransformer {

	private final FriendBodyService friendBodyService;

	private final GroupBodyService groupBodyService;

	private final Pattern getUrlPattern;

	private final Pattern acknowledgeReadNextUrlPattern;

	@Inject
	public MockBodyTransformer(final FriendBodyService friendBodyService,
							   final GroupBodyService groupBodyService,
							   final Config config) {

		this.friendBodyService = friendBodyService;
		this.groupBodyService = groupBodyService;
		this.getUrlPattern = Pattern.compile(config.getString("wiremock.notify.getUrl"));
		this.acknowledgeReadNextUrlPattern = Pattern.compile(config.getString("wiremock.notify.acknowledgeReadNextUrl"));

	}

	@Override
	public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource fileSource) {

		final String url = request.getUrl();

		final Matcher acknowledgeReadNextUrlMatcher = acknowledgeReadNextUrlPattern.matcher(url);

		if (acknowledgeReadNextUrlMatcher.matches()) {
			if (url.contains("group")) {
				return ResponseDefinitionBuilder
					.like(responseDefinition).but()
					.withBody(groupBodyService.createGroupBody())
					.build();
			} else if (url.contains("friendship")) {
				return ResponseDefinitionBuilder
					.like(responseDefinition).but()
					.withBody(friendBodyService.createFriendBody())
					.build();
			}
		}

		final Matcher getUrlMatcher = getUrlPattern.matcher(url);

		if (getUrlMatcher.matches()) {
			if (url.contains("group")) {
				return ResponseDefinitionBuilder
					.like(responseDefinition).but()
					.withBody(groupBodyService.createGroupBody())
					.build();
			} else if (url.contains("friendship")) {
				return ResponseDefinitionBuilder
					.like(responseDefinition).but()
					.withBody(friendBodyService.createFriendBody())
					.build();
			}
		}

		return responseDefinition;

	}

	@Override
	public String name() {
		return MockBodyTransformer.class.getSimpleName();
	}

}
