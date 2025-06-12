package org.example.expert.domain.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Annotation 처리 때문에 작업중지
@WebMvcTest(TodoController.class)
public class TodoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TodoService todoService;

    @MockBean
    private AuthUserArgumentResolver authUserArgumentResolver;

    @BeforeEach
    void setUp() throws Exception {
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.USER);
        given(authUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(authUserArgumentResolver.resolveArgument(any(), any(), any(), any())).will(i ->{
            System.out.println("resolveArgument called");
            return authUser;
        });
        mockMvc = MockMvcBuilders.standaloneSetup(new TodoController(todoService))
                .setCustomArgumentResolvers(authUserArgumentResolver)
                .setMessageConverters(new MappingJackson2HttpMessageConverter()) // ✅ 이거 추가
                .build();
    }

    @Test
    public void todo_저장시_제목_띄어쓰기() throws Exception {
        //given
        TodoSaveRequest request = new TodoSaveRequest("Title with blank", "contents");
        String jsonBody = objectMapper.writeValueAsString(request);

        //when & then
        mockMvc.perform(post("/todos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody))
                .andExpect(status().isOk())
                .andDo(print());
    }
}