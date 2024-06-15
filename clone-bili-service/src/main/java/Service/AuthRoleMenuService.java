package Service;

import Dao.AuthRoleMenuDao;
import domain.Auth.AuthRoleMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AuthRoleMenuService {

    @Autowired
    private AuthRoleMenuDao authRoleMenuDao;
    public List<AuthRoleMenu> authRoleMenuByRoleIds(Set<Long> roleIdSet) {
        return authRoleMenuDao.authRoleMenuByRoleIds(roleIdSet);
    }
}
