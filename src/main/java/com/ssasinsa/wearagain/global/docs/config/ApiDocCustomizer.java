package com.ssasinsa.wearagain.global.docs.config;

import com.ssasinsa.wearagain.global.docs.annotation.ApiDoc;
import com.ssasinsa.wearagain.domain.auth.exception.AdminAuthErrorCode;
import com.ssasinsa.wearagain.global.exception.ErrorResponse;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.core.annotation.AnnotatedElementUtils;

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

        responses.addApiResponse("200", response);

        // If this operation belongs to admin auth package, add documented possible admin auth error responses.
        String handlerPackage = handlerMethod.getBeanType().getPackageName();
        if (handlerPackage != null && handlerPackage.contains(".domain.auth")) {
            addAdminErrorResponses(responses);
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

    private void addAdminErrorResponses(ApiResponses responses) {
        for (AdminAuthErrorCode errorCode : AdminAuthErrorCode.values()) {
            responses.addApiResponse(String.valueOf(errorCode.getStatus()), new ApiResponse()
                    .description(errorCode.getMessage())
                    .content(buildErrorContent()));
        }
    }

    private Content buildErrorContent() {
        MediaType mediaType = new MediaType();
        Schema<ErrorResponse> schema = new Schema<>();
        schema.set$ref("#/components/schemas/ErrorResponse");
        mediaType.schema(schema);

        Content content = new Content();
        content.addMediaType(MEDIA_TYPE_JSON, mediaType);
        return content;
    }
}
