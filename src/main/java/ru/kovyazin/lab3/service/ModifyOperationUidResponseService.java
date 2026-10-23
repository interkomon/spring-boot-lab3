package ru.kovyazin.lab3.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.kovyazin.lab3.model.Response;
import java.util.UUID;

@Slf4j
@Service
@Qualifier("ModifyOperationUidResponseService")
public class ModifyOperationUidResponseService implements ModifyResponseService {
    @Override
    public Response modify(Response response) {
        String previous = response.getOperationUid();
        response.setOperationUid(UUID.randomUUID().toString());
        log.info("uid={} response.operationUid: {} -> {}", response.getUid(),
                previous, response.getOperationUid());
        return response;
    }
}
