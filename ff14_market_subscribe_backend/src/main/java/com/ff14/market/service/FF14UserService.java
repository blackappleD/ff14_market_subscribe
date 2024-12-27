package com.ff14.market.service;

import cn.hutool.crypto.digest.BCrypt;
import com.ff14.market.auth.TokenInfo;
import com.ff14.market.dto.UserRegisterReqDTO;
import com.ff14.market.dto.UserResDTO;
import com.ff14.market.dto.LoginReqDTO;
import com.ff14.market.exception.FF14Exception;
import com.ff14.market.mapper.FF14UserMapper;
import com.ff14.market.po.FF14UserPO;
import com.ff14.market.repo.FF14UserRepo;
import com.ff14.market.util.AuthUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author chentong
 * @version 1.0
 * @description: description
 * @date 2024/12/20 9:27
 */
@Service
public class FF14UserService {

	@Resource
	private FF14UserRepo ff14UserRepo;

	@Resource
	private FF14UserMapper ff14UserMapper;

	public UserResDTO get(String id) {
		return ff14UserMapper.po2dto(findById(id));
	}

	public FF14UserPO findById(String id) {
		Optional<FF14UserPO> byId = ff14UserRepo.findById(id);
		if (byId.isEmpty()) {
			throw FF14Exception.UserException.notFound();
		}
		return byId.get();
	}

	public TokenInfo login(LoginReqDTO dto) {
		Optional<FF14UserPO> userOpt = ff14UserRepo.findByUserAccount(dto.getUserAccount());
		if (userOpt.isEmpty()) {
			userOpt = ff14UserRepo.findByEmail(dto.getUserAccount());
		}

		if (userOpt.isEmpty()) {
			throw FF14Exception.UserException.notFound(dto.getUserAccount());
		}

		FF14UserPO user = userOpt.get();

		if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
			throw FF14Exception.UserException.passwordError();
		}
		return AuthUtil.login(user.getId());
	}

	public void logout() {
		AuthUtil.logout();
	}

	public void insert(UserRegisterReqDTO dto) {

		if (ff14UserRepo.existsByEmail(dto.getEmail())) {
			throw FF14Exception.UserException.emailExists(dto.getEmail());
		}
		FF14UserPO user = ff14UserMapper.dto2po(dto);
		// 加密密码
		user.setPassword(BCrypt.hashpw(dto.getPassword()));
		ff14UserRepo.save(user);

	}

	public void changeEmail(UserRegisterReqDTO dto) {

		FF14UserPO user = findById(AuthUtil.getLoginId());
		if (user.getEmail().equals(dto.getEmail())) {
			return;
		}
		if (ff14UserRepo.existsByEmail(dto.getEmail())) {
			throw FF14Exception.UserException.emailBoundToAnotherUser();
		}
		user.setEmail(dto.getEmail());
		ff14UserRepo.save(user);
	}


}
