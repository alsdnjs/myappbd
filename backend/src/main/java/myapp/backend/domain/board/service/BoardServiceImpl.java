package myapp.backend.domain.board.service;

import java.util.List;
import java.util.ArrayList;

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
        List<BoardVO> boardList = boardMapper.getBoardList();
        
        // 각 게시글에 대해 imageUrls 설정
        for (BoardVO board : boardList) {
            if (board.getImage_url() != null && board.getImage_url().contains(",")) {
                String[] imageUrlArray = board.getImage_url().split(",");
                List<String> imageUrls = new ArrayList<>();
                for (String url : imageUrlArray) {
                    imageUrls.add(url.trim());
                }
                board.setImageUrls(imageUrls);
            } else if (board.getImage_url() != null) {
                // 단일 이미지인 경우
                List<String> imageUrls = new ArrayList<>();
                imageUrls.add(board.getImage_url());
                board.setImageUrls(imageUrls);
            }
        }
        
        return boardList;
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
                    }
                }
                
                // 4. 모든 이미지 URL을 하나의 ImageVO에 저장
                ImageVO combinedImageVO = new ImageVO();
                combinedImageVO.setImage_url(allImageUrls.toString());
                combinedImageVO.setImage_id(0); // MyBatis가 자동으로 설정
                
                boardMapper.insertImage(combinedImageVO);
                
                // 5. 게시글의 image_id 업데이트
                boardMapper.updateBoardImageId(board.getBoard_id(), combinedImageVO.getImage_id());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("게시글 작성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    // 이미지 저장 및 게시글 연결
    private ImageSaveResult saveImageAndConnectToBoard(MultipartFile image, int boardId, int order) throws IOException {
        // 업로드 디렉토리 설정
        String uploadDir = System.getProperty("user.dir") + "/backend/src/main/resources/static/upload/";
        
        // 디렉토리가 없으면 생성
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }
        
        // 파일명 생성 (UUID + 원본 확장자)
        String originalFilename = image.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storedFilename = UUID.randomUUID().toString() + extension;
        
        // 파일 저장
        Path filePath = Paths.get(uploadDir, storedFilename);
        Files.copy(image.getInputStream(), filePath);
        
        // 이미지 URL 생성
        String imageUrl = "/upload/" + storedFilename;
        
        // ImageVO 생성 (실제로는 사용하지 않음, 로깅용)
        ImageVO imageVO = new ImageVO();
        imageVO.setImage_url(imageUrl);
        imageVO.setImage_id(0);
        
        // MyBatis keyProperty로 설정된 실제 이미지 ID 사용
        int actualImageId = imageVO.getImage_id();
        
        System.out.println("이미지 저장됨: " + storedFilename + " (게시글 ID: " + boardId + ", 순서: " + order + ")");
        System.out.println("저장 경로: " + filePath.toAbsolutePath());
        System.out.println("이미지 URL: " + imageUrl);
        System.out.println("MyBatis 반환값: " + actualImageId);
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
        
        // 여러 이미지 URL을 배열로 설정
        if (boardDetail != null && boardDetail.getImage_url() != null && boardDetail.getImage_url().contains(",")) {
            String[] imageUrlArray = boardDetail.getImage_url().split(",");
            List<String> imageUrls = new ArrayList<>();
            for (String url : imageUrlArray) {
                imageUrls.add(url.trim());
            }
            boardDetail.setImageUrls(imageUrls);
        } else if (boardDetail != null && boardDetail.getImage_url() != null) {
            // 단일 이미지인 경우
            List<String> imageUrls = new ArrayList<>();
            imageUrls.add(boardDetail.getImage_url());
            boardDetail.setImageUrls(imageUrls);
        }
        
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
