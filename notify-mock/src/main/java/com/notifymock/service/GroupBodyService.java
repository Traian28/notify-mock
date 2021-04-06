package com.notifymock.service;

import com.ea.eadp.notify.models.responses.MessageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import lombok.SneakyThrows;

import java.util.*;

public class GroupBodyService {

	private final ObjectMapper objectMapper;

	private final Map<String, Object> baseMessage;

	private final List<String> types;

	@Inject
	public GroupBodyService(final ObjectMapper objectMapper,
							final Config config) {
		this.objectMapper = objectMapper;
		this.types = config.getStringList("groups.types");
		this.baseMessage = new HashMap<>();
		baseMessage.put("groupTypeId", UUID.randomUUID().toString());
	}

	@SneakyThrows
	public String createGroupBody() {

		final String receiptId = UUID.randomUUID().toString();
		final List<Map<String, Object>> messages = new ArrayList<>();
		final Random random = new Random();
		for (int i = 0; i < 10; i++) {
			final Map<String, Object> message = new HashMap<>(baseMessage);

			final String type = types.get(random.nextInt(types.size()));
			final long timestamp = System.currentTimeMillis();
			final String groupId = UUID.randomUUID().toString();
			final String groupName = UUID.randomUUID().toString();
			final long by = nextPositiveLong(random);
			final long to = nextPositiveLong(random);

			message.put("type", type);
			message.put("timestamp", timestamp);
			message.put("groupGuid", groupId);
			message.put("groupName", groupName);
			message.put("by", by);
			message.put("to", to);

			messages.add(message);
		}

		final MessageResponse response = new MessageResponse(receiptId, messages);

		return objectMapper.writeValueAsString(response);

	}

	private long nextPositiveLong(Random random) {
		long bits, val;
		final long n = 999999999999999999L;
		do {
			bits = (random.nextLong() << 1) >>> 1;
			val = bits % n;
		} while (bits-val+(n-1) < 0L);
		return val;
	}

}
