package com.greenroom.server.api.domain.greenroom.handler;

import com.greenroom.server.api.domain.greenroom.dto.OneAdornmentCreationDto;
import com.greenroom.server.api.domain.greenroom.dto.TodoCreationDto;
import com.greenroom.server.api.domain.greenroom.service.AdornmentService;
import com.greenroom.server.api.domain.greenroom.service.GradeService;
import com.greenroom.server.api.domain.greenroom.service.TodoService;
import com.greenroom.server.api.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventHandler {
    private final TodoService todoService;
    private final GradeService gradeService;
    private final AdornmentService adornmentService;

    @EventListener
    public void createTodo(TodoCreationDto todoCreationDto){

        todoService.createTodo(todoCreationDto);
    }

    @EventListener
    public void checkingLevelUpAfterSeedUpdate(User user){

         gradeService.checkingLevelUp(user);
    }

    @EventListener
    public void createOneAdornment(OneAdornmentCreationDto oneAdornmentCreationDto){

        adornmentService.createOneAdornment(oneAdornmentCreationDto);
    }


}
