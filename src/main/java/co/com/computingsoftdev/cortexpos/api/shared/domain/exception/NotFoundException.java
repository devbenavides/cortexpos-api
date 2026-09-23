package co.com.computingsoftdev.cortexpos.api.shared.domain.exception;

public class NotFoundException extends RuntimeException{
    public NotFoundException(String msm){
        super("The "+msm+" not found");
    }
}
