package myapp.backend.domain.auth.mapper;

import myapp.backend.domain.auth.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    UserVO findBySnsIdAndSnsType(@Param("sns_id") String sns_id, @Param("sns_type") String sns_type);

    void save(UserVO user);

    void updateUser(UserVO user);

    UserVO findByUserId(@Param("user_id") int user_id);

    // DB에서 직접 user_id 조회
    Integer getUserIdBySnsInfo(@Param("sns_id") String sns_id, @Param("sns_type") String sns_type);
} 