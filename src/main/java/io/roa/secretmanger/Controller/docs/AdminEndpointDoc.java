package io.roa.secretmanger.Controller.docs;

import io.roa.secretmanger.DTO.projection.UserSummaryProjection;
import io.roa.secretmanger.DTO.response.ApiRes;
import io.roa.secretmanger.DTO.response.AuditLogResponse;
import io.roa.secretmanger.DTO.response.PageResponse;
import io.roa.secretmanger.DTO.response.Shamir.ShamirStatusResponse;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(
        name = "Admin",
        description = "Admin-only operations — user management, Shamir key setup, and audit log access"
)
@SecurityRequirement(name = "bearerAuth")
public interface AdminEndpointDoc {


    @Operation(summary = "List all users",
            description = "Returns a paginated list of all users including name, email, role, and active status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Success",
                                      "data": {
                                        "content": [
                                          {
                                            "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                            "name": "Roa",
                                            "email": "roa@demo.com",
                                            "role": "ADMIN",
                                            "isActive": true
                                          }
                                        ],
                                        "pagination": {
                                          "pageNumber": 0,
                                          "pageSize": 20,
                                          "totalElements": 6,
                                          "totalPages": 1
                                        }
                                      },
                                      "errors": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "No JWT token provided or token expired",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "Authentication failed", "data": null, "errors": null}
                                    """))),
            @ApiResponse(responseCode = "403", description = "Authenticated user does not have ADMIN role",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "You don't have permission to perform this action", "data": null, "errors": null}
                                    """)))
    })
    ApiRes<PageResponse<UserSummaryProjection>> getAllUsers(Pageable pageable);


    @Operation(summary = "Deactivate a user",
            description = "Deactivates a user account immediately. The user loses login access. Existing votes and audit records are preserved.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deactivated successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": true, "message": "User deactivated", "data": null, "errors": null}
                                    """))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "User not found", "data": null, "errors": null}
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
    ApiRes<Void> deactivateUser(
            @Parameter(description = "UUID of the user to deactivate", required = true)
            @PathVariable UUID userId);


    @Operation(summary = "Activate a user",
            description = "Re-activates a previously deactivated user. The user can log in again immediately.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User activated successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": true, "message": "User activated", "data": null, "errors": null}
                                    """))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "User not found", "data": null, "errors": null}
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
    ApiRes<Void> activateUser(
            @Parameter(description = "UUID of the user to activate", required = true)
            @PathVariable UUID userId);


    @Operation(summary = "Initialise Shamir key splitting",
            description = """
                    Splits the master AES-256 key using Shamir's Secret Sharing and distributes
                    one encrypted share to each admin. Can only be called once.
                    A majority of admins must later cooperate to reconstruct the key for ADMIN-tier access.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Master key split and distributed successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": true, "message": "Master key split and distributed to all admins", "data": null, "errors": null}
                                    """))),
            @ApiResponse(responseCode = "409", description = "Shamir shares already distributed — cannot re-initialise",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "Shamir shares have already been distributed", "data": null, "errors": null}
                                    """))),
            @ApiResponse(responseCode = "404", description = "No admin users found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "No admin users found to distribute shares to", "data": null, "errors": null}
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
    ApiRes<Void> initShamir();


    @Operation(summary = "Get Shamir initialisation status",
            description = "Returns whether the master key has been split and how many admin shares exist.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Success",
                                      "data": {
                                        "initialized": true,
                                        "totalShares": 2
                                      },
                                      "errors": null
                                    }
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
    ApiRes<ShamirStatusResponse> getShamirStatus();


    @Operation(summary = "Get audit logs",
            description = """
                    Returns a paginated, filterable audit trail of all sensitive actions.
                    Filter by actor, action type, or target type.
                    Available actions: CREDENTIAL_ACCESSED, VOTE_CAST, USER_DEACTIVATED, SHAMIR_INIT.
                    Available target types: CREDENTIAL, APPROVAL_REQUEST.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Success",
                                      "data": {
                                        "content": [
                                          {
                                            "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                            "actorName": "Tiamat",
                                            "actorEmail": "tiamat@demo.com",
                                            "action": "CREDENTIAL_ACCESSED",
                                            "targetType": "CREDENTIAL",
                                            "targetId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                                            "metadata": null,
                                            "performedAt": "2025-03-16T10:32:00"
                                          }
                                        ],
                                        "pagination": {
                                          "pageNumber": 0,
                                          "pageSize": 50,
                                          "totalElements": 24,
                                          "totalPages": 1
                                        }
                                      },
                                      "errors": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameter — e.g. malformed UUID for actorId",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Validation failed",
                                      "data": null,
                                      "errors": {"actorId": "Invalid UUID format"}
                                    }
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
    ApiRes<PageResponse<AuditLogResponse>> getAuditLogs(
            @Parameter(description = "Filter by actor UUID — who performed the action")
            @RequestParam(required = false) UUID actorId,

            @Parameter(description = "Filter by action type — e.g. CREDENTIAL_ACCESSED, VOTE_CAST")
            @RequestParam(required = false) String action,

            @Parameter(description = "Filter by target type — CREDENTIAL or APPROVAL_REQUEST")
            @RequestParam(required = false) String targetType,

            Pageable pageable);
}
