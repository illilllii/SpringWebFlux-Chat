package com.cos.chat;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@CrossOrigin
@RestController
public class ChatController {
	Sinks.Many<String> sink; // processor // 지속적 응답
	public ChatController() {
		this.sink = Sinks.many().multicast().onBackpressureBuffer();
	}
	
	@GetMapping("/send/{username}/{message}")
	public void send(@PathVariable String username, @PathVariable String message) {
		String chat = username + " : " + message;
		sink.tryEmitNext(chat);
	}
	
	@GetMapping(value="/sse")
	public Flux<ServerSentEvent<String>> sse() { // ServerSentEvent의 ContentType은 text event stream
		return sink.asFlux().map(e -> ServerSentEvent.builder(e).build()).doOnCancel(()->{
			System.out.println("SSE 종료됨");
			sink.asFlux().blockLast();
		}); // 구독
	}
} 
