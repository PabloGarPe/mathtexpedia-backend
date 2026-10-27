package mathtexpedia.es.api.infrastructure.application;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    /**
     * Propiedades que son una referencia a otro DTO y pueden ser nulas (esquema -> propiedades). Con {@code @Schema(nullable = true)}
     * springdoc genera {@code {"type": "null", "$ref": ...}}, y los generadores de tipos (openapi-typescript) ignoran lo que
     * acompaña a un {@code $ref}, así que el cliente las ve como no nulas. Aquí se reescriben como {@code oneOf: [$ref, null]}.
     * Una propiedad de este tipo que se añada a otro DTO tiene que listarse aquí.
     */
    private static final Map<String, Set<String>> NULLABLE_REFERENCES = Map.of(
            "CommentDto", Set.of("author")
    );

    @Bean
    public OpenAPI mathtexpediaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mathtexpedia API")
                        .description("Documentación de los endpoints del backend de la Mathtexpedia")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    public OpenApiCustomizer nullableReferencesCustomizer() {
        return openApi -> {
            if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null)
                return;

            Map<String, Schema> schemas = openApi.getComponents().getSchemas();
            NULLABLE_REFERENCES.forEach((schemaName, propertyNames) -> {
                Schema<?> schema = schemas.get(schemaName);
                if (schema == null || schema.getProperties() == null)
                    return;

                for (String propertyName : propertyNames) {
                    Schema property = schema.getProperties().get(propertyName);
                    if (property != null && property.get$ref() != null)
                        schema.getProperties().put(propertyName, nullableReference(property));
                }
            });
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Schema<?> nullableReference(Schema<?> reference) {
        Schema<?> wrapper = new Schema<>();
        wrapper.setDescription(reference.getDescription());
        wrapper.setOneOf(List.of(new Schema<>().$ref(reference.get$ref()), new Schema<>().types(Set.of("null"))));
        return wrapper;
    }

}
