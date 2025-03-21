package hello.world.tutorial.websocketdomo.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class StompEventListener {

    //항상 connect 되어있는 sessionId만 가지고 있다.
    private final ConcurrentHashMap<String, String> sessionMap = new ConcurrentHashMap<>();

    //현재 연결된 회원들을 조회
    public Set<String> getSessions() {
        return sessionMap.keySet();
    }

    @EventListener
    public void listener(SessionConnectEvent sessionConnectEvent) {  //실행하면, Connect(요청보냄), Connected(요청 완료) 함께 실행
        log.info("sessionConnectEvent. {}", sessionConnectEvent);

    }

    //connect 된 sessionId 를 들고 있음
    @EventListener
    public void listener(SessionConnectedEvent sessionConnectedEvent) {
        log.info("sessionConnectedEvent. {}", sessionConnectedEvent);
        String sessionId = sessionConnectedEvent.getMessage().getHeaders().get("simpSessionId").toString();
        sessionMap.put(sessionId, sessionId); //connected 되면, sessionId를 HashMap에 추가
    }

    @EventListener
    public void listener(SessionSubscribeEvent sessionSubscribeEvent) { // -> 여기까지 patload byte = 0  // 1대 1통신 : session Id 를 다르게 가져온다

        log.info("sessionSubscribeEvent. {}", sessionSubscribeEvent);
    }

    @EventListener
    public void listener(SessionUnsubscribeEvent sessionUnsubscribeEvent) {
        log.info("sessionUnsubscribeEvent. {}", sessionUnsubscribeEvent);
    }

    //Disconnect 된 sessionId는 삭제
    @EventListener
    public void listener(SessionDisconnectEvent sessionDisconnectEvent) {
        log.info("sessionDisconnectEvent. {}", sessionDisconnectEvent);
        String sessionId = sessionDisconnectEvent.getMessage().getHeaders().get("simpSessionId").toString();
        sessionMap.remove(sessionId);
    }
}