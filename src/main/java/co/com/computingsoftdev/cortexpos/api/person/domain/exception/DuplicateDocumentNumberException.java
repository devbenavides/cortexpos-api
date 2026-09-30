package co.com.computingsoftdev.cortexpos.api.person.domain.exception;

public class DuplicateDocumentNumberException extends RuntimeException{
    public DuplicateDocumentNumberException(String msm){
        super(msm);
    }
}
