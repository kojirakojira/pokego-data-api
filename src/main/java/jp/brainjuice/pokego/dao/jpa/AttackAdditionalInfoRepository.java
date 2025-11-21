package jp.brainjuice.pokego.dao.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Meta;

import jp.brainjuice.pokego.business.constant.AttackAnnotationTypeEnum;
import jp.brainjuice.pokego.dao.jpa.entity.AttackAdditionalInfo;
import jp.brainjuice.pokego.dao.jpa.entity.AttackAdditionalInfoPk;

public interface AttackAdditionalInfoRepository extends JpaRepository<AttackAdditionalInfo, AttackAdditionalInfoPk> {

	@Meta(comment = "find by moveId and annotationType")
	List<AttackAdditionalInfo> findByMoveIdAndAnnotationType(String moveId, AttackAnnotationTypeEnum annotationType);
}
