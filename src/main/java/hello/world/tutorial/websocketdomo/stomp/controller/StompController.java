package hello.world.tutorial.websocketdomo.stomp.controller;

import hello.world.tutorial.websocketdomo.listener.StompEventListener;
import hello.world.tutorial.websocketdomo.stomp.dto.RequestDto;
import hello.world.tutorial.websocketdomo.stomp.dto.ResSessionDto;
import hello.world.tutorial.websocketdomo.stomp.dto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.Set;

@Controller
@Slf4j
public class StompController {

    private final StompEventListener eventListener;

    public StompController(StompEventListener eventListener) {
        this.eventListener = eventListener;
    }

    //    @GetMapping("/aaa")
    @MessageMapping("/hello") //수신 request  -> "/app/hello"  -> ResponseDto 로직 실행
    @SendTo("/topic/hello") // response  -> return 받음
    public ResponseDto basic(RequestDto reqDto, Message<RequestDto> message, MessageHeaders headers) {
        //log 찍기
        log.info("reqDto: {}", reqDto); //reqDto : body의 정보만 담음
        log.info("message: {}", message); //message :  body, head 등 모든 정보 담아서 수신
        log.info("headers: {}", headers); //headers: 헤더만 수신

        return new ResponseDto(reqDto.getMessage(), LocalDateTime.now()); //응답함
    }

    @MessageMapping({"/hello/{detail}"}) //수신 request  -> "/app/hello"  -> ResponseDto 로직 실행
    @SendTo({"/topic/hello", "/topic/hello2"}) // response  -> return 받음
    public ResponseDto detail(RequestDto reqDto, @DestinationVariable("detail") String detail) { //PathVariable
        //log 찍기
        log.info("reqDto: {}", reqDto);
        log.info("message: {}", detail);

        return new ResponseDto(reqDto.getMessage(), LocalDateTime.now()); //응답함
    }


    @MessageMapping({"/sessions"}) //수신 request  -> "/app/sessions"  -> 요청을 보낸 세션Id(user) 를 알아야해
    @SendToUser("/queue/sessions") //"user/queue/sessions" response  -> return 받음 // 특정 유저한테만 정보를 전달할 때, 사용 (queue : 세션기반 1데1통신 할때 사용하는 예약어 비슷?)
    //구독이 같이도 실제로 요청을 보낸 유저한테만 응답을 함
    public ResSessionDto sessions(RequestDto reqDto, MessageHeaders headers) { //PathVariable
        //log 찍기
        log.info("reqDto: {}", reqDto);
        String sessionId = headers.get("sessionId").toString();
        log.info("sessionId: {}", sessionId);

        //연결된 회원들의 정보를 가져옴
        Set<String> sessions = eventListener.getSessions();
        return new ResSessionDto(sessions.size(), sessions.stream().toList(), sessionId, LocalDateTime.now()); //응답함
    }
}
