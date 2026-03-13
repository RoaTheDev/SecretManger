package io.roa.secretmanger.Model.Value;

public enum CredentialType {
    ENV_VAR,
    API_KEY,
    DATABASE_URL,
    SSH_KEY,
    TLS_CERT,
    NGINX_CONFIG,
    CONFIG_FILE,    // Generic config files (.yaml, .json, .toml, etc.)
    OTHER           // Anything that doesn't fit the above
}