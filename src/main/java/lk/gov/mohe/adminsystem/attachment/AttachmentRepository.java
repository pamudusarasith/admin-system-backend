package lk.gov.mohe.adminsystem.attachment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {
    long countByParentIdAndParentType(Integer parentId, ParentTypeEnum parentType);
}