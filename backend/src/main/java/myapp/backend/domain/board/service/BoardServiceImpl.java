package myapp.backend.domain.board.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import myapp.backend.domain.board.mapper.BoardMapper;
import myapp.backend.domain.board.vo.BoardVO;
import myapp.backend.domain.board.vo.ImageVO;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


@Service
public class BoardServiceImpl implements BoardService {
    @Autowired
    private BoardMapper boardMapper;
    
    @Override
    public List<BoardVO> getBoardList() {
        return boardMapper.getBoardList();
    }
    
    @Override
    public void createBoard(BoardVO board) {
        boardMapper.insertBoard(board); // 게시물 작성
    }
    
    @Override
    public void createBoardWithImages(String content, MultipartFile[] images, int userId) {
        try {
            // 1. 게시글 먼저 저장
            BoardVO board = new BoardVO();
            board.setBoard_content(content);
            board.setUser_id(userId);
            boardMapper.insertBoard(board);
            
            // 2. board_id가 설정되었는지 확인
            if (board.getBoard_id() == 0) {
                // board_id가 설정되지 않았다면 최근 생성된 게시글 조회
                board = boardMapper.getLatestBoardByUserId(userId);
            }
            
            // 3. 이미지가 있으면 처리 (여러 이미지를 쉼표로 구분해서 저장)
            if (images != null && images.length > 0) {
                StringBuilder allImageUrls = new StringBuilder();
                int firstImageId = 0;
                
                for (int i = 0; i < images.length; i++) {
                    MultipartFile image = images[i];
                    if (!image.isEmpty()) {
                        // 이미지 저장
                        ImageSaveResult result = saveImageAndConnectToBoard(image, board.getBoard_id(), i);
                        
                        // 모든 이미지 URL을 쉼표로 구분해서 저장
                        if (i > 0) {
                            allImageUrls.append(",");
                        }
                        allImageUrls.append(result.getImageUrl());
                        
                        // 첫 번째 이미지 ID 저장
                        if (i == 0) {
                            firstImageId = result.getImageId();
                        }
                    }
                }
                
                // 모든 이미지 URL을 쉼표로 구분해서 하나의 레코드로 저장
                if (allImageUrls.length() > 0) {
                    ImageVO combinedImageVO = new ImageVO();
                    combinedImageVO.setImage_id(0);
                    combinedImageVO.setImage_url(allImageUrls.toString());
                    
                    int combinedImageId = boardMapper.insertImage(combinedImageVO);
                    
                    // board.image_id를 결합된 이미지 ID로 설정
                    boardMapper.updateBoardImageId(board.getBoard_id(), combinedImageId);
                    
                    System.out.println("여러 이미지 결합 저장: " + allImageUrls.toString());
                    System.out.println("결합된 이미지 ID: " + combinedImageId);
                }
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "게시글 작성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    private ImageSaveResult saveImageAndConnectToBoard(MultipartFile image, int boardId, int order) throws IOException {
        // 이미지 저장 경로 설정 (절대 경로로 수정)
        String uploadDir = "backend/src/main/resources/static/upload/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // 고유한 파일명 생성
        String originalFilename = image.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String storedFilename = UUID.randomUUID().toString() + fileExtension;
        
        // 파일 저장
        Path filePath = Paths.get(uploadDir + storedFilename);
        Files.copy(image.getInputStream(), filePath);
        
        // images 테이블에 저장
        String imageUrl = "/upload/" + storedFilename;
        ImageVO imageVO = new ImageVO();
        imageVO.setImage_id(0);  // 초기값 설정
        imageVO.setImage_url(imageUrl);
        
        System.out.println("=== 이미지 저장 디버깅 ===");
        System.out.println("ImageVO 생성: image_id=" + imageVO.getImage_id() + ", image_url=" + imageVO.getImage_url());
        
        int imageId = boardMapper.insertImage(imageVO);
        
        System.out.println("insertImage 호출 후 ImageVO: image_id=" + imageVO.getImage_id());
        System.out.println("insertImage 반환값: " + imageId);
        System.out.println("=== 디버깅 끝 ===");
        
        // MyBatis keyProperty로 설정된 실제 이미지 ID 사용
        int actualImageId = imageVO.getImage_id();
        
        System.out.println("이미지 저장됨: " + storedFilename + " (게시글 ID: " + boardId + ", 순서: " + order + ")");
        System.out.println("저장 경로: " + filePath.toAbsolutePath());
        System.out.println("이미지 URL: " + imageUrl);
        System.out.println("MyBatis 반환값: " + imageId);
        System.out.println("실제 이미지 ID: " + actualImageId);
        
        return new ImageSaveResult(actualImageId, imageUrl);
    }
    
    // 이미지 저장 결과를 담는 내부 클래스
    private static class ImageSaveResult {
        private final int imageId;
        private final String imageUrl;
        
        public ImageSaveResult(int imageId, String imageUrl) {
            this.imageId = imageId;
            this.imageUrl = imageUrl;
        }
        
        public int getImageId() { return imageId; }
        public String getImageUrl() { return imageUrl; }
    }
    
    @Override
    public BoardVO getBoard(int board_id) {
        // 조회수 증가
        increaseViewCount(board_id);
        // 게시글 조회
        return boardMapper.getBoardDetailById(board_id); // 개별 글 조회
    }
    
    @Override
    public void increaseViewCount(int board_id) {
        boardMapper.updateViewCount(board_id);
    }
    
    @Override
    public BoardVO getBoardDetail(int board_id, Integer currentUserId) {
        // 조회수 증가
        increaseViewCount(board_id);
        
        // 상세 정보 조회
        BoardVO boardDetail = boardMapper.getBoardDetailById(board_id);
        
        return boardDetail;
    }

    @Override
    public void deleteBoard(int boardId, int requestUserId) {
    Integer authorUserId = boardMapper.findAuthorUserId(boardId);
    if (authorUserId == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
    }
    if (!authorUserId.equals(requestUserId)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 삭제할 수 있습니다.");
    }
    int deleted = boardMapper.deleteBoard(boardId);
    if (deleted == 0) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "삭제 대상이 존재하지 않습니다.");
    }
}

    @Override
    public void updateBoard(int boardId, BoardVO updatedBoard, int requestUserId) {
        Integer authorUserId = boardMapper.findAuthorUserId(boardId);
        if (authorUserId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
        if (!authorUserId.equals(requestUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 수정할 수 있습니다.");
        }

        // 업데이트 대상 보드 ID를 확실히 설정
        updatedBoard.setBoard_id(boardId);
        int updated = boardMapper.updateBoard(updatedBoard);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "수정 대상이 존재하지 않습니다.");
        }
    }
}
