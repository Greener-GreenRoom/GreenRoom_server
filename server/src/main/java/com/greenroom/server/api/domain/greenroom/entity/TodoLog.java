package com.greenroom.server.api.domain.greenroom.entity;

import com.greenroom.server.api.domain.common.BaseTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "todo_log")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodoLog extends BaseTime {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long todoLogId;

    @ManyToOne
    @JoinColumn(name = "todo_id")
    private Todo todo;

    @Builder
    public TodoLog(Long todoLogId, Todo todo) {
        this.todoLogId = todoLogId;
        this.todo = todo;
    }
}

