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
import org.springframework.web.multipart.MultipartFile;

import myapp.backend.domain.board.service.BoardService;
import myapp.backend.domain.board.vo.BoardVO;
import myapp.backend.domain.auth.vo.UserPrincipal;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("api/board")
public class BoardController {
    @Autowired
    private BoardService boardService;
    
    @GetMapping("board")
    public List<BoardVO> getBoardList() {
        return boardService.getBoardList();
    }
    
    // 게시물 작성 (이미지 첨부 포함) - 인증된 사용자만
    @PostMapping("board/insert")
    public ResponseEntity<?> createBoard(
            @RequestParam("content") String content,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        // 인증된 사용자만 게시글 작성 가능
        if (principal == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        
        try {
            int userId = principal.getUserId();
            boardService.createBoardWithImages(content, images, userId);
            return ResponseEntity.ok().body("게시글이 성공적으로 작성되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("게시글 작성에 실패했습니다: " + e.getMessage());
        }
    }
    
    // 게시물 작성 (제목 + 내용 + 이미지 첨부) - 인증된 사용자만
    @PostMapping("board/insert-with-title")
    public ResponseEntity<?> createBoardWithTitle(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        // 인증된 사용자만 게시글 작성 가능
        if (principal == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        
        try {
            int userId = principal.getUserId();
            // [제목] 내용 형식으로 합치기
            String combinedContent = "[" + title + "] " + content;
            boardService.createBoardWithImages(combinedContent, images, userId);
            return ResponseEntity.ok().body("게시글이 성공적으로 작성되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("게시글 작성에 실패했습니다: " + e.getMessage());
        }
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
    
    // 이미지 조회 API (인증 없이 접근 가능)
    @GetMapping("/image/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            // 이미지 파일 경로 설정 (절대 경로로 수정)
            String uploadDir = "backend/src/main/resources/static/upload/";
            Path filePath = Paths.get(uploadDir + filename);
            Resource resource = new FileSystemResource(filePath.toFile());
            
            if (resource.exists() && resource.isReadable()) {
                // 이미지 타입 설정
                String contentType = determineContentType(filename);
                
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 파일 확장자에 따른 Content-Type 결정
    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }
}
