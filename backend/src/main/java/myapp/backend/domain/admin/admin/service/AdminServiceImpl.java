package myapp.backend.domain.admin.admin.service;

import myapp.backend.domain.admin.admin.mapper.AdminMapper;
import myapp.backend.domain.admin.admin.vo.AdminVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public AdminVO getAdminInfo(int user_id) {
        return adminMapper.getAdminInfo(user_id);
    }

    @Override
    public boolean isAdmin(int user_id) {
        return adminMapper.isAdmin(user_id);
    }
}


