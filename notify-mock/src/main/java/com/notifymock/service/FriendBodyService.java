package com.notifymock.service;

import com.ea.eadp.notify.models.responses.MessageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import lombok.SneakyThrows;

import java.util.*;

public class FriendBodyService {

	private final ObjectMapper objectMapper;

	private final Map<String, Object> baseMessage;

	private final List<String> types;

	@Inject
	public FriendBodyService(final ObjectMapper objectMapper,
							 final Config config) {
		this.objectMapper = objectMapper;
		this.types = config.getStringList("friends.types");
		this.baseMessage = new HashMap<>();
	}

	@SneakyThrows
	public String createFriendBody() {

		final String receiptId = UUID.randomUUID().toString();
		final List<Map<String, Object>> messages = new ArrayList<>();
		final Random random = new Random();
		for (int i = 0; i < 10; i++) {
			final Map<String, Object> message = new HashMap<>(baseMessage);

			final String type = types.get(random.nextInt(types.size()));
			final String from = UUID.randomUUID().toString();
			final String to = UUID.randomUUID().toString();

			message.put("type", type);
			message.put("from", from);
			message.put("to", to);

			messages.add(message);
		}

		final MessageResponse response = new MessageResponse(receiptId, messages);

		return objectMapper.writeValueAsString(response);

	}

}
