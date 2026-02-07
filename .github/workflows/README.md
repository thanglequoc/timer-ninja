# GitHub Actions Workflow Documentation

## Pull Request CI/CD Workflow

This workflow automatically builds, tests, and scans for security vulnerabilities on every pull request to the `master` branch.

### Workflow File
`.github/workflows/pull-request.yml`

### What It Does

#### 1. Build & Test Job
Runs in parallel on three Java LTS versions (17, 21, 25):
- Checks out the code
- Sets up the specified Java version
- Caches Gradle dependencies for faster builds
- Runs `./gradlew clean build test` with all JUnit 5 tests
- Uploads test results and reports
- Publishes test results to the GitHub UI

#### 2. Security Scanning Job
Runs only if all build jobs pass:
- Scans all dependencies for known vulnerabilities using OWASP Dependency-Check
- Checks against the National Vulnerability Database (NVD)
- Fails the build if any high-severity vulnerability (CVSS ≥ 7.0) is found
- Uploads detailed security reports

### Workflow Triggers

The workflow automatically runs when:
- A pull request is created targeting the `master` branch
- A pull request targeting `master` is updated (new commits, title changes, etc.)

### Viewing Workflow Results

1. **On the Pull Request**: Check the "Checks" tab at the bottom of the PR page
2. **GitHub Actions Tab**: Navigate to the "Actions" tab in your repository
3. **Artifacts**: Download test reports and security scan results from the workflow run

### Configuration

#### Java Versions
Currently tests on: Java 17, 21, 25
To add/remove versions, modify the `matrix.java` array in the workflow file.

#### Security Threshold
The build fails if any dependency has a CVSS score ≥ 7.0 (high severity).
To adjust, modify `failBuildOnCVSS` in `build.gradle`:
```gradle
dependencyCheck {
    failBuildOnCVSS = 7.0  // Change this value as needed
}
```

### Branch Protection (Recommended)

To enforce that PRs must pass CI before merging:

1. Go to GitHub repository → Settings → Branches
2. Click "Add branch protection rule"
3. Select `master` branch
4. Enable:
   - ✅ "Require status checks to pass before merging"
   - ✅ "Require branches to be up to date before merging"
   - ✅ Select "Build & Test (Java 17)", "Build & Test (Java 21)", "Build & Test (Java 25)", and "Security Scanning"

### Troubleshooting

#### Workflow Not Running
- Ensure the PR is targeting the `master` branch
- Check that the workflow file is in `.github/workflows/` directory

#### Security Scan Failures
- Check the uploaded `security-scan-results.html` artifact for details
- If it's a false positive, you can suppress it by creating a suppression file:
  ```gradle
  dependencyCheck {
      suppressionFile = 'dependency-check-suppressions.xml'
  }
  ```

#### Build Failures
- Check the test reports in the uploaded artifacts
- View the workflow logs in the GitHub Actions tab

### Performance

The workflow uses several optimizations:
- **Gradle Caching**: Dependencies are cached between runs for faster builds
- **Matrix Strategy**: All Java versions test in parallel
- **Conditional Execution**: Security scan only runs if builds pass

### Cost

This workflow uses GitHub Actions free tier:
- 2000 free minutes per month for public repositories
- 2000 free minutes per month for private repositories
- Each job typically takes 3-5 minutes

### Next Steps

1. **Create a PR** to `master` branch to test the workflow
2. **Verify** all checks pass
3. **Enable branch protection** (recommended)
4. **Monitor** security scan results regularly