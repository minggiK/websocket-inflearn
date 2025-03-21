package hello.world.tutorial.websocketdomo.stomp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
public class ResponseDto {
    private String message;
    private LocalDateTime localDateTime;
}
