package com.notifymock.core;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.dropwizard.lifecycle.Managed;
import lombok.SneakyThrows;

public class MockWorker implements Managed {

	private final String scenarioStartName;

	private final String acknowledgeUrl;

	private final String getUrl;

	private final String acknowledgeReadNextUrl;

	private final String messageUrl;

	private final WireMockServer wireMockServer;

	@Inject
	public MockWorker(final Config config,
					  final MockRequestListener mockRequestListener,
					  final MockBodyTransformer mockBodyTransformer) {

		this.scenarioStartName = config.getString("wiremock.scenario.start");
		this.acknowledgeUrl = config.getString("wiremock.notify.acknowledgeUrl");
		this.getUrl = config.getString("wiremock.notify.getUrl");
		this.acknowledgeReadNextUrl = config.getString("wiremock.notify.acknowledgeReadNextUrl");
		this.messageUrl = config.getString("wiremock.rtm.messagesUrl");
		final int processingDelay = config.getInt("wiremock.processingDelay");
		final int port = config.getInt("wiremock.port");

		wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig()
			.port(port)
			.extensions(mockBodyTransformer));
		wireMockServer.setGlobalFixedDelay(processingDelay);
		wireMockServer.addMockServiceRequestListener(mockRequestListener);

		stubScenarioStartStopEndpoint();
		stubAcknowledgeMessage();
		stubGetMessage();
		stubAcknowledgeReadNext();
		stubMessage();

	}

	@Override
	public void start() throws Exception {
		wireMockServer.start();
	}

	@Override
	public void stop() throws Exception {
		wireMockServer.stop();
	}

	private void stubScenarioStartStopEndpoint() {

		wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/start")).inScenario("Mock")
				.whenScenarioStateIs(Scenario.STARTED)
				.willReturn(WireMock.aResponse().withStatus(200).withTransformers())
				.willSetStateTo(scenarioStartName));

		wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/stop")).inScenario("Mock")
				.whenScenarioStateIs(scenarioStartName)
				.willReturn(WireMock.aResponse().withStatus(200))
				.willSetStateTo(Scenario.STARTED));

	}

	private void stubAcknowledgeMessage() {

		wireMockServer.stubFor(WireMock.delete(WireMock.urlMatching(acknowledgeUrl)).inScenario("Mock")
				.whenScenarioStateIs(scenarioStartName)
				.withHeader("Authorization", WireMock.matching("NEXUS_S2S .*"))
				.willReturn(WireMock.aResponse().withStatus(200)));

	}

	@SneakyThrows
	private void stubGetMessage() {

		wireMockServer.stubFor(WireMock.get(WireMock.urlPathMatching(getUrl)).inScenario("Mock")
			.whenScenarioStateIs(Scenario.STARTED)
			.withHeader("Authorization", WireMock.matching("NEXUS_S2S .*"))
			.willReturn(WireMock.aResponse().withStatus(204)));

		wireMockServer.stubFor(WireMock.get(WireMock.urlPathMatching(getUrl)).inScenario("Mock")
				.whenScenarioStateIs(scenarioStartName)
				.withHeader("Authorization", WireMock.matching("NEXUS_S2S .*"))
				.willReturn(WireMock.aResponse()
					.withStatus(200)
					.withHeader("Content-Type", "application/json")));

	}

	@SneakyThrows
	private void stubAcknowledgeReadNext() {

		wireMockServer.stubFor(WireMock.put(WireMock.urlPathMatching(acknowledgeReadNextUrl)).inScenario("Mock")
			.whenScenarioStateIs(Scenario.STARTED)
			.withHeader("Authorization", WireMock.matching("NEXUS_S2S .*"))
			.withHeader("Content-Length", WireMock.matching("0"))
			.willReturn(WireMock.aResponse().withStatus(204)));

		wireMockServer.stubFor(WireMock.put(WireMock.urlPathMatching(acknowledgeReadNextUrl)).inScenario("Mock")
				.whenScenarioStateIs(scenarioStartName)
				.withHeader("Authorization", WireMock.matching("NEXUS_S2S .*"))
				.withHeader("Content-Length", WireMock.matching("0"))
				.willReturn(WireMock.aResponse()
					.withStatus(200)
					.withHeader("Content-Type", "application/json")));

	}

	private void stubMessage() {

		wireMockServer.stubFor(WireMock.post(WireMock.urlPathMatching(messageUrl)).inScenario("Mock")
			.whenScenarioStateIs(scenarioStartName)
			.willReturn(WireMock.aResponse()
					.withStatus(200)));

	}

}
