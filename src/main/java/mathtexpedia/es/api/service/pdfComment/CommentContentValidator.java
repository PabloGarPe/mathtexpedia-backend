package mathtexpedia.es.api.service.pdfComment;

import mathtexpedia.es.api.domain.exception.CommentContentException;
import mathtexpedia.es.api.domain.model.pdfComment.CommentContentError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Reglas de formato del texto de un comentario, aplicadas antes de encolarlo (sin IA): normaliza el texto y rechaza enlaces,
 * imágenes y HTML.
 */
@Component
public class CommentContentValidator {

    private static final Pattern IMAGE = Pattern.compile("!\\[[^\\]]*\\][(\\[]");
    private static final Pattern LINK = Pattern.compile(
            "\\[[^\\]]*\\]\\("
                    + "|(?m)^\\s*\\[[^\\]]+\\]:\\s*\\S+"
                    + "|(?i)\\b(?:https?|ftp)://"
                    + "|(?i)\\bwww\\."
                    + "|(?i)\\b(?:mailto:|javascript:|vbscript:|data:[a-z]+/)");
    private static final Pattern HTML = Pattern.compile(
            "(?i)</?(?:script|iframe|img|svg|style|object|embed|link|form|input|video|audio)\\b(?:\\s[^>]*)?/?>|(?i)<a\\s[^>]*>");
    private static final Pattern INVISIBLE = Pattern.compile("[\\p{Cc}&&[^\\n\\t]]|[\\u200B-\\u200D\\u2060\\uFEFF]");

    private final int maxLength;

    public CommentContentValidator(@Value("${comments.max-length}") int maxLength) {
        this.maxLength = maxLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public String validate(String raw) throws CommentContentException {
        if (raw == null)
            throw new CommentContentException(CommentContentError.EMPTY);

        String text = Normalizer.normalize(raw, Normalizer.Form.NFC).replace("\r\n", "\n").replace('\r', '\n');
        text = INVISIBLE.matcher(text).replaceAll("").strip();

        if (text.isEmpty())
            throw new CommentContentException(CommentContentError.EMPTY);
        if (text.codePointCount(0, text.length()) > maxLength)
            throw new CommentContentException(CommentContentError.TOO_LONG);
        if (IMAGE.matcher(text).find())
            throw new CommentContentException(CommentContentError.IMAGES_NOT_ALLOWED);
        if (LINK.matcher(text).find())
            throw new CommentContentException(CommentContentError.LINKS_NOT_ALLOWED);
        if (HTML.matcher(text).find())
            throw new CommentContentException(CommentContentError.HTML_NOT_ALLOWED);

        return text;
    }
}
