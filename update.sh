#!/bin/bash
set -e

# Remotes: origin = Forgejo on gum (main home), github = the fork on GitHub (shared copy).

# Sync fork's master with upstream on GitHub (same as the "Sync fork" button)
gh api repos/gnoling/Suwayomi-Server/merge-upstream -f branch=master

git checkout master
git pull github master
git checkout local-regex
git merge master --no-edit

# Push to both
git push origin local-regex master
git push github local-regex
