#!/bin/bash
# Merge upstream Suwayomi into local-regex, build the server jar locally, and deploy it to gum.
#
#   update-build-deploy.sh                      merge + push + build + deploy (run inside a checkout)
#   update-build-deploy.sh --no-deploy --jar-out FILE
#                                               merge + push + build, copy the jar to FILE
#   update-build-deploy.sh --deploy-jar FILE    only deploy FILE to gum
#
# Options: --upstream REF   upstream ref or commit to merge (default: master)
#          --no-push        don't push the merged branch to the fork or to Forgejo
#
# Works in any checkout of the repo (this working copy, or updateapps' clone in
# ~/build/updateapps-tmp/suwayomi), using explicit URLs rather than remote names. Pushing goes over
# HTTPS with gh's token, so no ssh-agent is needed.
set -euo pipefail

UPSTREAM_URL=https://github.com/Suwayomi/Suwayomi-Server.git
FORK_REPO=gnoling/Suwayomi-Server
FORK_URL=https://github.com/$FORK_REPO.git
# Forgejo on gum (LAN only) is the main home since 2026-11-20; GitHub stays the shared copy. Push to both.
FORGEJO_URL=ssh://git@git.gum.0s.rip:2222/Garret/Suwayomi-Server.git
BRANCH=local-regex
JDK=/usr/lib/jvm/java-25-openjdk-amd64
GUM=gum
GUM_DIR=/home/garret/docker/suwayomi
CONTAINER=suwayomi-suwayomi-1

deploy=1 push=1 jar_out="" deploy_jar="" upstream_ref=master

die() { echo "ERROR: $*" >&2; exit 1; }
step() { echo "==> $*"; }
gitc() { git -c credential.helper= -c 'credential.helper=!gh auth git-credential' "$@"; }

while [ $# -gt 0 ]; do
    case "$1" in
        --no-deploy) deploy=0 ;;
        --no-push) push=0 ;;
        --jar-out) jar_out=$2; shift ;;
        --deploy-jar) deploy_jar=$2; shift ;;
        --upstream) upstream_ref=$2; shift ;;
        -h | --help) sed -n '2,15p' "$0"; exit 0 ;;
        *) die "unknown option: $1" ;;
    esac
    shift
done

deploy_to_gum() {
    local jar=$1
    [ -s "$jar" ] || die "no jar at $jar"
    local revision
    revision=$(unzip -p "$jar" META-INF/MANIFEST.MF | sed -n 's/^Implementation-Version: *\([^[:space:]]*\).*/\1/p')
    step "Deploying $(basename "$jar") ($revision) to $GUM"
    ssh "$GUM" "cp -f $GUM_DIR/Suwayomi-Server.jar $GUM_DIR/Suwayomi-Server.jar.old"
    scp -q "$jar" "$GUM:$GUM_DIR/Suwayomi-Server.jar.new"
    # Swap in place so the container never sees a half-copied jar
    ssh "$GUM" "mv -f $GUM_DIR/Suwayomi-Server.jar.new $GUM_DIR/Suwayomi-Server.jar && cd $GUM_DIR && docker compose restart"

    step "Waiting for the server to come back"
    local query='{"query":"{aboutServer{revision}}"}' running=""
    for _ in $(seq 1 60); do
        running=$(ssh "$GUM" "docker exec $CONTAINER curl -s -m 3 -H 'Content-Type: application/json' -d '$query' http://localhost:4567/api/graphql" 2>/dev/null |
            sed -n 's/.*"revision":"\([^"]*\)".*/\1/p') || true
        [ -n "$running" ] && break
        sleep 5
    done
    [ -n "$running" ] || die "server did not answer after restart (previous jar kept as Suwayomi-Server.jar.old)"
    [ -z "$revision" ] || [ "$running" = "$revision" ] || die "server reports $running, expected $revision"
    step "Server is running $running"
}

if [ -n "$deploy_jar" ]; then
    deploy_to_gum "$deploy_jar"
    exit 0
fi

cd "$(git rev-parse --show-toplevel)"
git diff --quiet && git diff --cached --quiet || die "uncommitted changes in $(pwd); commit or stash them first"

step "Syncing fork master with upstream"
gh api "repos/$FORK_REPO/merge-upstream" -f branch=master --jq .message || echo "   (fork master sync failed; continuing)"

step "Fetching upstream $upstream_ref and fork $BRANCH"
gitc fetch --quiet "$UPSTREAM_URL" "$upstream_ref"
upstream=$(git rev-parse FETCH_HEAD)
gitc fetch --quiet "$FORK_URL" "$BRANCH"
fork=$(git rev-parse FETCH_HEAD)

if git show-ref --verify --quiet "refs/heads/$BRANCH"; then
    git checkout --quiet "$BRANCH"
    # Pick up commits pushed from elsewhere; a local branch that is ahead (unpushed) is fine too
    git merge --quiet --ff-only "$fork" || die "local $BRANCH and the fork's $BRANCH have diverged"
else
    git checkout --quiet -B "$BRANCH" "$fork"
fi

if git merge-base --is-ancestor "$upstream" HEAD; then
    step "$BRANCH already contains upstream ${upstream:0:8}"
else
    step "Merging upstream ${upstream:0:8} into $BRANCH"
    if ! git merge --no-edit --quiet "$upstream"; then
        git merge --abort
        die "merge conflict with upstream ${upstream:0:8}; merge it by hand in a checkout of $BRANCH"
    fi
fi

if [ "$push" = 1 ] && [ "$(git rev-parse HEAD)" != "$fork" ]; then
    step "Pushing $BRANCH to $FORK_REPO"
    gitc push --quiet "$FORK_URL" "HEAD:refs/heads/$BRANCH"
fi
if [ "$push" = 1 ]; then
    step "Pushing $BRANCH to Forgejo"
    # Not fatal: Forgejo is only reachable on the LAN
    git push --quiet "$FORGEJO_URL" "HEAD:refs/heads/$BRANCH" || echo "   (Forgejo push failed; continuing)"
    if [ "$upstream_ref" = master ]; then
        git push --quiet "$FORGEJO_URL" "$upstream:refs/heads/master" || echo "   (Forgejo master push failed; continuing)"
    fi
fi

step "Building $(git log -1 --format='%h %s')"
[ -x "$JDK/bin/java" ] || die "JDK 25 not found at $JDK"
rm -f server/build/Suwayomi-Server-v*.jar
JAVA_HOME=$JDK ./gradlew :server:shadowJar --console=plain -q
jars=(server/build/Suwayomi-Server-v*.jar)
[ ${#jars[@]} -eq 1 ] && [ -s "${jars[0]}" ] || die "build produced no jar"
jar=$(realpath "${jars[0]}")
# (unzip -t falsely flags shadow jars as zip bombs; listing is enough to catch a broken archive)
unzip -l "$jar" >/dev/null || die "built jar is unreadable"
step "Built $jar"

if [ -n "$jar_out" ]; then
    cp -f "$jar" "$jar_out"
    step "Copied to $jar_out"
fi
if [ "$deploy" = 1 ]; then
    deploy_to_gum "$jar"
fi
