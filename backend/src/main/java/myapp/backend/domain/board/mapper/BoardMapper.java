package myapp.backend.domain.board.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import myapp.backend.domain.board.vo.BoardVO;

@Mapper
public interface BoardMapper {
    List<BoardVO> getBoardList();
    void insertBoard(BoardVO board); // 게시물 작성
    void updateViewCount(int board_id); // 조회수 증가용
    BoardVO getBoardDetailById(int board_id); // 개별 글 조회
    Integer findAuthorUserId(int board_id); // 작성자 조회
    int deleteBoard(int board_id); // 게시물 삭제
    int updateBoard(BoardVO board); // 게시물 수정
}
