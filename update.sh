#!/bin/bash
set -e

# Sync fork's master with upstream on GitHub (same as the "Sync fork" button)
gh api repos/gnoling/Suwayomi-Server/merge-upstream -f branch=master

git checkout master
git pull
git checkout local-regex
git merge master --no-edit
git push
