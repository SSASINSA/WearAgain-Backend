package com.ssasinsa.wearagain.global.docs.config;

import com.ssasinsa.wearagain.global.docs.annotation.ApiDoc;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
public class ApiDocCustomizer implements OperationCustomizer {

    private static final String MEDIA_TYPE_JSON = "application/json";

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        // findMergedAnnotation will resolve meta-annotations such as @AdminAuthApiDocs.Login
        ApiDoc apiDoc = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), ApiDoc.class);
        if (apiDoc == null) {
            return operation;
        }

        operation.setSummary(apiDoc.summary());
        operation.setDescription(apiDoc.description());

        ApiResponses responses = operation.getResponses();
        if (responses == null) {
            responses = new ApiResponses();
        }

        ApiResponse response = new ApiResponse().description(apiDoc.summary());
        Content content = buildContent(apiDoc);
        if (content != null) {
            response.content(content);
        }
        responses.addApiResponse(apiDoc.successStatus(), response);

        String[] errorResponses = apiDoc.errorResponses();
        if (errorResponses.length > 0) {
            for (String err : errorResponses) {
                int delimiter = err.indexOf(':');
                if (delimiter <= 0 || delimiter >= err.length() - 1) {
                    continue;
                }
                String status = err.substring(0, delimiter);
                String example = err.substring(delimiter + 1);
                ApiResponse errorResponse = new ApiResponse().description("Error " + status);
                Content errorContent = new Content();
                MediaType mediaType = new MediaType();
                mediaType.addExamples("example", new Example().value(example));
                errorContent.addMediaType(MEDIA_TYPE_JSON, mediaType);
                errorResponse.setContent(errorContent);
                responses.addApiResponse(status, errorResponse);
            }
        }

        operation.setResponses(responses);
        return operation;
    }

    private Content buildContent(ApiDoc apiDoc) {
        boolean hasContent = false;
        MediaType mediaType = new MediaType();

        if (!apiDoc.responseExample().isEmpty()) {
            mediaType.addExamples("example", new Example().value(apiDoc.responseExample()));
            hasContent = true;
        }

        if (apiDoc.responseSchema() != Void.class) {
            Schema<?> schema = new Schema<>();
            schema.set$ref("#/components/schemas/" + apiDoc.responseSchema().getSimpleName());
            mediaType.schema(schema);
            hasContent = true;
        }

        if (!hasContent) {
            return null;
        }

        Content content = new Content();
        content.addMediaType(MEDIA_TYPE_JSON, mediaType);
        return content;
    }
}
