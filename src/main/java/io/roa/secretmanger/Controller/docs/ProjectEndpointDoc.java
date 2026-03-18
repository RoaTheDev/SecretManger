package io.roa.secretmanger.Controller.docs;


import io.roa.secretmanger.DTO.request.Project.AddMemberRequest;
import io.roa.secretmanger.DTO.request.Project.CreateProjectRequest;
import io.roa.secretmanger.DTO.response.ApiRes;
import io.roa.secretmanger.DTO.response.PageResponse;
import io.roa.secretmanger.DTO.response.Project.ProjectCreatedResponse;
import io.roa.secretmanger.DTO.response.Project.ProjectDetail;
import io.roa.secretmanger.DTO.response.Project.ProjectSummary;
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

@Tag(name = "Projects", description = "Project management — create projects and manage members")
@SecurityRequirement(name = "bearerAuth")
public interface ProjectEndpointDoc {

    @Operation(summary = "Create a project",
            description = "Creates a new project. Admin only. The creating admin is automatically added as a member.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Project created successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Project created",
                                      "data": {
                                        "id": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
                                      },
                                      "errors": null
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Validation failed — name is required",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "message": "Validation failed",
                                      "data": null,
                                      "errors": {
                                        "name": "must not be blank"
                                      }
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
    ApiRes<ProjectCreatedResponse> create(@RequestBody CreateProjectRequest request);


    @Operation(summary = "Add a member to a project",
            description = "Adds an existing user to a project. Admin only. Members can view credentials in the project and submit access requests.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member added successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": true, "message": "Member added", "data": null, "errors": null}
                                    """))),
            @ApiResponse(responseCode = "404", description = "Project or user not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "User not found", "data": null, "errors": null}
                                    """))),
            @ApiResponse(responseCode = "409", description = "User is already a member of this project",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": false, "message": "User is already a member", "data": null, "errors": null}
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
    ApiRes<Void> addMember(
            @Parameter(description = "UUID of the project", required = true)
            @PathVariable UUID projectId,
            @RequestBody AddMemberRequest request);


    @Operation(summary = "Remove a member from a project",
            description = "Removes a user from a project. Admin only. Their existing approval votes and audit records are preserved.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member removed successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"success": true, "message": "Member removed", "data": null, "errors": null}
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
    ApiRes<Void> removeMember(
            @Parameter(description = "UUID of the project", required = true)
            @PathVariable UUID projectId,
            @Parameter(description = "UUID of the user to remove", required = true)
            @PathVariable UUID userId);


    @Operation(summary = "List my projects",
            description = "Returns a paginated list of projects the current user is a member of or created.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Success",
                                      "data": {
                                        "content": [
                                          {
                                            "id": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
                                            "name": "Web Application",
                                            "description": "Customer-facing web portal",
                                            "memberCount": 4,
                                            "createdAt": "2025-03-16T08:00:00"
                                          }
                                        ],
                                        "pagination": {
                                          "pageNumber": 0,
                                          "pageSize": 20,
                                          "totalElements": 2,
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
                                    """)))
    })
    ApiRes<PageResponse<ProjectSummary>> getMyProjects(Pageable pageable);


    @Operation(summary = "Get project detail",
            description = "Returns full project details including the member list. Only accessible to project members.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project detail retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Success",
                                      "data": {
                                        "id": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
                                        "name": "Web Application",
                                        "description": "Customer-facing web portal",
                                        "members": [
                                          {
                                            "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                            "name": "Roa",
                                            "email": "roa@demo.com",
                                            "role": "ADMIN"
                                          }
                                        ],
                                        "createdAt": "2025-03-16T08:00:00"
                                      },
                                      "errors": null
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
                                    """)))
    })
    ApiRes<ProjectDetail> getDetail(
            @Parameter(description = "UUID of the project", required = true)
            @PathVariable UUID projectId);
}