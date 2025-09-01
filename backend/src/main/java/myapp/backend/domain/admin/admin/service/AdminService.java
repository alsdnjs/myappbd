package myapp.backend.domain.admin.admin.service;

import myapp.backend.domain.admin.admin.vo.AdminVO;

public interface AdminService {
    AdminVO getAdminInfo(int user_id);
    boolean isAdmin(int user_id);
}


