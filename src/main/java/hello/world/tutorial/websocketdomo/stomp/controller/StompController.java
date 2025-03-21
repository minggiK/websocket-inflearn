package hello.world.tutorial.websocketdomo.stomp.controller;

import hello.world.tutorial.websocketdomo.listener.StompEventListener;
import hello.world.tutorial.websocketdomo.stomp.dto.RequestDto;
import hello.world.tutorial.websocketdomo.stomp.dto.ResSessionDto;
import hello.world.tutorial.websocketdomo.stomp.dto.ResponseDto;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Controller
@Slf4j
public class StompController {

    private final StompEventListener eventListener;
    private final SimpMessagingTemplate messagingTemplate;
    private final TaskScheduler taskScheduler;
    private final ConcurrentHashMap<String,  ScheduledFuture<?>> sessionMap = new ConcurrentHashMap<>();

    public StompController(StompEventListener eventListener, SimpMessagingTemplate messagingTemplate, TaskScheduler taskScheduler) {
        this.eventListener = eventListener;
        this.messagingTemplate = messagingTemplate;
        this.taskScheduler = taskScheduler;
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
    //"user/queue/sessions" response  -> return 받음 // 특정 유저한테만 정보를 전달할 때, 사용 (queue : 세션기반 1데1통신 할때 사용하는 예약어 비슷?)
    //구독이 같이도 실제로 요청을 보낸 유저한테만 응답을 함
    @SendToUser("/queue/sessions")
    public ResSessionDto sessions(RequestDto reqDto, MessageHeaders headers) { //PathVariable
        //log 찍기
        log.info("reqDto: {}", reqDto);
        String sessionId = headers.get("sessionId").toString();
        log.info("sessionId: {}", sessionId);

        //연결된 회원들의 정보를 가져옴
        Set<String> sessions = eventListener.getSessions();
        return new ResSessionDto(sessions.size(), sessions.stream().toList(), sessionId, LocalDateTime.now()); //응답함
    }


    //simpMessaging
    @MessageMapping({"/code1"}) //수신 request  -> "/app/code1"  -> ResponseDto 로직 실행
//    @SendTo({"/topic/hello", "/topic/hello2"}) // response  -> return 받음
    public void code1(RequestDto reqDto, Message<RequestDto> message, MessageHeaders headers) { //PathVariable
        //log 찍기
        log.info("reqDto: {}", reqDto);
        log.info("message: {}", message);
        log.info("message: {}", headers);

        ResponseDto resDto = new ResponseDto(reqDto.getMessage(), LocalDateTime.now());
        //@SendTo 대신 코드 추가
        messagingTemplate.convertAndSend("/topic/hello", resDto );

    }

    @MessageMapping({"/code2"}) // "/app/code2"
//    @SendToUser("/queue/sessions")
    public void code2(RequestDto reqDto, MessageHeaders headers) { //PathVariable
        //log 찍기
        log.info("reqDto: {}", reqDto);
        //요청보낸 사용자의 세션Id
        String sessionId = headers.get("sessionId").toString();
        log.info("sessionId: {}", sessionId);

        //연결된 회원들의 정보를 가져옴
        Set<String> sessions = eventListener.getSessions();
        ResSessionDto resSessionDto = new ResSessionDto(sessions.size(), sessions.stream().toList(), sessionId,LocalDateTime.now());
        messagingTemplate.convertAndSendToUser(sessionId, "/queue/sessions", resSessionDto, createHeaders(sessionId)); //마지막 인자 header를 넣어야 수신이 가능

    }

    MessageHeaders createHeaders(@Nullable String sessionId) {
        //인자로 받은 sessionId를 받아서 header로 만들어줌
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }

//특정 유저에게 실시간으로 변경되는 정보를 전달   @Scheduled -> 정적인 기능(start, stop을 따로설정할 수 없다.) : 프로그래밍 방식으로 사용
    @MessageMapping("/start") // "/app/start"
    public void start(RequestDto reqDto, MessageHeaders headers) {

        log.info("headers: {}", headers);
        //header에서 세션Id 가져오기
        String sessionId = headers.get("simpSessionId").toString();
        log.info("headers: {}", sessionId);

        //프로그래밍 방식 : 원하는시점에 스케줄러를 시작할 수 있다. (동적으로 만들어줌)
        ScheduledFuture<?> scheduledFuture = taskScheduler.scheduleAtFixedRate(() -> {  //주기 정보를 알려줘야해
            Random random = new Random();
            int currentPrice = random.nextInt(100);
            messagingTemplate.convertAndSendToUser(sessionId, "/queue/trade", currentPrice, createHeaders(sessionId));
        }, Duration.ofSeconds(3)); //3초에 한번식 정보(앞의내용)를 알려주도록 설정

        //sessionId 에게 scheduledFuture 알려주고있다.
        sessionMap.put(sessionId, scheduledFuture);
//        //10초 뒤에 알림 취소
//        Thread.sleep(10000);
//        scheduledFuture.cancel(true);
    }

    @MessageMapping("/stop") // "/app/start"
    public void stop(RequestDto reqDto, MessageHeaders headers) {

        log.info("headers: {}", headers);
        //header에서 세션Id 가져오기
        String sessionId = headers.get("simpSessionId").toString();
        log.info("sessionId: {}", sessionId);

        //프로그래밍 방식 : 원하는시점에 스케줄러를 시작할 수 있다. (동적으로 만들어줌)


//        //10초 뒤에 알림 취소 -> 형태가 다르다?
//        Thread.sleep(10000);
//        scheduledFuture.cancel(true);
        //알림 정지
        ScheduledFuture<?> remove = sessionMap.remove(sessionId);
        remove.cancel(true);
    }

    @MessageMapping("/exception")  // "/app/exception"
    @SendTo("/topic/hello")
    public void exception(RequestDto reqDto, MessageHeaders headers) throws Exception {
        log.info("request: {}", reqDto);
        String message = reqDto.getMessage();
        switch (message) {
            case "runtime":
                throw new RuntimeException();
            case "nullPointer" :
                throw new NullPointerException();
            case "io":
                throw new IOException();
            case "exception":
                throw new Exception();
            default:
                throw new InvalidParameterException();
        }

    }
}
