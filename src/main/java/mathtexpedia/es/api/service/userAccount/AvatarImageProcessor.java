package mathtexpedia.es.api.service.userAccount;

import mathtexpedia.es.api.domain.exception.MathtexpediaInvalidException;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * Valida una imagen subida como foto de perfil y la normaliza: recorte cuadrado centrado, reducida a 256 px como
 * máximo y recodificada como JPEG. Al decodificarla de verdad se descarta el contenido que solo finge ser una imagen.
 */
@Component
public class AvatarImageProcessor {

    public static final String OUTPUT_CONTENT_TYPE = "image/jpeg";

    static final int MAX_INPUT_BYTES = 2 * 1024 * 1024;
    private static final int MIN_SIDE = 64;
    // Límite de lado antes de decodificar, para que una imagen diminuta en bytes pero enorme en píxeles no agote la memoria
    private static final int MAX_SIDE = 6000;
    private static final int OUTPUT_SIDE = 256;
    private static final float JPEG_QUALITY = 0.88f;
    private static final Set<String> ACCEPTED_FORMATS = Set.of("jpeg", "png");

    public byte[] normalize(byte[] input) throws MathtexpediaInvalidException {
        if (input == null || input.length == 0)
            throw new MathtexpediaInvalidException("Avatar image is required");
        if (input.length > MAX_INPUT_BYTES)
            throw new MathtexpediaInvalidException("Avatar image must not exceed " + MAX_INPUT_BYTES / (1024 * 1024) + " MB");

        BufferedImage source = decode(input);

        int side = Math.min(source.getWidth(), source.getHeight());
        BufferedImage square = source.getSubimage((source.getWidth() - side) / 2, (source.getHeight() - side) / 2, side, side);

        return encode(scale(square, Math.min(side, OUTPUT_SIDE)));
    }

    private BufferedImage decode(byte[] input) throws MathtexpediaInvalidException {
        try (ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(input))) {
            Iterator<ImageReader> readers = stream == null ? null : ImageIO.getImageReaders(stream);
            if (readers == null || !readers.hasNext())
                throw new MathtexpediaInvalidException("File is not a supported image (JPEG or PNG)");

            ImageReader reader = readers.next();
            try {
                if (!ACCEPTED_FORMATS.contains(reader.getFormatName().toLowerCase()))
                    throw new MathtexpediaInvalidException("Avatar image must be JPEG or PNG");

                reader.setInput(stream, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width < MIN_SIDE || height < MIN_SIDE)
                    throw new MathtexpediaInvalidException("Avatar image must be at least " + MIN_SIDE + "x" + MIN_SIDE + " pixels");
                if (width > MAX_SIDE || height > MAX_SIDE)
                    throw new MathtexpediaInvalidException("Avatar image must not exceed " + MAX_SIDE + " pixels per side");

                return reader.read(0);
            } finally {
                reader.dispose();
            }
        } catch (IOException | RuntimeException e) {
            // Imagen corrupta o con una codificación que el decodificador no soporta (p. ej. JPEG CMYK)
            throw new MathtexpediaInvalidException("Avatar image could not be read");
        }
    }

    /** Escala por mitades mientras la imagen sea más del doble del destino, para no perder calidad con un solo salto grande. */
    private BufferedImage scale(BufferedImage image, int target) {
        BufferedImage current = toRgb(image);
        while (current.getWidth() > target * 2) {
            current = resize(current, current.getWidth() / 2);
        }
        return current.getWidth() == target ? current : resize(current, target);
    }

    private BufferedImage toRgb(BufferedImage image) {
        return resize(image, image.getWidth());
    }

    /** Dibuja sobre fondo blanco (el JPEG no admite transparencia) a un lado de {@code side} píxeles. */
    private BufferedImage resize(BufferedImage image, int side) {
        BufferedImage out = new BufferedImage(side, side, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, side, side);
            g.drawImage(image, 0, 0, side, side, null);
        } finally {
            g.dispose();
        }
        return out;
    }

    private byte[] encode(BufferedImage image) {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             MemoryCacheImageOutputStream out = new MemoryCacheImageOutputStream(bytes)) {
            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(JPEG_QUALITY);

            writer.setOutput(out);
            writer.write(null, new IIOImage(image, null, null), params);
            out.flush();
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Error encoding avatar image", e);
        } finally {
            writer.dispose();
        }
    }
}
