# Release Setup Guide for Timer Ninja

This document explains how to set up release process for publishing Timer Ninja to Maven Central (Sonatype) using JReleaser.

## Recovery from Previous Mistake

During a previous session, following critical files were accidentally deleted:
- `public_key.asc` - GPG public key
- `secret_key.asc` - GPG secret key (containing passphrase)
- Sonatype credentials from build.gradle

This guide documents recovery process and provides instructions for future reference.

## What Has Been Done

### 1. Generated New GPG Key Pair
A new GPG key pair has been generated with following details:
- **Key ID**: `799A99750C819FB915ECDBBC144D9369E0328F75`
- **Type**: RSA 4096-bit
- **Owner**: Thang Le Quoc <thanglequoc.it@gmail.com>
- **Expiration**: None (permanent)
- **Protection**: No passphrase (for automated CI/CD)

**Note**: The key was generated without a passphrase to support automated releases in CI/CD pipelines. This is a common practice but requires careful handling of secret key file.

### 2. Exported Keys
- `public_key.asc` - Exported and stored in project root (safe to commit)
- `secret_key.asc` - Exported and stored in project root (DO NOT commit)

### 3. Updated .gitignore
Added rules to prevent accidental commits of sensitive files:
```
### Security - Keys & Credentials ###
secret_key.asc
*.asc
!public_key.asc
```

### 4. Created JReleaser Configuration
Created `jreleaser.yml` with proper configuration for:
- Maven Central deployment
- Sonatype integration
- GPG signing
- Environment variable-based credentials

### 5. Updated build.gradle
Added JReleaser DSL configuration to enable signing and deployment features. No hardcoded credentials.

## Required Setup Steps

### 1. Publish Your GPG Public Key to a Key Server

Before you can publish to Maven Central, your public key must be available on a public key server:

```bash
# Upload to multiple key servers
gpg --keyserver keyserver.ubuntu.com --send-keys 799A99750C819FB915ECDBBC144D9369E0328F75
gpg --keyserver pgp.mit.edu --send-keys 799A99750C819FB915ECDBBC144D9369E0328F75
gpg --keyserver keys.openpgp.org --send-keys 799A99750C819FB915ECDBBC144D9369E0328F75
```

**Important**: The key must be propagated to at least one key server before Maven Central will accept signed artifacts.

### 2. Set Up Sonatype User Token

You need to obtain Sonatype User Token for publishing to Maven Central:

1. Go to https://central.sonatype.com/usertoken
2. Sign in with your Sonatype account (or create one)
3. Click "Generate User Token"
4. Save the generated credentials:
   - **Token ID** (username): This is just an identifier
   - **Token** (password): This is the actual BEARER token that will be used for authentication

**Note**: Sonatype User Token uses BEARER token authentication. Both username and password are required in configuration, but for BEARER auth, JReleaser only uses the password field as the actual token.

### 3. Configure Environment Variables

#### For Local Development:
Set the following environment variables before running JReleaser:

```bash
export SONATYPE_USERNAME="your-sonatype-user-token-username"
export SONATYPE_PASSWORD="your-sonatype-user-token-password"
export GPG_PASSPHRASE=""  # Empty since key has no passphrase
```

You can add these to your shell profile (~/.zshrc or ~/.bashrc):
```bash
# Add to ~/.zshrc or ~/.bashrc
export SONATYPE_USERNAME="your-username"
export SONATYPE_PASSWORD="your-password"
export GPG_PASSPHRASE=""
```

#### For GitHub Actions:
Add these as secrets and variables in your repository settings:

**Variables** (not sensitive - visible to repository members):
- `GPG_PUBLIC_KEY` - The full content of `public_key.asc` file (NOT base64-encoded)

**Secrets** (sensitive - hidden):
- `GPG_SECRET_KEY` - The full content of `secret_key.asc` file (NOT base64-encoded)
- `SONATYPE_USERNAME` - Your Sonatype User Token username (Token ID)
- `SONATYPE_PASSWORD` - Your Sonatype User Token password (the actual token)

### 4. Verify GPG Key Configuration

Ensure that GPG keys are properly configured:

```bash
# List public keys
gpg --list-keys

# List secret keys
gpg --list-secret-keys

# Verify key is exported
cat public_key.asc
cat secret_key.asc
```

## Release Process

### Option 1: Local Release

1. Set environment variables:
```bash
export SONATYPE_USERNAME="your-username"
export SONATYPE_PASSWORD="your-password"
export GPG_PASSPHRASE=""
```

2. Build and publish:
```bash
./gradlew clean build
./gradlew publishToMavenLocal
```

3. Deploy to Maven Central:
```bash
./gradlew jreleaserFullRelease
```

