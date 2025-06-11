package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {
    @InjectMocks
    private TodoService todoService;

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private WeatherClient weatherClient;

    @Test
    public void todo_정상등록 () {
        //given
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.USER);
        TodoSaveRequest request = new TodoSaveRequest("Title", "Contents");

        when(weatherClient.getTodayWeather()).thenReturn("TodayWeather");
        when(todoRepository.save(any(Todo.class))).thenAnswer(i -> {
            Todo savedTodo = i.getArgument(0);
            savedTodo.setId(1L);
            return savedTodo;
        });

        //when
        TodoSaveResponse response = todoService.saveTodo(authUser, request);

        //then
        assertEquals(1L, response.getId());
        assertEquals("Title", response.getTitle());
        assertEquals("Contents", response.getContents());
        assertEquals("TodayWeather", response.getWeather());
        assertEquals(1L, response.getUser().getId());
        assertEquals("test@example.com", response.getUser().getEmail());

        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    public void todo_Page_정상조회 () {
        //given
        int page = 1;
        int size = 3;
        Pageable pageable = PageRequest.of(page - 1, size);

        User testUser = new User("test@example.com", "password", UserRole.USER);
        testUser.setId(1L);
        Todo testTodo = new Todo("title", "contents", "weather", testUser);
        testTodo.setId(1L);

        List<Todo> testList = new ArrayList<>();
        testList.add(testTodo);

        Page<Todo> testPage = new PageImpl<>(testList, pageable, 1);
        when(todoRepository.findAllByOrderByModifiedAtDesc(any(Pageable.class))).thenReturn(testPage);
        //when
        Page<TodoResponse> response = todoService.getTodos(page, size);

        //then
        TodoResponse firstTodoResponse = response.getContent().get(0);
        // Todo
        assertEquals(testTodo.getId(), firstTodoResponse.getId());
        assertEquals(testTodo.getTitle(), firstTodoResponse.getTitle());
        // User
        assertEquals(testUser.getId(), firstTodoResponse.getUser().getId());
        assertEquals(testTodo.getUser().getEmail(), firstTodoResponse.getUser().getEmail());
        // Page
        assertEquals(page-1, response.getNumber());
        assertEquals(size, response.getPageable().getPageSize());
        assertEquals(testList.size(), response.getTotalPages());
        assertEquals((testList.size() / size) + 1,response.getTotalPages());

        verify(todoRepository).findAllByOrderByModifiedAtDesc(pageable);
    }
}
