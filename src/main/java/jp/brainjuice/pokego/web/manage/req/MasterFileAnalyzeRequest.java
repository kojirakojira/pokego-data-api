package jp.brainjuice.pokego.web.manage.req;

import jakarta.validation.constraints.NotEmpty;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MasterFileAnalyzeRequest {

	@NotEmpty
	private String userId;
	@NotEmpty
	private MultipartFile masterFile;
}
