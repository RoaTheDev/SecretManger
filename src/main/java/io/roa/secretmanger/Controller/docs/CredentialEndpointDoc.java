package io.roa.secretmanger.Controller.docs;

import io.roa.secretmanger.DTO.request.ApprovalRequest.AccessRequestedResponse;
import io.roa.secretmanger.DTO.request.ApprovalRequest.CreateCredentialRequest;
import io.roa.secretmanger.DTO.response.ApiRes;
import io.roa.secretmanger.DTO.response.PageResponse;
import io.roa.secretmanger.DTO.response.Shamir.CredentialCreatedResponse;
import io.roa.secretmanger.DTO.response.Shamir.CredentialDetail;
import io.roa.secretmanger.DTO.response.Shamir.CredentialRevealResponse;
import io.roa.secretmanger.DTO.response.Shamir.CredentialSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Tag(name = "Credentials", description = "Credential vault — create, list, request access, and reveal secrets")
@SecurityRequirement(name = "bearerAuth")
public interface CredentialEndpointDoc {

    @Operation(summary = "Create a credential",
            description = "Stores a new encrypted credential in the vault. Admin only. The plain text value is encrypted with AES-256-GCM before storage — it is never saved in plain text.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Credential created successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Credential created",
                                      "data": {
                                        "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7"
                                      },
                                      "errors": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Validation failed — missing or invalid fields",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Validation failed",
                                      "data": null,
                                      "errors": {
                                        "name": "must not be blank",
                                        "value": "must not be blank",
                                        "accessTier": "must not be null"
                                      }
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "Project not found", "data": null, "errors": null}
                                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "Authentication failed", "data": null, "errors": null}
                                    """))),
            @ApiResponse(responseCode = "403", description = "Not an admin",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "You don't have permission to perform this action", "data": null, "errors": null}
                                    """)))
    })
    ApiRes<CredentialCreatedResponse> create(@RequestBody CreateCredentialRequest request);


    @Operation(summary = "Delete a credential",
            description = "Permanently deletes a credential and all associated approval requests. Admin only. This action cannot be undone.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credential deleted successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": true, "message": "Credential deleted", "data": null, "errors": null}
                                    """))),
            @ApiResponse(responseCode = "404", description = "Credential not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "Credential not found", "data": null, "errors": null}
                                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "Authentication failed", "data": null, "errors": null}
                                    """))),
            @ApiResponse(responseCode = "403", description = "Not an admin",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "You don't have permission to perform this action", "data": null, "errors": null}
                                    """)))
    })
    ApiRes<Void> delete(
            @Parameter(description = "UUID of the credential to delete", required = true)
            @PathVariable UUID credentialId);


    @Operation(summary = "Request access to a credential",
            description = """
                    Submits a multi-party approval request to access a credential's value.
                    The request stays PENDING until enough approvers vote.
                    Quorum required depends on the credential's approval policy:
                    RELAXED = 1 approver, STANDARD = 2 approvers, STRICT = 3 approvers.
                    ADMIN tier always requires all admins regardless of policy.
                    Duplicate pending requests for the same credential are rejected.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Access request submitted successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Access request submitted",
                                      "data": {
                                        "requestId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                        "status": "PENDING",
                                        "quorumRequired": 3
                                      },
                                      "errors": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "No active approvers available",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "No active approvers available. Please contact your administrator.", "data": null, "errors": null}
                                    """))),
            @ApiResponse(responseCode = "404", description = "Credential not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "Credential not found", "data": null, "errors": null}
                                    """))),
            @ApiResponse(responseCode = "409", description = "A pending request already exists for this credential",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "You already have a pending request for this credential", "data": null, "errors": null}
                                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "Authentication failed", "data": null, "errors": null}
                                    """)))
    })
    ApiRes<AccessRequestedResponse> requestAccess(
            @Parameter(description = "UUID of the credential to request access to", required = true)
            @PathVariable UUID credentialId);


    @Operation(summary = "Reveal a credential value",
            description = """
                    Returns the decrypted plain text value of a credential.
                    Only accessible after an approved request exists for the current user.
                    Access expires after 1 hour — a new request must be submitted after expiry.
                    This action is recorded in the audit log.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credential value revealed successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Success",
                                      "data": {
                                        "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                                        "name": "Production Environment Variables",
                                        "type": "ENV_VAR",
                                        "value": "DB_HOST=prod-db.internal\\nDB_PASS=Str0ng$DbPass!",
                                        "expiresAt": "2025-03-16T11:32:00"
                                      },
                                      "errors": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "No approved request found, or access has expired",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Your access has expired. Please submit a new request.",
                                      "data": null,
                                      "errors": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Credential not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "Credential not found", "data": null, "errors": null}
                                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "Authentication failed", "data": null, "errors": null}
                                    """)))
    })
    ApiRes<CredentialRevealResponse> reveal(
            @Parameter(description = "UUID of the credential to reveal", required = true)
            @PathVariable UUID credentialId);


    @Operation(summary = "List credentials in a project",
            description = "Returns a paginated list of credential summaries for a project. Only name, type, tier, and approval policy are returned — the encrypted value is never included in list responses.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credentials retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Success",
                                      "data": {
                                        "content": [
                                          {
                                            "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                                            "name": "Production Environment Variables",
                                            "type": "ENV_VAR",
                                            "accessTier": "PROJECT",
                                            "approvalPolicy": "STRICT",
                                            "createdAt": "2025-03-16T08:00:00"
                                          }
                                        ],
                                        "pagination": {
                                          "pageNumber": 0,
                                          "pageSize": 20,
                                          "totalElements": 4,
                                          "totalPages": 1
                                        }
                                      },
                                      "errors": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "User is not a member of the project",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "You are not a member of this project", "data": null, "errors": null}
                                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "Authentication failed", "data": null, "errors": null}
                                    """)))
    })
    ApiRes<PageResponse<CredentialSummary>> listByProject(
            @Parameter(description = "UUID of the project", required = true)
            @PathVariable UUID projectId,
            Pageable pageable);


    @Operation(summary = "Get credential detail",
            description = "Returns the full detail of a credential — name, type, tier, policy, and who created it. The encrypted value is not included.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credential detail retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Success",
                                      "data": {
                                        "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                                        "name": "Production Environment Variables",
                                        "type": "ENV_VAR",
                                        "accessTier": "PROJECT",
                                        "approvalPolicy": "STRICT",
                                        "createdBy": "Roa",
                                        "createdAt": "2025-03-16T08:00:00"
                                      },
                                      "errors": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "User is not a member of the project",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "You are not a member of this project", "data": null, "errors": null}
                                    """))),
            @ApiResponse(responseCode = "404", description = "Credential not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "Credential not found", "data": null, "errors": null}
                                    """))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "Authentication failed", "data": null, "errors": null}
                                    """)))
    })
    ApiRes<CredentialDetail> getDetail(
            @Parameter(description = "UUID of the credential", required = true)
            @PathVariable UUID credentialId);
}