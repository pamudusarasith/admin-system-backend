package lk.gov.mohe.adminsystem.letter;

import lk.gov.mohe.adminsystem.attachment.Attachment;
import lk.gov.mohe.adminsystem.attachment.AttachmentRepository;
import lk.gov.mohe.adminsystem.storage.MinioStorageService;
import lk.gov.mohe.adminsystem.util.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class LetterService {

    private final LetterRepository letterRepository;
    private final AttachmentRepository attachmentRepository;
    private final LetterMapper letterMapper;
    private final MinioStorageService storageService;

    public PaginatedResponse<LetterDetailsMinDto> getLetters(Integer page,
                                                             Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<LetterDetailsMinDto> lettersPage =
            letterRepository.findAll(pageable).map(letterMapper::toLetterDetailsMinDto);

        return new PaginatedResponse<>(lettersPage);
    }

    @Transactional
    public Letter createLetter(CreateOrUpdateLetterRequestDto request,
                               MultipartFile[] attachments) {
        if (letterRepository.existsLetterByReference(request.reference())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Reference already exists");
        }

        Letter letter = letterMapper.toEntity(request);
        letter.setStatus(StatusEnum.NEW);
        Letter savedLetter = letterRepository.save(letter);

        if (attachments != null) {
            for (MultipartFile attachment : attachments) {
                if (attachment.isEmpty()) {
                    throw new IllegalArgumentException("Attachment cannot be empty");
                }
                Attachment newAttachment = new Attachment();
                newAttachment.setFileName(attachment.getOriginalFilename());
                String objectName = storageService.upload("letters", attachment);
                newAttachment.setFilePath(objectName);
                newAttachment.setFileType(attachment.getContentType());
                newAttachment.attachToParent(savedLetter);
                attachmentRepository.save(newAttachment);
            }
        }
        return savedLetter;
    }

    public void updateLetter(Integer id, CreateOrUpdateLetterRequestDto request) {
        Letter letter = letterRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Letter not found with id: "
                + id));

        letterMapper.updateEntityFromCreateOrUpdateLetterRequestDto(request, letter);

        letterRepository.save(letter);
    }
}
