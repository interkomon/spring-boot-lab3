package ru.kovyazin.lab3;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.kovyazin.lab3.controller.MyController;
import ru.kovyazin.lab3.model.*;
import ru.kovyazin.lab3.service.*;
import ru.kovyazin.lab3.util.DateTimeUtil;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class Lab3FeaturesTest {
    @Autowired
    @Qualifier("ModifySystemTimeResponseService")
    private ModifyResponseService timeModifier;
    @Autowired
    @Qualifier("ModifyOperationUidResponseService")
    private ModifyResponseService uidModifier;
    @Autowired
    private ValidationService validation;
    @Autowired
    private ObjectMapper mapper;

    private Response response() {
        return Response.builder().uid("42").operationUid("op-42")
                .systemTime("1999-01-01T00:00:00Z").code(Codes.SUCCESS)
                .errorCode(ErrorCodes.EMPTY).errorMessage(ErrorMessages.EMPTY).build();
    }

    @Test
    void qualifiersResolveDifferentServices() {
        assertInstanceOf(ModifySystemTimeResponseService.class, timeModifier);
        assertInstanceOf(ModifyOperationUidResponseService.class, uidModifier);
    }

    @Test
    void uuidModifierChangesOnlyOperationUid(CapturedOutput output) {
        Response response = response();
        assertSame(response, uidModifier.modify(response));
        assertEquals(4, UUID.fromString(response.getOperationUid()).version());
        assertEquals("42", response.getUid());
        assertEquals("1999-01-01T00:00:00Z", response.getSystemTime());
        assertEquals(Codes.SUCCESS, response.getCode());
        assertTrue(output.getAll().contains("response.operationUid: op-42 ->"));
    }

    @Test
    void timeModifierChangesOnlyTime(CapturedOutput output) {
        Response response = response();
        Instant before = Instant.now().minusSeconds(1);
        assertSame(response, timeModifier.modify(response));
        assertFalse(Instant.parse(response.getSystemTime()).isBefore(before));
        assertEquals("op-42", response.getOperationUid());
        assertEquals("42", response.getUid());
        assertEquals(Codes.SUCCESS, response.getCode());
        assertTrue(output.getAll().contains("response.systemTime: 1999-01-01"));
    }

    @Test
    void dateFormatterIsIndependentAndUTC() {
        var first = DateTimeUtil.getCustomFormat();
        var second = DateTimeUtil.getCustomFormat();
        assertNotSame(first, second);
        assertEquals("UTC", first.getTimeZone().getID());
        assertEquals("1970-01-01T00:00:00.000Z", first.format(new Date(0)));
    }

    @ParameterizedTest
    @EnumSource(Systems.class)
    void systemEnumHasDescriptionAndRequestToString(Systems system) throws Exception {
        String json = "{\"systemName\":\"" + system.name() + "\"}";
        Request request = mapper.readValue(json, Request.class);
        assertSame(system, request.getSystemName());
        assertFalse(system.getDescription().isBlank());
        for (String field : new String[]{"uid", "operationUid", "systemName", "systemTime",
                "source", "communicationId", "templateId", "productCode", "smsCode"}) {
            assertTrue(request.toString().contains(field + "="));
        }
        assertTrue(request.toString().contains("systemName=" + system.name()));
    }

    @Test
    void failureInModifierReturns500(CapturedOutput output) throws Exception {
        ModifyResponseService broken = response -> { throw new IllegalStateException("Сбой изменения"); };
        MockMvcBuilders.standaloneSetup(new MyController(validation, broken)).build()
                .perform(post("/feedback").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uid\":\"42\",\"operationUid\":\"op-42\","
                                + "\"systemTime\":\"1999-01-01\",\"communicationId\":1}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("UnknownException"))
                .andExpect(jsonPath("$.errorMessage").value("Произошла непредвиденная ошибка"));
        assertTrue(output.getAll().contains("ERROR"));
        assertTrue(output.getAll().contains("Сбой изменения"));
    }

    @Test
    void uuidImplementationWorksInController() throws Exception {
        var result = MockMvcBuilders.standaloneSetup(new MyController(validation, uidModifier)).build()
                .perform(post("/feedback").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uid\":\"42\",\"operationUid\":\"op-42\","
                                + "\"systemTime\":\"1999-01-01\",\"communicationId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("success")).andReturn();
        String uuid = mapper.readTree(result.getResponse().getContentAsByteArray())
                .get("operationUid").asText();
        assertEquals(4, UUID.fromString(uuid).version());
    }
}
