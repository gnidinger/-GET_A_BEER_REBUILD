package backend.global.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.util.MimeTypeUtils;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.core.Resume;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Configuration
public class RSocketConfig {

	@Value("${spring.application.name}")
	private String appName;

	@Bean
	RSocketRequester getRSocketRequester(RSocketStrategies rSocketStrategies) {
		return RSocketRequester.builder()
			.rsocketConnector(connector -> connector.reconnect(Retry.backoff(10, Duration.ofMillis(500))))
			.rsocketStrategies(rSocketStrategies)
			.dataMimeType(MimeTypeUtils.APPLICATION_JSON)
			.tcp("localhost", 7070);
	}

	@Bean
	RSocket rSocket() {
		return new RSocket() {
			@Override
			public Mono<Void> fireAndForget(Payload payload) {
				String request = payload.getDataUtf8();
				System.out.println("Received fire-and-forget request: " + request);
				return Mono.empty();
			}

			@Override
			public Mono<Payload> requestResponse(Payload payload) {
				String request = payload.getDataUtf8();
				System.out.println("Received request-response request: " + request);
				String response = "Hello from " + appName;
				return Mono.just(DefaultPayload.create(response));
			}

			@Override
			public Flux<Payload> requestStream(Payload payload) {
				String request = payload.getDataUtf8();
				System.out.println("Received request-stream request: " + request);
				String response1 = "Hello from " + appName + " 1";
				String response2 = "Hello from " + appName + " 2";
				String response3 = "Hello from " + appName + " 3";
				return Flux.just(DefaultPayload.create(response1),
					DefaultPayload.create(response2), DefaultPayload.create(response3));
			}
		};
	}

	@Bean
	public RSocketServerCustomizer rSocketServerCustomizer(RSocket rSocket) {
		return rSocketServer -> rSocketServer
			.acceptor((setup, sendingSocket) -> Mono.just(rSocket))
			.resume(new Resume()
				.sessionDuration(Duration.ofMinutes(10))
				.cleanupStoreOnKeepAlive());
	}

	@Bean
	public Mono<RSocket> redisRSocket() {
		RedisURI redisURI = RedisURI.create("redis://localhost");
		RedisClient redisClient = RedisClient.create(redisURI);
		return Mono.defer(() ->
			RSocketConnector.create()
				.metadataMimeType(MimeTypeUtils.APPLICATION_JSON_VALUE)
				.dataMimeType(MimeTypeUtils.ALL_VALUE)
				.connect(TcpClientTransport.create(redisURI.getHost(), redisURI.getPort()))
				.retry()
		);
	}

	@Bean
	public RSocketMessageHandler rSocketMessageHandler(RSocketStrategies rSocketStrategies) {
		RSocketMessageHandler rSocketMessageHandler = new RSocketMessageHandler();
		rSocketMessageHandler.setRSocketStrategies(rSocketStrategies);
		return rSocketMessageHandler;
	}
}
