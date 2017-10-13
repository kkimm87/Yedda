package com.apps.user.dao;

import javax.servlet.http.HttpSession;

import com.apps.common.DTO;
import com.apps.common.WorkDiv;
import com.apps.user.domain.UserVO;

/**
 * @since 2017-09-22
 * @author 임채현
 * @version 1.0
 * UserDao extends WorkDiv
 */

public interface UserDao extends WorkDiv {

	public DTO do_selectOne(DTO dto);
	public int do_check_id(String id);
	public int do_check_passwd(DTO dto);
	public DTO do_login(DTO dto);
	public void do_logout(HttpSession session);

	
}
