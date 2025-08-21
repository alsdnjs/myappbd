package myapp.backend.domain.board.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import myapp.backend.domain.board.vo.BoardVO;
import myapp.backend.domain.board.vo.ImageVO;

@Mapper
public interface BoardMapper {
    List<BoardVO> getBoardList();
    void insertBoard(BoardVO board); // 게시물 작성
    void updateViewCount(int board_id); // 조회수 증가용
    BoardVO getBoardDetailById(int board_id); // 개별 글 조회
    Integer findAuthorUserId(int board_id); // 작성자 조회
    int deleteBoard(int board_id); // 게시물 삭제
    int updateBoard(BoardVO board); // 게시물 수정
    
    // 이미지 관련 메서드
    int insertImage(ImageVO imageVO); // 이미지 정보 저장
    void updateBoardImageId(int board_id, int image_id); // 게시글의 image_id 업데이트
    

    
    // 최근 생성된 게시글 조회
    BoardVO getLatestBoardByUserId(int userId);
    

}
