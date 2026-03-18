package io.roa.secretmanger.Controller.docs;


import io.roa.secretmanger.DTO.request.Auth.LoginRequest;
import io.roa.secretmanger.DTO.request.Auth.RegisterRequest;
import io.roa.secretmanger.DTO.response.ApiRes;
import io.roa.secretmanger.DTO.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "Auth", description = "Authentication — register, login, token refresh, and logout")
public interface AuthEndpointDoc {

    @Operation(summary = "Register a new user",
            description = "Creates a new user account. Role is assigned at registration by an admin.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "message": "User registered successfully",
                              "data": null,
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
                                "email": "must be a valid email address",
                                "password": "must be at least 8 characters"
                              }
                            }
                            """))),
            @ApiResponse(responseCode = "409", description = "Email already in use",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "success": false,
                              "message": "Email already in use",
                              "data": null,
                              "errors": null
                            }
                            """)))
    })
    ApiRes<Void> register(RegisterRequest request);


    @Operation(summary = "Login",
            description = """
                    Authenticates a user and returns a short-lived JWT access token in the response body.
                    A long-lived refresh token is set as an HttpOnly cookie automatically.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "message": "Success",
                              "data": {
                                "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                                "user": {
                                  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                  "name": "Tiamat",
                                  "email": "tiamat@demo.com",
                                  "role": "DEVELOPER"
                                }
                              },
                              "errors": null
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "Validation failed — missing fields",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "success": false,
                              "message": "Validation failed",
                              "data": null,
                              "errors": {
                                "email": "must not be blank",
                                "password": "must not be blank"
                              }
                            }
                            """))),
            @ApiResponse(responseCode = "401", description = "Invalid email or password",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "success": false,
                              "message": "Invalid email or password",
                              "data": null,
                              "errors": null
                            }
                            """)))
    })
    ApiRes<LoginResponse> login(LoginRequest request, HttpServletResponse response);


    @Operation(summary = "Refresh access token",
            description = """
                    Issues a new access token using the refresh token stored in the HttpOnly cookie.
                    The old refresh token is rotated and a new one is set in the cookie.
                    No request body required.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "message": "Success",
                              "data": {
                                "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                                "user": {
                                  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                                  "name": "Tiamat",
                                  "email": "tiamat@demo.com",
                                  "role": "DEVELOPER"
                                }
                              },
                              "errors": null
                            }
                            """))),
            @ApiResponse(responseCode = "401", description = "Refresh token missing, expired, or invalid",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "success": false,
                              "message": "No refresh token found",
                              "data": null,
                              "errors": null
                            }
                            """)))
    })
    ApiRes<LoginResponse> refresh(HttpServletRequest request, HttpServletResponse response);


    @Operation(summary = "Logout",
            description = "Clears the refresh token cookie. The access token will expire naturally based on its TTL.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logged out successfully",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "message": "Logged out successfully",
                              "data": null,
                              "errors": null
                            }
                            """)))
    })
    ApiRes<Void> logout(HttpServletResponse response);
}