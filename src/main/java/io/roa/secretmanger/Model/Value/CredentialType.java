package io.roa.secretmanger.Model.Value;

public enum CredentialType {
    ENV_VAR,
    API_KEY,
    DATABASE_URL,
    NGINX_CONFIG,
    DOCKER_CONFIG,
    TERRAFORM,
    SSH_KEY,
    TLS_CERT,
    CONFIG_FILE,    // Generic config files (.yaml, .json, .toml, etc.)
    OTHER
}