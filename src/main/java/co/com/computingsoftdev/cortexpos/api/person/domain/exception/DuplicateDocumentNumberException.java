package co.com.computingsoftdev.cortexpos.api.person.domain.exception;

import co.com.computingsoftdev.cortexpos.api.shared.domain.exception.ConflictException;
import co.com.computingsoftdev.cortexpos.api.shared.domain.exception.DomainException;

public class DuplicateDocumentNumberException extends ConflictException {
    public DuplicateDocumentNumberException(String documentNumber){
        super("DUPLICATE_DOCUMENT_NUMBER",
                "Ya existe una persona con el documento " + documentNumber);
    }
}
