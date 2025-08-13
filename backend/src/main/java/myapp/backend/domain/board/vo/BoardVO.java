package myapp.backend.domain.board.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardVO {
    private int board_id;
    private String board_content;
    private LocalDateTime uploaded_at;
    private int view;

    //join
    private int user_id; // user 테이블 
    private int image_id; // image 테이블 
    private String username; // 작성자 이름 (user 테이블)
}
