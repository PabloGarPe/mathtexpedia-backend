package mathtexpedia.es.api.presentation;

import mathtexpedia.es.api.domain.exception.*;
import mathtexpedia.es.api.domain.security.UserProfile;
import mathtexpedia.es.api.domain.security.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;

import java.text.SimpleDateFormat;

public class GenericController {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(java.util.Date.class,
                new CustomDateEditor(new SimpleDateFormat("dd/MM/yyyy"), true));
    }

    protected void logErrorAndHttpResponse(Exception e, String httpCode, Level level) {

        String message = "Converting " + e.getClass().getName() + " into " + httpCode + " HTTP Code";

        switch (level) {
            case DEBUG:
                logger.debug(message, e);
                break;

            case ERROR:
                logger.error(message, e);
                break;

            case INFO:
                logger.info(message, e);
                break;

            case TRACE:
                logger.trace(message, e);
                break;

            case WARN:
                logger.warn(message, e);
                break;

            default:
                logger.error(message, e);
                break;
        }
    }

    public void checkIfAdmin(UserProfile user) throws MathtexpediaUnauthorizedException {
        if (!user.getRole().equals(UserRole.ADMIN))
            throw new MathtexpediaUnauthorizedException("Not an administrator");
    }

    @ExceptionHandler(PortBadRequestException.class)
    protected ResponseEntity<Object> handlePortBadRequestException(PortBadRequestException e) {
        logErrorAndHttpResponse(e, "400", Level.ERROR);

        return new ResponseEntity<>(new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PortConflictException.class)
    protected ResponseEntity<Object> handlePortConflictException(PortConflictException e) {
        logErrorAndHttpResponse(e, "409", Level.ERROR);

        return new ResponseEntity<>(new HttpHeaders(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PortActionNotPerformedException.class)
    protected ResponseEntity<Object> handlePortActionNotPerformedException(PortActionNotPerformedException e) {
        logErrorAndHttpResponse(e, "500", Level.ERROR);

        return new ResponseEntity<>(new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MathtexpediaUnauthorizedException.class)
    protected ResponseEntity<Object> handleMathtexpediaUnauthorizedException(MathtexpediaUnauthorizedException e) {
        logErrorAndHttpResponse(e, "401", Level.ERROR);

        return new ResponseEntity<>(new HttpHeaders(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MathtexpediaNotFoundException.class)
    protected ResponseEntity<Object> handleMathtexpediaNotFoundException(MathtexpediaNotFoundException e) {
        logErrorAndHttpResponse(e, "404", Level.ERROR);

        return new ResponseEntity<>(new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MathtexpediaInvalidException.class)
    protected ResponseEntity<Object> handleMathtexpediaInvalidException(MathtexpediaInvalidException e) {
        logErrorAndHttpResponse(e, "400", Level.ERROR);

        return new ResponseEntity<>(new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        logErrorAndHttpResponse(e, "400", Level.ERROR);

        return new ResponseEntity<>(new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleException(Exception e) {
        logErrorAndHttpResponse(e, "500", Level.ERROR);

        return new ResponseEntity<>(new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
