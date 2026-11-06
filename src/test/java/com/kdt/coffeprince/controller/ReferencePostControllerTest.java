package com.kdt.coffeprince.controller;

import com.kdt.coffeprince.exception.BusinessExceptionHandler;
import com.kdt.coffeprince.exception.ValidationExceptionHandler;
import com.kdt.coffeprince.service.ReferencePostService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 선택 심화: RestClient 외부 게시글 조회 — 실제 RestClient가 "가짜 외부 서버"를 HTTP로 호출해 실패 처리를 확인한다
// 실제 외부 서비스(jsonplaceholder)에는 오류를 일으키지 않도록, 외부 주소를 테스트마다 가짜 서버·닫힌 포트·응답 없는 주소로 바꾼다
// 실행: ./gradlew test --tests "*ReferencePostControllerTest"
class ReferencePostControllerTest {

    // 응답이 오지 않는 주소(사설 대역 중 사용하지 않는 IP): 연결을 시도하면 응답 없이 기다리다 연결 시간 초과가 난다
    private static final String NO_RESPONSE_ADDRESS = "http://10.255.255.1";

    private HttpServer fakeServer;
    private ExecutorService fakeServerThreads;

    @AfterEach
    void stopFakeServer() {
        if (fakeServer != null) {
            fakeServer.stop(0);
        }
        if (fakeServerThreads != null) {
            fakeServerThreads.shutdownNow();  // 응답을 일부러 늦추던 스레드도 정리
        }
    }

    @Test
    @DisplayName("성공: 외부 서버가 준 게시글(id·userId·title·body)을 200으로 그대로 반환하고, 요청한 postId로 외부를 호출한다")
    void 외부_게시글을_그대로_반환한다() throws Exception {
        AtomicReference<String> calledPath = new AtomicReference<>();
        String baseUrl = startFakeServer(exchange -> {
            calledPath.set(exchange.getRequestURI().getPath());
            respond(exchange, 200, """
                    {"userId": 3, "id": 7, "title": "외부 게시글 제목 7", "body": "외부 게시글 본문 7"}
                    """);
        });

        mockMvcFor(baseUrl).perform(get("/api/v1/reference/posts/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.userId").value(3))
                .andExpect(jsonPath("$.title").value("외부 게시글 제목 7"))
                .andExpect(jsonPath("$.body").value("외부 게시글 본문 7"));

        assertThat(calledPath.get()).isEqualTo("/posts/7");
    }

    @Test
    @DisplayName("404: 외부 서버가 404로 응답하면 404 REFERENCE_POST_NOT_FOUND와 fieldErrors {}를 반환한다")
    void 외부_404면_404를_반환한다() throws Exception {
        String baseUrl = startFakeServer(exchange -> respond(exchange, 404, "{}"));

        mockMvcFor(baseUrl).perform(get("/api/v1/reference/posts/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("REFERENCE_POST_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("외부 게시글을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    @DisplayName("서버 오류: 외부 서버가 500으로 응답하면 502 OUTSIDE_API_FAILED를 반환한다")
    void 외부_서버_오류면_502를_반환한다() throws Exception {
        String baseUrl = startFakeServer(exchange -> respond(exchange, 500, "{}"));

        mockMvcFor(baseUrl).perform(get("/api/v1/reference/posts/1"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("OUTSIDE_API_FAILED"))
                .andExpect(jsonPath("$.message").value("외부 서버를 호출하지 못했습니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    @DisplayName("연결 실패: 아무 서버도 없는 포트로 연결이 거절되면 502 OUTSIDE_API_FAILED를 반환한다")
    void 연결이_거절되면_502를_반환한다() throws Exception {
        String baseUrl = "http://127.0.0.1:" + findClosedPort();

        mockMvcFor(baseUrl).perform(get("/api/v1/reference/posts/1"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("OUTSIDE_API_FAILED"))
                .andExpect(jsonPath("$.message").value("외부 서버를 호출하지 못했습니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    @DisplayName("연결 시간 초과: 3초 안에 연결되지 않으면 504 OUTSIDE_API_TIMEOUT을 반환한다")
    void 연결_시간_초과면_504를_반환한다() throws Exception {
        mockMvcFor(NO_RESPONSE_ADDRESS).perform(get("/api/v1/reference/posts/1"))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.code").value("OUTSIDE_API_TIMEOUT"))
                .andExpect(jsonPath("$.message").value("외부 서버의 응답 시간이 초과되었습니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    @DisplayName("응답 읽기 시간 초과: 연결 후 5초 안에 응답이 없으면 504 OUTSIDE_API_TIMEOUT을 반환한다")
    void 응답_읽기_시간_초과면_504를_반환한다() throws Exception {
        // 연결은 받아 주고, 응답은 6초 뒤에 보낸다 (읽기 제한 5초보다 늦게)
        String baseUrl = startFakeServer(exchange -> {
            try {
                Thread.sleep(6_000);
                respond(exchange, 200, """
                        {"userId": 1, "id": 1, "title": "늦은 응답", "body": "늦은 응답"}
                        """);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        });

        mockMvcFor(baseUrl).perform(get("/api/v1/reference/posts/1"))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.code").value("OUTSIDE_API_TIMEOUT"))
                .andExpect(jsonPath("$.message").value("외부 서버의 응답 시간이 초과되었습니다."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    @DisplayName("입력 오류: postId가 0이면 외부를 호출하지 않고 400 VALIDATION_ERROR와 fieldErrors.postId를 반환한다")
    void postId가_0이면_400을_반환한다() throws Exception {
        AtomicInteger callCount = new AtomicInteger();
        String baseUrl = startFakeServer(exchange -> {
            callCount.incrementAndGet();
            respond(exchange, 200, "{}");
        });

        mockMvcFor(baseUrl).perform(get("/api/v1/reference/posts/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("입력값을 확인해 주세요."))
                .andExpect(jsonPath("$.fieldErrors.postId").exists());

        assertThat(callCount.get()).isZero();
    }

    // 외부 주소만 바꾼 실제 Controller·Service로 MockMvc를 만든다 (오류 응답은 실제 예외 핸들러가 만든다)
    private MockMvc mockMvcFor(String baseUrl) {
        ReferencePostController controller = new ReferencePostController(new ReferencePostService(baseUrl));
        return MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new BusinessExceptionHandler(), new ValidationExceptionHandler())
                .build();
    }

    // 가짜 외부 서버: 빈 포트에 띄우고 /posts/ 아래 요청을 handler대로 응답한다
    private String startFakeServer(HttpHandler handler) throws IOException {
        fakeServerThreads = Executors.newCachedThreadPool();
        fakeServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        fakeServer.createContext("/posts/", handler);
        fakeServer.setExecutor(fakeServerThreads);
        fakeServer.start();
        return "http://127.0.0.1:" + fakeServer.getAddress().getPort();
    }

    private static void respond(HttpExchange exchange, int status, String json) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    // 잠깐 열었다 닫은 포트 번호: 아무 서버도 듣고 있지 않아 연결이 거절된다
    private static int findClosedPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
