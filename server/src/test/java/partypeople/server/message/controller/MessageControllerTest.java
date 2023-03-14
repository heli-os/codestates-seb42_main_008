package partypeople.server.message.controller;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.util.UriComponentsBuilder;
import partypeople.server.message.dto.MessageDto;
import partypeople.server.message.entity.Message;
import partypeople.server.message.mapper.MessageMapper;
import partypeople.server.message.service.MessageService;

import java.net.URI;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MessageControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @MockBean
    private MessageService messageService;

    @MockBean
    private MessageMapper mapper;

    @Test
    void postMessageTest() throws Exception {
        //given
        MessageDto.Post post = new MessageDto.Post("Message Test",
            1L, 2L, 3L);
        String content = gson.toJson(post);
        Message message = new Message();
        message.setMessageId(1L);

        given(mapper.messagePostToMessage(Mockito.any(MessageDto.Post.class))).willReturn(new Message());
        given(messageService.createMessage(Mockito.any(Message.class)))
            .willReturn(message);

        //when
        ResultActions actions = mockMvc.perform(post("/messages")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content));

        //then
        actions
            .andExpect(status().isCreated())
            .andExpect(header().string("location", is(startsWith("/messages/"))));
    }

    @Test
    void getMessageTest() throws Exception {
        //given
        long messageId = 1L;
        MessageDto.Response response = new MessageDto.Response(1L,
            "Message Test", 1L,
            new MessageDto.Response.Sender(2L, "test"), LocalDateTime.now(), true);

        given(messageService.findMessage(Mockito.anyLong())).willReturn(new Message());
        given(mapper.messageToMessageResponse(Mockito.any(Message.class))).willReturn(response);
        URI uri = UriComponentsBuilder.newInstance().path("/messages/{message-Id}").buildAndExpand(messageId).toUri();

        //when
        ResultActions actions = mockMvc.perform(
            get(uri)
                .accept(MediaType.APPLICATION_JSON));

        //then
        actions
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").value(response.getContent()))
            .andExpect(jsonPath("$.data.companionId").value(response.getCompanionId()))
            .andExpect(jsonPath("$.data.sender.id").value(2L))
            .andExpect(jsonPath("$.data.read").value(true));
    }

    @Test
    void getMessages() {
    }

    @Test
    void deleteMessage() {
    }
}