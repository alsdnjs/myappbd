package myapp.backend.domain.board.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import myapp.backend.domain.board.service.BoardService;
import myapp.backend.domain.board.vo.BoardVO;
import myapp.backend.domain.auth.vo.UserPrincipal;

@RestController
@RequestMapping("api/board")
public class BoardController {
    @Autowired
    private BoardService boardService;
    
    @GetMapping("board")
    public List<BoardVO> getBoardList() {
        return boardService.getBoardList();
    }
    
    // 게시물 작성
    @PostMapping("board/insert")
    public void createBoard(@RequestBody BoardVO board) {
        boardService.createBoard(board);
    }
    
    // 개별 글 조회
    @GetMapping("board/{board_id}")
    public BoardVO getBoard(@PathVariable int board_id) {
        return boardService.getBoard(board_id);
    }
    
    // 조회수 증가 (별도 API)
    @PostMapping("board/{board_id}/view")
    public void increaseViewCount(@PathVariable int board_id) {
        boardService.increaseViewCount(board_id);
    }

    // 게시물 상세페이지 (디테일)
    @GetMapping("board/detail/{board_id}")
    public BoardVO getBoardDetail(
            @PathVariable("board_id") int board_id,
            @RequestParam(required = false) Integer currentUserId) {
        return boardService.getBoardDetail(board_id, currentUserId);
    }

    // 게시물 삭제 (작성자만)
    @DeleteMapping("/board/delete/{board_id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable("board_id") int board_id,
                                            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        boardService.deleteBoard(board_id, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    // 게시물 수정 (작성자만)
    @PutMapping("/update/{board_id}")
    public ResponseEntity<Void> updateBoard(@PathVariable("board_id") int board_id,
                                            @RequestBody BoardVO updatedBoard,
                                            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            System.out.println("AuthenticationPrincipal is null -> 401 반환");
            return ResponseEntity.status(401).build();
        }
        // PathVariable을 우선시하여 보안 강화
        updatedBoard.setBoard_id(board_id);
        boardService.updateBoard(board_id, updatedBoard, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
