package io.roa.secretmanger.Repo.seeder;

import io.roa.secretmanger.Model.Entity.Credential;
import io.roa.secretmanger.Model.Entity.Project;
import io.roa.secretmanger.Model.Entity.User;
import io.roa.secretmanger.Model.Value.AccessTier;
import io.roa.secretmanger.Model.Value.ApprovalPolicy;
import io.roa.secretmanger.Model.Value.CredentialType;
import io.roa.secretmanger.Model.Value.UserRole;
import io.roa.secretmanger.Repo.CredentialRepo;
import io.roa.secretmanger.Repo.ProjectRepo;
import io.roa.secretmanger.Repo.UserRepo;
import io.roa.secretmanger.Service.CryptoService;
import io.roa.secretmanger.Service.ShamirService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Profile("local")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepo        userRepo;
    private final ProjectRepo     projectRepo;
    private final CredentialRepo  credentialRepo;
    private final ShamirService   shamirService;
    private final CryptoService   cryptoService;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "Password123!";

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepo.count() > 0) {
            log.info("Seeder skipped — data already exists");
            return;
        }

        log.info("Seeding demo data...");

        seedUsers();
        seedProjects();
        seedCredentials();
        seedShamirShares();

        log.info("Seeding complete — {} users, {} projects, {} credentials",
                userRepo.count(), projectRepo.count(), credentialRepo.count());
    }


    public void seedUsers() {
        createUser("Roa",    "roa@demo.com",    UserRole.ADMIN);
        createUser("Alice",  "alice@demo.com",  UserRole.ADMIN);
        createUser("Rem",    "rem@demo.com",    UserRole.TEAM_LEAD);
        createUser("Anna",   "anna@demo.com",   UserRole.PROJECT_MANAGER);
        createUser("Tiamat", "tiamat@demo.com", UserRole.DEVELOPER);
        createUser("Gwen",   "gwen@demo.com",   UserRole.DEVELOPER);

        log.info("Created 6 users — all passwords: {}", DEFAULT_PASSWORD);
        log.info("Accounts: roa@ | alice@ | rem@ | anna@ | tiamat@ | gwen@ @demo.com");
    }


    public void seedProjects() {
        var roa    = userRepo.findByEmail("roa@demo.com").orElseThrow();
        var rem    = userRepo.findByEmail("rem@demo.com").orElseThrow();
        var anna   = userRepo.findByEmail("anna@demo.com").orElseThrow();
        var tiamat = userRepo.findByEmail("tiamat@demo.com").orElseThrow();
        var gwen   = userRepo.findByEmail("gwen@demo.com").orElseThrow();

        Project webApp = new Project();
        webApp.setName("Web Application");
        webApp.setDescription("Customer-facing web portal — React frontend, Spring Boot backend");
        webApp.setCreatedBy(roa);
        webApp.setMembers(List.of(roa, rem, anna, tiamat));
        projectRepo.save(webApp);

        Project infra = new Project();
        infra.setName("Infrastructure");
        infra.setDescription("Server configuration, deployment pipelines, and cloud resources");
        infra.setCreatedBy(roa);
        infra.setMembers(List.of(roa, rem, gwen));
        projectRepo.save(infra);

        log.info("Created 2 projects: 'Web Application', 'Infrastructure'");
    }

    public void seedCredentials() {
        var roa    = userRepo.findByEmail("roa@demo.com").orElseThrow();
        var webApp = projectRepo.findAll().stream()
                .filter(p -> p.getName().equals("Web Application"))
                .findFirst().orElseThrow();
        var infra  = projectRepo.findAll().stream()
                .filter(p -> p.getName().equals("Infrastructure"))
                .findFirst().orElseThrow();


        saveCredential(webApp, roa,
                "Development Environment Variables",
                CredentialType.ENV_VAR,
                """
                DB_HOST=localhost
                DB_PORT=5432
                DB_NAME=webapp_dev
                DB_USER=dev_user
                DB_PASS=devpassword
                JWT_SECRET=dev-jwt-secret
                APP_PORT=8080
                """,
                AccessTier.PROJECT, ApprovalPolicy.RELAXED);

        saveCredential(infra, roa,
                "Local Docker Compose",
                CredentialType.DOCKER_CONFIG,
                """
                version: '3.8'
                services:
                  app:
                    image: webapp:dev
                    ports:
                      - '8080:8080'
                  db:
                    image: postgres:15
                    environment:
                      POSTGRES_PASSWORD: devpassword
                """,
                AccessTier.PROJECT, ApprovalPolicy.RELAXED);


        saveCredential(webApp, roa,
                "Staging Environment Variables",
                CredentialType.ENV_VAR,
                """
                DB_HOST=staging-db.internal
                DB_PORT=5432
                DB_NAME=webapp_staging
                DB_USER=staging_user
                DB_PASS=Staging$Pass99
                JWT_SECRET=staging-jwt-256bit-secret
                STRIPE_KEY=sk_test_demo123
                """,
                AccessTier.PROJECT, ApprovalPolicy.STANDARD);

        saveCredential(infra, roa,
                "Staging Nginx Config",
                CredentialType.NGINX_CONFIG,
                """
                server {
                    listen 80;
                    server_name staging.webapp.demo.com;
                    location / {
                        proxy_pass http://localhost:8080;
                        proxy_set_header Host $host;
                    }
                }
                """,
                AccessTier.PROJECT, ApprovalPolicy.STANDARD);


        saveCredential(webApp, roa,
                "Production Environment Variables",
                CredentialType.ENV_VAR,
                """
                DB_HOST=prod-db.internal
                DB_PORT=5432
                DB_NAME=webapp_prod
                DB_USER=app_user
                DB_PASS=Str0ng$DbPass!
                JWT_SECRET=prod-jwt-secret-256bit
                STRIPE_KEY=sk_live_demo123
                SMTP_PASS=Smtp$ecret99
                """,
                AccessTier.PROJECT, ApprovalPolicy.STRICT);

        saveCredential(infra, roa,
                "Production Docker Compose",
                CredentialType.DOCKER_CONFIG,
                """
                version: '3.8'
                services:
                  app:
                    image: webapp:latest
                    environment:
                      - DB_HOST=${DB_HOST}
                      - DB_PASS=${DB_PASS}
                      - JWT_SECRET=${JWT_SECRET}
                    ports:
                      - '8080:8080'
                    restart: always
                """,
                AccessTier.PROJECT, ApprovalPolicy.STRICT);

        saveCredential(infra, roa,
                "Terraform Cloud Variables",
                CredentialType.TERRAFORM,
                """
                TF_VAR_db_password=Infra$ecret99
                TF_VAR_region=ap-southeast-1
                TF_VAR_instance_type=t3.medium
                AWS_ACCESS_KEY_ID=AKIAIOSFODNN7DEMO
                AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/DEMO/bPxRfiCYDEMOKEY
                """,
                AccessTier.PROJECT, ApprovalPolicy.STRICT);


        saveCredential(webApp, roa,
                "Production Nginx SSL Certificate",
                CredentialType.NGINX_CONFIG,
                """
                -----BEGIN CERTIFICATE-----
                MIIDXTCCAkWgAwIBAgIJANDEMOCERTDEMO...
                -----END CERTIFICATE-----
                -----BEGIN PRIVATE KEY-----
                MIIEvQIBADANBgkqhkiG9w0BAQEFAADEM...
                -----END PRIVATE KEY-----
                """,
                AccessTier.ADMIN, ApprovalPolicy.STRICT);

        saveCredential(infra, roa,
                "Terraform Backend State Config",
                CredentialType.TERRAFORM,
                """
                terraform {
                  backend "s3" {
                    bucket     = "prod-terraform-state"
                    key        = "global/s3/terraform.tfstate"
                    region     = "ap-southeast-1"
                    access_key = "AKIAIOSFODNN7ADMIN"
                    secret_key = "AdminOnlySecret/K7MDENG/ADMINKEY"
                  }
                }
                """,
                AccessTier.ADMIN, ApprovalPolicy.STRICT);

        log.info("Created 9 credentials: 2 RELAXED, 2 STANDARD, 3 STRICT (PROJECT tier), 2 ADMIN tier");
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void seedShamirShares() {
        if (shamirService.isInitialized()) {
            log.info("Shamir shares already distributed — skipping");
            return;
        }
        shamirService.splitAndDistribute();
        log.info("Shamir shares distributed — threshold: {}/{} admins required",
                (userRepo.countByRole(UserRole.ADMIN) / 2) + 1,
                userRepo.countByRole(UserRole.ADMIN));
    }


    private void createUser(String name, String email, UserRole role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setRole(role);
        user.setActive(true);
        userRepo.save(user);
    }

    private void saveCredential(Project project, User createdBy,
                                String name, CredentialType type,
                                String plainValue, AccessTier tier,
                                ApprovalPolicy policy) {
        Credential credential = new Credential();
        credential.setProject(project);
        credential.setCreatedBy(createdBy);
        credential.setName(name);
        credential.setType(type);
        credential.setEncryptedValue(cryptoService.encrypt(plainValue));
        credential.setAccessTier(tier);
        credential.setApprovalPolicy(policy);
        credentialRepo.save(credential);
    }
}