package com.kdt.coffeprince.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass //테이블을 만들지 않고 필드만 자식에게 물려줌
@EntityListeners(AuditingEntityListener.class) // 저장/수정 시점을 감시해서 날짜를 자동으로 입력
public class BaseTimeEntity {

    @CreatedDate
    @Column(updatable = false) // 처음 저장된 시각 고정, 수정해도 안바뀜
    private LocalDateTime createdDate;

    @LastModifiedDate //수정할 때마다 자동 갱신
    private LocalDateTime modifiedDate;
}

