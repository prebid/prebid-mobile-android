#! /bin/bash

#################################
# Update Maven Release folder
#################################

# Merge Script
if [ -d "Maven" ]; then
  cd Maven/
fi

set -euo pipefail

function echoX() {
  echo -e "PREBID DEPLOY-LOG: $@"
}

####################################################
# USAGE
####################################################

usage(){ cat <<'EOF'
Usage:
  deployPrebidMobile.sh --version <x.y.z | x.y.z-SNAPSHOT>

Examples:
  ./deployPrebidMobile.sh --version x.y.z
  ./deployPrebidMobile.sh --version x.y.z-SNAPSHOT
EOF
}

####################################################
# SCRIPT CONFIGURATION (VERSION, DEPLOY URL, PATH)
####################################################

VERSION="${RELEASE_VERSION:-}"

while [[ $# -gt 0 ]]; do
  case "$1" in
    -v|--version) VERSION="${2:-}"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echoX "Unknown arg: $1"; usage; exit 1 ;;
  esac
done

[[ -z "${VERSION}" ]] && { echoX "ERROR: version is required."; usage; exit 1; }

IS_SNAPSHOT=false
if [[ "$VERSION" == *-SNAPSHOT ]]; then
  IS_SNAPSHOT=true
fi

if ! $IS_SNAPSHOT; then
  if ! [[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echoX "WARNING: '$VERSION' doesn't look like x.y.z"; usage; exit 1;
  fi
fi

NAMESPACE="org.prebid"

RELEASE_URL="https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/"
SNAPSHOT_URL="https://central.sonatype.com/repository/maven-snapshots/"
REPO_ID="central"

DEPLOY_URL="${RELEASE_URL}"
if $IS_SNAPSHOT; then
  DEPLOY_URL="${SNAPSHOT_URL}"
fi

BASE_DIR="$PWD"
DEPLOY_DIR_NAME="filesToDeploy"
DEPLOY_DIR_ABSOLUTE="$BASE_DIR/$DEPLOY_DIR_NAME"

rm -r "$DEPLOY_DIR_ABSOLUTE" || true
mkdir "$DEPLOY_DIR_ABSOLUTE"

cd ..
bash ./buildPrebidMobile.sh

cp -r ../generated/* "$DEPLOY_DIR_ABSOLUTE" || true

####################################################
# HELPER FUNCTIONS
####################################################

# -------------------------------------------------
# Deploy POM and artifacts to Maven via DEPLOY_URL
#
# mavenDeploy [pom_path] [artifact_path] [sources_path] [javadoc_path] [deploy_url]
# -------------------------------------------------
function mavenDeploy () {
  local POM="$1" FILE="$2" SOURCE="$3" JDOC="$4" URL="$5"

  MAVEN_GPG_PASSPHRASE="$GPG_PASSPHRASE" mvn -B gpg:sign-and-deploy-file \
    -DrepositoryId="${REPO_ID}" \
    -Durl="${URL}" \
    -DpomFile="${POM}" \
    -Dfile="${FILE}" \
    -Dsources="${SOURCE}" \
    -Djavadoc="${JDOC}" \
    -Dgpg.keyname="${GPG_KEYNAME}" \
    -Dgpg.executable=gpg \
    -Dgpg.homedir="$HOME/.gnupg" \
    "-DgpgArguments=--pinentry-mode loopback"
}

# -------------------------------------------------
# Replace version in the POM file based on IS_SNAPSHOT condition
#
# replace_version_placeholder [pom_path] [revision]
# -------------------------------------------------
function replace_version_placeholder() {
  local ORIGINAL_POM="$1"; local REVISION="$2";
  local MODIFIED_POM="$(dirname "$ORIGINAL_POM")/pom/pom.xml"
  mkdir -p "$(dirname "$ORIGINAL_POM")/pom"

  awk -v VER="$REVISION" '
    BEGIN { inParent=0; projectVersionDone=0 }
    {
      if ($0 ~ /<parent>/)   inParent=1
      if ($0 ~ /<\/parent>/) inParent=0

      gsub(/<revision>[[:space:]]*[^<]*[[:space:]]*<\/revision>/, "<revision>" VER "</revision>")
      gsub(/<version>[[:space:]]*\$\{revision\}[[:space:]]*<\/version>/, "<version>" VER "</version>")
      gsub(/<version>[[:space:]]*\$\{project\.version\}[[:space:]]*<\/version>/, "<version>" VER "</version>")

      if (!inParent && !projectVersionDone && match($0, /<version>[^<]+<\/version>/)) {
        pv = substr($0, RSTART, RLENGTH)
        if (pv ~ /\$\{revision\}|\$\{project\.version\}/) {
          sub(/<version>[^<]+<\/version>/, "<version>" VER "</version>")
        }
        projectVersionDone=1
      }
      print
    }
  ' "$ORIGINAL_POM" > "$MODIFIED_POM"
  echo "$MODIFIED_POM"
}

# -------------------------------------------------
# Reading the creds for the Maven Central from the ~/.m2/settings.xml
#
# load_maven_central_creds [serverId] [settingsPath]
# -------------------------------------------------
function load_maven_central_creds() {
  local SERVER_ID="${1:-central}"
  local SETTINGS_PATH="${2:-$HOME/.m2/settings.xml}"

  if [[ ! -f "$SETTINGS_PATH" ]]; then
    echoX "Maven Central settings not found: $SETTINGS_PATH" >&2
    return 2
  fi

  if [[ -n "${CENTRAL_USERNAME:-}" && -n "${CENTRAL_PASSWORD:-}" ]]; then
    return 0
  fi

  local EXPORTS;
  
  EXPORTS="$(
    python3 - "$SERVER_ID" "$SETTINGS_PATH" <<'PY'
import sys, os, xml.etree.ElementTree as ET, shlex
sid = sys.argv[1]
path = os.path.expanduser(sys.argv[2])

def warn(msg):
    sys.stderr.write(f"PREBID DEPLOY-LOG: {msg}\n")
    sys.stderr.flush()

try:
    tree = ET.parse(path)
except Exception as e:
    warn(f"Cannot read Maven Central settings at {path}: {e}")
    sys.exit(2)

root = tree.getroot()

for el in root.iter():
    if '}' in el.tag:
        el.tag = el.tag.split('}', 1)[1]

def text(elem, name):
    x = elem.find(name)
    return (x.text or '').strip() if x is not None else ''

servers = root.findall(".//servers/server")
matches = [s for s in servers if text(s, "id") == sid]

if not matches:
    warn(f'Server id "{sid}" not found in {path}')
    sys.exit(3)
if len(matches) > 1:
    warn(f'Multiple <server> entries with id "{sid}" found; using the first')

srv = matches[0]
user = text(srv, "username")
pwd  = text(srv, "password")

if not user:
    warn(f'Username empty for server id "{sid}"')
    sys.exit(4)
if not pwd:
    warn(f'Password empty for server id "{sid}"')
    sys.exit(5)

print("export CENTRAL_USERNAME=" + shlex.quote(user))
print("export CENTRAL_PASSWORD=" + shlex.quote(pwd))

PY
  )" || return $?

  eval "$EXPORTS"
}

# -------------------------------------------------
# Finalize artifacts to the Maven Central Portal (releases only)
#
# finalizeUploadToPortal
# -------------------------------------------------
function finalizeUploadToPortal() {
  echoX "Finalizing upload to Central Publisher Portal (namespace: ${NAMESPACE})"

  load_maven_central_creds
  echoX "${CENTRAL_USERNAME:?CENTRAL_USERNAME not set}" : "${CENTRAL_PASSWORD:?CENTRAL_PASSWORD not set}"

  local TOKEN
  TOKEN=$(printf "%s:%s" "$CENTRAL_USERNAME" "$CENTRAL_PASSWORD" | base64 | tr -d '\n')

  curl -sSf -X POST \
    "https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/${NAMESPACE}?publishing_type=user_managed" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Length: 0"
}

####################################################
# DEPLOYMENT
####################################################

# Read GPG to sign the artifacts
GPG_PASSPHRASE=""

function setupGPG() {
  if [[ -z "${GPG_KEYNAME:-}" ]]; then
    echoX "GPG_KEYNAME env var is not set. Take the 'sec xxxxxxx/GPG_KEYNAME' from the found keys below:" >&2
    gpg --list-secret-keys --keyid-format=long
    return 2
  fi

  read -r -s -p "GPG passphrase for '${GPG_KEYNAME}': " GPG_PASSPHRASE
  echoX $GPG_PASSPHRASE

  export GPG_TTY=${GPG_TTY:-$(tty || true)}
}

setupGPG

# Deploy each module one-by-one
modules=("PrebidMobile-core" "PrebidMobile" "PrebidMobile-gamEventHandlers" "PrebidMobile-admobAdapters" "PrebidMobile-maxAdapters")
extensions=("aar" "jar" "jar" "jar" "jar")

for n in ${!modules[@]}; do
  echo -e "\n"
  echoX "Deploying ${modules[$n]} on Maven..."

  extension="${extensions[$n]}"
  module="${modules[$n]}"
  if [ "$extension" == "aar" ]; then
    compiledPath=$"$DEPLOY_DIR_ABSOLUTE/aar/${module}-release.aar"
  else
    compiledPath=$"$DEPLOY_DIR_ABSOLUTE/${module}.jar"
  fi
  
  # Configure the final POM file with correct version
  pom="$(replace_version_placeholder "${BASE_DIR}/${module}-pom.xml" "${VERSION}" "${module}")"

  mavenDeploy $"$pom" "$compiledPath" $"$DEPLOY_DIR_ABSOLUTE/${module}-sources.jar" $"$DEPLOY_DIR_ABSOLUTE/${module}-javadoc.jar" "${DEPLOY_URL}"
done

# Reset variables and temp data
unset GPG_PASSPHRASE
rm -rf pom

# Notify about next steps
if $IS_SNAPSHOT; then
  echoX "$VERSION published to the Central snapshots repo"
  echoX "Consumers can add: https://central.sonatype.com/repository/maven-snapshots/"
else
  finalizeUploadToPortal

  echoX "Release uploaded to Central Portal as VALIDATED"
  echoX "Open: https://central.sonatype.com/publishing/deployments to publish/drop"
fi

echoX "End Script"