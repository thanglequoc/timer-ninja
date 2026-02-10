# GitHub Actions Workflows

This directory contains GitHub Actions workflows for CI/CD and release automation.

## Workflows

### 1. Pull Request CI/CD (`pull-request.yml`)
- **Trigger**: Pull requests to master branch
- **Purpose**: Build and test code across multiple Java versions
- **Matrix**: Tests on Java 17, 21, and 25
- **Actions**:
  - Checks out code
  - Sets up JDK
  - Runs Gradle build
  - Executes tests
  - Uploads test results and reports
  - Publishes test results to PR

### 2. Release (`release.yml`)
- **Trigger**: Manual workflow dispatch only
- **Purpose**: Publish releases to Maven Central using version from build.gradle
- **Actions**:
  - Checks out code
  - Sets up JDK 17
  - Imports GPG signing key
  - Builds and tests project
  - Publishes to Maven Central via JReleaser
  - Cleans up sensitive data

## Setup Instructions for Release Workflow

### Step 1: Configure GitHub Secrets

You need to add the following secrets to your GitHub repository:

1. Go to your repository on GitHub
2. Navigate to **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret** and add each secret:

#### Required Secrets:

| Secret Name | Description | How to Get |
|------------|-------------|------------|
| `SECRET_KEY_ASC` | Base64-encoded GPG private key | See Step 2 below |
| `SONATYPE_USERNAME` | Sonatype User Token username | See Step 3 below |
| `SONATYPE_PASSWORD` | Sonatype User Token password | See Step 3 below |
| `GITHUB_TOKEN` | (Auto-provided) GitHub token | No action needed |

### Step 2: Encode Your GPG Secret Key

The workflow needs your GPG secret key encoded in base64 format:

```bash
# From your project root directory
base64 -i secret_key.asc
```

Copy the entire output (a long string) and add it as a GitHub secret named `SECRET_KEY_ASC`.

**Note**: On Mac, you might need:
```bash
base64 secret_key.asc | tr -d '\n'
```

### Step 3: Generate Sonatype User Token

You need a Sonatype User Token for Maven Central publishing:

1. Go to https://central.sonatype.com
2. Sign in with your Sonatype account
3. Navigate to your account settings (click on your username)
4. Find "User Token" or "API Keys" section
5. Generate a new User Token
6. Copy the **username** and **password** (these are NOT your login credentials)
7. Add them as GitHub secrets:
   - `SONATYPE_USERNAME` = the token username
   - `SONATYPE_PASSWORD` = the token password

**Important**: Sonatype User Tokens are different from your regular login credentials. You must use the token credentials for publishing.

### Step 4: Publish Your GPG Public Key

Before releasing, ensure your GPG public key is available on a key server:

```bash
# From your local machine
gpg --keyserver keyserver.ubuntu.com --send-keys 799A99750C819FB915ECDBBC144D9369E0328F75
```

If you have network issues, you can upload manually at https://keys.openpgp.org/upload using the content from `public_key.asc`.

### Step 5: Verify Workflow Permissions

Ensure your workflow has the necessary permissions:

1. Go to **Settings** → **Actions** → **General**
2. Under "Workflow permissions", select:
   - ✅ Read and write permissions
   - ✅ Allow GitHub Actions to create and approve pull requests

## How to Release

### Manual Release Process

1. **Prepare for Release**:
   ```bash
   # Update version in build.gradle
   # Example: version '1.3.0'
   
   # Run tests locally first
   ./gradlew clean build test
   
   # Commit your changes
   git add build.gradle
   git commit -m "chore: bump version to 1.3.0"
   git push origin master
   ```

2. **Trigger the Workflow**:
   - Go to **Actions** tab in your GitHub repository
   - Select **Release** workflow from the left sidebar
   - Click **Run workflow** button
   - Select the branch (usually `master`)
   - Click **Run workflow**