### Option 2: GitHub Actions (Recommended)

1. Ensure GitHub Actions are configured in `.github/workflows/`

2. Add variables and secrets to repository (Settings → Secrets and variables → Actions):

**Variables**:
- `GPG_PUBLIC_KEY` - Copy the entire content of `public_key.asc` file (including -----BEGIN/END PGP PUBLIC KEY BLOCK-----)

**Secrets**:
- `GPG_SECRET_KEY` - Copy the entire content of `secret_key.asc` file (including -----BEGIN/END PGP PRIVATE KEY BLOCK-----)
- `SONATYPE_USERNAME` - Your Sonatype User Token username (Token ID)
- `SONATYPE_PASSWORD` - Your Sonatype User Token password (the actual token)

3. Trigger the workflow manually:
   - Go to **Actions** → **Release - Publish to Sonatype Maven Central**
   - Click **Run workflow**
   - Select branch (usually `master`)
   - Click **Run workflow**

## Publishing Your Public Key

If you haven't already published your public key to a key server, do this now:

```bash
# Upload to Ubuntu key server (most commonly used)
gpg --keyserver keyserver.ubuntu.com --send-keys 799A99750C819FB915ECDBBC144D9369E0328F75

# Verify it's available
gpg --keyserver keyserver.ubuntu.com --recv-keys 799A99750C819FB915ECDBBC144D9369E0328F75
```

Wait a few minutes for key to propagate across servers before attempting to publish.

## Troubleshooting

### Issue: "Key not found on key server"
**Solution**: Upload your public key to a key server and wait for propagation (can take 5-30 minutes)

### Issue: "GPG signing failed"
**Solution**: Ensure `secret_key.asc` exists in project root and is accessible

### Issue: "Sonatype authentication failed (401 Unauthorized)"
**Solution**: 
- Verify you're using User Token credentials, not your regular Sonatype login
- Ensure BOTH `SONATYPE_USERNAME` and `SONATYPE_PASSWORD` are set in GitHub secrets
- Check that `jreleaser.yml` has `authorization: BEARER` configured
- Verify the token hasn't expired by regenerating it at https://central.sonatype.com/usertoken
- Ensure environment variables in workflow use correct JRELEASER-specific names: `JRELEASER_DEPLOY_MAVEN_MAVENCENTRAL_SONATYPE_USERNAME` and `JRELEASER_DEPLOY_MAVEN_MAVENCENTRAL_SONATYPE_PASSWORD`

### Issue: "Artifacts rejected by Maven Central"
**Solution**: 
- Verify your GPG public key is on a key server
- Check that pom.xml has all required metadata
- Ensure all required files (javadoc jar, sources jar) are included

### Issue: "Version mismatch"
**Solution**:
- Ensure that version in `build.gradle` is one you want to release
- Commit and push version change before running workflow
- The workflow uses version directly from `build.gradle`

### Issue: "Signing is not enabled. Skipping"
**Solution**: This is expected when running locally without all environment variables set. In GitHub Actions with proper secrets, signing will be enabled.

### Issue: "Deploying is not enabled. Skipping"
**Solution**: This is expected when running locally without `SONATYPE_PASSWORD` set. In GitHub Actions with proper secrets, deployment will be enabled.

## Security Best Practices

1. **Never commit `secret_key.asc`** - It's already in .gitignore
2. **Never share Sonatype credentials** - Use environment variables or secrets
3. **Back up your secret key securely** - Store in a password manager or encrypted storage
4. **Keep revocation certificate** - Located at `~/.gnupg/openpgp-revocs.d/799A99750C819FB915ECDBBC144D9369E0328F75.rev`
5. **Consider using a passphrase** - For production, add a passphrase and store it securely

## Key Revocation Certificate

The revocation certificate is stored at:
```
~/.gnupg/openpgp-revocs.d/799A99750C819FB915ECDBBC144D9369E0328F75.rev
```

**IMPORTANT**: Back up this certificate in a secure location. If your private key is ever compromised, you'll need it to revoke the public key.

## JReleaser Configuration Details

### Authentication Method
The current configuration uses **BEARER authentication** (Sonatype User Token), not BASIC authentication. This is configured in `jreleaser.yml` with `authorization: BEARER`.

### Sonatype User Token Authentication
Sonatype Central uses User Token authentication for publishing:
- **Token ID** (username): Identifier for the token
- **Token** (password): The actual BEARER token used for authentication

For BEARER authentication with JReleaser:
- Set `authorization: BEARER` in `jreleaser.yml`
- Set `username: ${SONATYPE_USERNAME}` and `password: ${SONATYPE_PASSWORD}` in YAML
- JReleaser ignores username for BEARER auth and only uses password as token

