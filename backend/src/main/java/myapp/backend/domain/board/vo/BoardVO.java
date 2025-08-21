package myapp.backend.domain.board.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

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
    private String image_url; // 이미지 URL (images 테이블에서 JOIN) - 여러 이미지 (쉼표로 구분)
    private List<String> imageUrls; // 여러 이미지 URL을 배열로 제공
    
    // board_content에서 제목과 내용을 분리하는 메서드
    public String getTitle() {
        if (board_content != null && board_content.startsWith("[") && board_content.contains("]")) {
            int endIndex = board_content.indexOf("]");
            if (endIndex > 1) {
                return board_content.substring(1, endIndex);
            }
        }
        return "";
    }
    
    public String getContent() {
        if (board_content != null && board_content.startsWith("[") && board_content.contains("]")) {
            int endIndex = board_content.indexOf("]");
            if (endIndex > 1) {
                return board_content.substring(endIndex + 1).trim();
            }
        }
        return board_content;
    }
}
