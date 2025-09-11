package lk.gov.mohe.adminsystem.attachment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {
    long countByParentIdAndParentType(Integer parentId, ParentTypeEnum parentType);

    List<Attachment> findByParentTypeAndParentId(ParentTypeEnum parentType,
                                                 Integer parentId);
}