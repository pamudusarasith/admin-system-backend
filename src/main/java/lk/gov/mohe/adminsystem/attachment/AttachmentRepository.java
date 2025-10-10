package lk.gov.mohe.adminsystem.attachment;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {
  long countByParentIdAndParentType(Integer parentId, ParentTypeEnum parentType);

  List<Attachment> findByParentTypeAndParentId(ParentTypeEnum parentType, Integer parentId);

  List<Attachment> findByParentTypeAndParentIdIn(
      ParentTypeEnum parentTypeEnum, List<Integer> parentIds);
}
