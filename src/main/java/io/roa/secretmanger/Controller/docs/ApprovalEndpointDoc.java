package io.roa.secretmanger.Controller.docs;


import io.roa.secretmanger.DTO.request.ApprovalRequest.CastVoteRequest;
import io.roa.secretmanger.DTO.response.ApiRes;
import io.roa.secretmanger.DTO.response.ApprovalRequest.ApprovalRequestSummary;
import io.roa.secretmanger.DTO.response.ApprovalRequest.VoteCastResponse;
import io.roa.secretmanger.DTO.response.PageResponse;
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

@Tag(name = "Approvals", description = "Approval voting — cast votes and view pending requests")
@SecurityRequirement(name = "bearerAuth")
public interface ApprovalEndpointDoc {

    @Operation(summary = "Cast a vote on an approval request",
            description = """
                    Cast an APPROVE or REJECT vote on a pending credential access request.
                    Eligible voters: ADMIN, TEAM_LEAD, PROJECT_MANAGER.
                    Each person can only vote once per request.
                    You cannot vote on your own request.
                    When enough approvals are collected (quorum), access is granted automatically.
                    A single rejection immediately rejects the request.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vote cast successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "message": "Vote cast",
                              "data": {
                                "requestId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                "currentStatus": "APPROVED",
                                "quorumReached": true
                              },
                              "errors": null
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "Request is no longer pending — already approved, rejected, or expired",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "success": false,
                              "message": "This request is no longer pending",
                              "data": null,
                              "errors": null
                            }
                            """))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {"success": false, "message": "Authentication failed", "data": null, "errors": null}
                            """))),
            @ApiResponse(responseCode = "403", description = "User does not have a voting role, or tried to vote on their own request",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "success": false,
                              "message": "You cannot vote on your own request",
                              "data": null,
                              "errors": null
                            }
                            """))),
            @ApiResponse(responseCode = "404", description = "Approval request not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {"success": false, "message": "Approval request not found", "data": null, "errors": null}
                            """))),
            @ApiResponse(responseCode = "409", description = "User has already voted on this request",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "success": false,
                              "message": "You have already voted on this request",
                              "data": null,
                              "errors": null
                            }
                            """)))
    })
    ApiRes<VoteCastResponse> castVote(
            @Parameter(description = "UUID of the approval request to vote on", required = true)
            @PathVariable UUID requestId,
            @RequestBody CastVoteRequest request);


    @Operation(summary = "List pending approval requests for current user",
            description = """
                    Returns a paginated list of credential access requests that the current user
                    is eligible to vote on and has not yet voted on.
                    Requests created by the current user are excluded.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pending requests retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "message": "Success",
                              "data": {
                                "content": [
                                  {
                                    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                    "credentialId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                                    "credentialName": "Production Environment Variables",
                                    "requestedBy": "Tiamat",
                                    "accessTier": "PROJECT",
                                    "status": "PENDING",
                                    "quorumRequired": 3,
                                    "approveCount": 1,
                                    "rejectCount": 0,
                                    "createdAt": "2025-03-16T09:15:00"
                                  }
                                ],
                                "pagination": {
                                  "pageNumber": 0,
                                  "pageSize": 20,
                                  "totalElements": 3,
                                  "totalPages": 1
                                }
                              },
                              "errors": null
                            }
                            """))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {"success": false, "message": "Authentication failed", "data": null, "errors": null}
                            """))),
            @ApiResponse(responseCode = "403", description = "User does not have a voting role",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {"success": false, "message": "You don't have permission to perform this action", "data": null, "errors": null}
                            """)))
    })
    ApiRes<PageResponse<ApprovalRequestSummary>> getPending(Pageable pageable);
}