3. **Monitor Progress**:
   - Click on the running workflow to view logs
   - The workflow will:
     - Build and test the code using the version from build.gradle
     - Sign artifacts with GPG
     - Publish to Maven Central
### Quick Release Guide

1. **Update version** in `build.gradle`:
   ```gradle
   version '1.3.0'
   ```

2. **Run tests locally**: `./gradlew clean build test`

3. **Commit and push**: 
   ```bash
   git add build.gradle
   git commit -m "chore: bump version to 1.3.0"
   git push origin master
   ```

4. **Go to Actions → Release workflow**
5. **Click "Run workflow"**
6. **Select branch** (usually `master`)
7. **Click "Run workflow"**

That's it! The workflow will use the version from `build.gradle` and handle the rest.

## Monitoring the Release

After triggering a release:

1. Go to **Actions** tab in your GitHub repository
2. Click on the running workflow
3. View the logs to monitor progress
4. Check for any errors or warnings

If the workflow fails:
- Check the logs for specific error messages
- Verify all secrets are correctly configured
- Ensure GPG key is properly encoded
- Verify Sonatype credentials are valid

## After the Release

Once the release succeeds:

1. **Maven Central**: Your artifacts will be available at https://central.sonatype.com/artifact/io.github.thanglequoc/timer-ninja
2. **Verification**: The release may take some time to sync (usually 10-30 minutes)
3. **Testing**: You can verify by checking Maven Central or using:
   ```bash
   # Search for your artifact
   curl https://search.maven.org/solrsearch/select?q=g:io.github.thanglequoc+AND+a:timer-ninja
   ```

## Troubleshooting

### Issue: "GPG import failed"
**Solution**: 
- Verify `SECRET_KEY_ASC` is properly base64 encoded
- Ensure the key is not corrupted
- Check that the key matches the key ID in the workflow

### Issue: "Version mismatch"
**Solution**:
- Ensure the version in `build.gradle` is the one you want to release
- Commit and push the version change before running the workflow
- The workflow uses the version directly from `build.gradle`

### Issue: "Sonatype authentication failed"
**Solution**:
- Verify you're using User Token credentials, not regular login
- Check that `SONATYPE_USERNAME` and `SONATYPE_PASSWORD` are correct
- Ensure the User Token is still valid (tokens can expire)

### Issue: "Key not found on key server"
**Solution**:
- Upload your public key to a key server
- Wait for propagation (5-30 minutes)
- Verify with: `gpg --keyserver keyserver.ubuntu.com --recv-keys YOUR_KEY_ID`

### Issue: "Build tests failed"
**Solution**:
- Run tests locally first: `./gradlew clean build test`
- Fix any failing tests before releasing
- Ensure code is committed to master branch

### Issue: "Permission denied"
**Solution**:
- Check workflow permissions in repository settings
- Ensure actions have read/write access
- Verify the GitHub token is available

## Security Considerations

1. **Never commit secret_key.asc** - It's in .gitignore
2. **Rotate secrets regularly** - Update Sonatype tokens periodically
3. **Monitor workflow runs** - Check for unauthorized access
4. **Limit workflow permissions** - Only give necessary permissions
5. **Use branch protection** - Require PR reviews for master branch

## Workflow Files

- `pull-request.yml` - CI/CD for pull requests
- `release.yml` - Automated release to Maven Central

## Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [JReleaser GitHub Actions](https://jreleaser.org/guide/latest/reference/github.html)
- [Sonatype Central Publishing](https://central.sonatype.org/publish/)
- [GPG Best Practices](https://gnupg.org/documentation/)

## Support

For issues with:
- **GitHub Actions**: Check [GitHub Actions Documentation](https://docs.github.com/en/actions)
- **JReleaser**: Check [JReleaser Documentation](https://jreleaser.org/)
- **Sonatype/Maven Central**: Check [Sonatype Central Guide](https://central.sonatype.org/)