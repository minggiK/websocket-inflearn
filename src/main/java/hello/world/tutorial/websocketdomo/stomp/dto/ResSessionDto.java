package hello.world.tutorial.websocketdomo.stomp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@AllArgsConstructor
@Data
public class ResSessionDto {

    private int count; // session들의 수
    private List<String> sessions;  //session들을 List로 담아준

    //1:1 통신
    private String sourceSessionId; //요청을 보낸 sessionId
    private LocalDateTime localDateTime;
}
