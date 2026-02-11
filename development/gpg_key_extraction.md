# Extracting GPG Keys for GitHub Secrets

## Commands to Extract GPG Keys

### 1. Export Secret Key
```bash
# List your GPG keys to find your key ID
gpg --list-secret-keys

# Export the secret key (replace 799A99750C819FB915ECDBBC144D9369E0328F75 with your key ID)
gpg --export-secret-keys --armor 799A99750C819FB915ECDBBC144D9369E0328F75 > secret_key.asc

# Copy the content (everything between -----BEGIN PGP PRIVATE KEY BLOCK----- and -----END PGP PRIVATE KEY BLOCK-----)
# This is your JRELEASER_GPG_SECRET_KEY value
cat secret_key.asc
```

### 2. Export Public Key
```bash
# Export the public key (replace 799A99750C819FB915ECDBBC144D9369E0328F75 with your key ID)
gpg --export --armor 799A99750C819FB915ECDBBC144D9369E0328F75 > public_key.asc

# Copy the content (everything between -----BEGIN PGP PUBLIC KEY BLOCK----- and -----END PGP PUBLIC KEY BLOCK-----)
# This is your JRELEASER_GPG_PUBLIC_KEY value
cat public_key.asc
```

### 3. Quick One-Liner to Copy to Clipboard (macOS)
```bash
# Copy secret key to clipboard
gpg --export-secret-keys --armor 799A99750C819FB915ECDBBC144D9369E0328F75 | pbcopy

# Copy public key to clipboard
gpg --export --armor 799A99750C819FB915ECDBBC144D9369E0328F75 | pbcopy
```

## Setting GitHub Secrets

1. Go to your GitHub repository
2. Navigate to **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. For `JRELEASER_GPG_PUBLIC_KEY`:
   - Name: `GPG_PUBLIC_KEY`
   - Value: Paste the entire content of `public_key.asc` (including the BEGIN/END lines)
5. For `JRELEASER_GPG_SECRET_KEY`:
   - Name: `GPG_SECRET_KEY`
   - Value: Paste the entire content of `secret_key.asc` (including the BEGIN/END lines)

## Notes

- **MEMORY mode** (which we're using) expects the actual key content, NOT base64-encoded
- The key content includes the header and footer lines (-----BEGIN/END PGP KEY BLOCK-----)
- Copy the entire file content, not just the key itself
- If your GPG key has a passphrase, set `JRELEASER_GPG_PASSPHRASE` to that passphrase instead of empty string