### JReleaser Environment Variables
GitHub Actions workflow sets the following JReleaser-specific environment variables:
- `JRELEASER_GITHUB_TOKEN`: GitHub token for creating releases
- `JRELEASER_GITHUB_USERNAME`: GitHub username
- `JRELEASER_GPG_PUBLIC_KEY`: GPG public key content
- `JRELEASER_GPG_SECRET_KEY`: GPG secret key content
- `JRELEASER_GPG_PASSPHRASE`: GPG passphrase (empty)
- `JRELEASER_DEPLOY_MAVEN_MAVENCENTRAL_SONATYPE_USERNAME`: Sonatype User Token username (from `SONATYPE_USERNAME` secret)
- `JRELEASER_DEPLOY_MAVEN_MAVENCENTRAL_SONATYPE_PASSWORD`: Sonatype User Token password (from `SONATYPE_PASSWORD` secret)

**Important**: JReleaser requires specific environment variable names with prefix `JRELEASER_`. The YAML file uses short names like `${SONATYPE_USERNAME}`, and JReleaser automatically looks for the corresponding full environment variable name.

### Signing Mode
Using MEMORY mode (default) which treats `JRELEASER_GPG_SECRET_KEY` as the actual GPG secret key content (not base64-encoded or file path).

## Next Steps

1. Upload your GPG public key to key servers
2. Generate and configure Sonatype User Token credentials
3. Set environment variables for your development environment
4. Configure GitHub Actions secrets and variables (if using CI/CD)
5. Test the release process
6. Create your first official release!

## GitHub Actions Release (Recommended)

For detailed setup and troubleshooting, see:
- **[`.github/workflows/README.md`](.github/workflows/README.md)** - Workflow documentation
- **[`development/jreleaser_fix.md`](jreleaser_fix.md)** - Recent fixes and configuration details

### Quick Setup for GitHub Actions

1. **Add variables and secrets** (Settings → Secrets and variables → Actions):
   
   **Variables**:
   - `GPG_PUBLIC_KEY`: Copy the entire content of `public_key.asc` file (including -----BEGIN/END PGP PUBLIC KEY BLOCK-----)
   
   **Secrets**:
   - `GPG_SECRET_KEY`: Copy the entire content of `secret_key.asc` file (including -----BEGIN/END PGP PRIVATE KEY BLOCK-----)
   - `SONATYPE_USERNAME`: Your Sonatype User Token username (Token ID)
   - `SONATYPE_PASSWORD`: Your Sonatype User Token password (the actual token)

2. **To release**:
   - Go to **Actions** → **Release - Publish to Sonatype Maven Central**
   - Click **Run workflow**
   - Select branch (usually `master`)
   - Click **Run workflow**
   - The workflow will use the version from `build.gradle` and deploy to Maven Central

3. **Verify workflow permissions**: Go to Settings → Actions → General and ensure:
   - ✅ Read and write permissions
   - ✅ Allow GitHub Actions to create and approve pull requests

## Additional Resources

- [JReleaser Documentation](https://jreleaser.org/)
- [JReleaser Maven Central Reference](https://jreleaser.org/guide/latest/reference/deploy/maven/maven-central)
- [Sonatype Central Publishing Guide](https://central.sonatype.org/publish/)
- [Maven Central Portal](https://central.sonatype.com/)
- [GPG Documentation](https://gnupg.org/documentation/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)

## Summary of Files

| File | Status | Purpose |
|------|--------|---------|
| `public_key.asc` | ✅ Safe to commit | GPG public key for signature verification |
| `secret_key.asc` | ❌ Do NOT commit | GPG private key for signing artifacts |
| `jreleaser.yml` | ✅ Safe to commit | JReleaser configuration |
| `build.gradle` | ✅ Updated | Build configuration with JReleaser DSL |
| `.gitignore` | ✅ Updated | Prevents accidental commits of secrets |
| `.github/workflows/release.yml` | ✅ Safe to commit | GitHub Actions workflow for releases |

## Workflow Flow

The GitHub Actions release workflow follows these steps:

1. **Run tests** - Ensure code quality
2. **Publish Maven artifacts** - Creates JARs and publishes to `build/staging-deploy`
3. **Validate JReleaser configuration** - Checks configuration before attempting release
4. **Full release with JReleaser**:
   - Sign artifacts with GPG
   - Deploy to Sonatype Maven Central
   - Create GitHub release

---

**Last Updated**: 2026-02-10
**Key ID**: 799A99750C819FB915ECDBBC144D9369E0328F75
**Authentication**: BEARER (Sonatype User Token)