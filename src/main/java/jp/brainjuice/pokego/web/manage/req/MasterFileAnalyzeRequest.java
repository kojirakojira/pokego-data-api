package jp.brainjuice.pokego.web.manage.req;

import java.util.Objects;

import jakarta.validation.constraints.NotEmpty;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Fileを含むため、multipart/form-dataとして受け取る。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MasterFileAnalyzeRequest {

	@NotEmpty
	private String userId;
	@NotEmpty
	private MultipartFile masterFile;
	private boolean isPrintRequestedQuickMoves;
	private boolean isPrintRequestedCinematicMoves;
	private boolean isPrintRequestedMoveEachPokemon;
	private boolean shouldSaveFastAttack;
	private boolean shouldSaveChargedAttack;

	/** multipart/form-dataのためStringで受け取ってしまう。booleanに変換する。 */
	public void setIsPrintRequestedQuickMoves(String isPrintRequestedQuickMoves) {
		this.isPrintRequestedQuickMoves = Objects.equals("true", isPrintRequestedQuickMoves);
	}
	/** multipart/form-dataのためStringで受け取ってしまう。booleanに変換する。 */
	public void setIsPrintRequestedCinematicMoves(String isPrintRequestedCinematicMoves) {
		this.isPrintRequestedCinematicMoves = Objects.equals("true", isPrintRequestedCinematicMoves);
	}
	/** multipart/form-dataのためStringで受け取ってしまう。booleanに変換する。 */
	public void setIsPrintRequestedMoveEachPokemon(String isPrintRequestedMoveEachPokemon) {
		this.isPrintRequestedMoveEachPokemon = Objects.equals("true", isPrintRequestedMoveEachPokemon);
	}
	/** multipart/form-dataのためStringで受け取ってしまう。booleanに変換する。 */
	public void setShouldSaveFastAttackFastAttack(String shouldSaveFastAttack) {
		this.shouldSaveFastAttack = Objects.equals("true", shouldSaveFastAttack);
	}
	/** multipart/form-dataのためStringで受け取ってしまう。booleanに変換する。 */
	public void setShouldSaveChargedAttack(String shouldSaveChargedAttack) {
		this.shouldSaveChargedAttack = Objects.equals("true", shouldSaveChargedAttack);
	}
}
