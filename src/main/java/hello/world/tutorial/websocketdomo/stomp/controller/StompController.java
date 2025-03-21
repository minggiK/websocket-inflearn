package hello.world.tutorial.websocketdomo.stomp.controller;

import hello.world.tutorial.websocketdomo.stomp.dto.RequestDto;
import hello.world.tutorial.websocketdomo.stomp.dto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;

@Controller
@Slf4j
public class StompController {

//    @GetMapping("/aaa")
    @MessageMapping("/hello") //수신 request  -> "/app/hello"  -> ResponseDto 로직 실행
    @SendTo("/topic/hello") // response  -> return 받음
    public ResponseDto basic(RequestDto reqtDto) { //수신하고
        log.info("reqDto: {}", reqtDto); //찍어주고

        return new ResponseDto(reqtDto.getMessage(), LocalDateTime.now()); //응답함
    }
}
