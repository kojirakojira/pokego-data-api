package jp.brainjuice.pokego.business.service.sub;

import java.util.List;

import org.springframework.stereotype.Service;

import jp.brainjuice.pokego.business.dao.AdminCommentRepository;
import jp.brainjuice.pokego.business.dao.entity.AdminComment;

/**
 * サイト運営者からのお知らせ機能
 */
@Service
public class AdminCommentService {

	private AdminCommentRepository adminCommentRepository;
	
	public AdminCommentService(AdminCommentRepository adminCommentRepository) {
		this.adminCommentRepository = adminCommentRepository;
	}
	
	public List<String> exec() {
		
		List<AdminComment> adminCommentList = adminCommentRepository.findAll();
		
		return adminCommentList.stream()
				.sorted((ac1, ac2) -> ac1.getCommentId() - ac2.getCommentId())
				.map(AdminComment::getComment)
				.toList();
		
	}
}
