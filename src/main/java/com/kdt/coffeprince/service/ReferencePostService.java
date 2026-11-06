package com.kdt.coffeprince.service;

import com.kdt.coffeprince.dto.ReferencePostResponse;
import com.kdt.coffeprince.exception.BusinessException;
import com.kdt.coffeprince.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.time.Duration;

// 선택 심화: RestClient로 외부 서버의 /posts/{postId}를 조회한다
// 실패는 두 종류로 나눠 처리한다
//  1) 외부 서버가 오류 상태로 "응답한" 경우  → onStatus      (404 → 404, 그 밖의 오류 → 502)
//  2) 응답 자체를 "받지 못한" 경우           → ResourceAccessException (시간 초과 → 504, 연결 거절·도메인 오류 → 502)
@Service
public class ReferencePostService {

    static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);  // 연결 대기 최대 3초
    static final Duration READ_TIMEOUT = Duration.ofSeconds(5);     // 연결 후 응답 읽기 대기 최대 5초

    private final RestClient restClient;

    // 외부 주소를 생성자로 받으므로, 테스트에서는 가짜 외부 서버 주소를 넣어 바로 만들 수 있다
    public ReferencePostService(@Value("${reference.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public ReferencePostResponse getPost(Long postId) {
        try {
            return restClient.get()
                    .uri("/posts/{postId}", postId)
                    .retrieve()
                    .onStatus(status -> status.isSameCodeAs(HttpStatus.NOT_FOUND), (request, response) -> {
                        throw new BusinessException(ErrorCode.REFERENCE_POST_NOT_FOUND);
                    })
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new BusinessException(ErrorCode.OUTSIDE_API_FAILED);
                    })
                    .body(ReferencePostResponse.class);
        } catch (ResourceAccessException e) {
            // 연결 시간 초과("Connect timed out")와 읽기 시간 초과("Read timed out") 모두 SocketTimeoutException
            if (e.getCause() instanceof SocketTimeoutException) {
                throw new BusinessException(ErrorCode.OUTSIDE_API_TIMEOUT);
            }
            // 연결 거절(ConnectException), 도메인 오류(UnknownHostException) 등
            throw new BusinessException(ErrorCode.OUTSIDE_API_FAILED);
        }
    }
}
