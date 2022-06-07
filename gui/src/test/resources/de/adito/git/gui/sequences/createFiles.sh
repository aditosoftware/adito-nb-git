#!/bin/sh

mkdir -p "$1"
cp EqualLineConflict/Original "$1"/Original
cp EqualLineConflict/Original "$1"/VersionA
cp EqualLineConflict/Original "$1"/VersionB
cp EqualLineConflict/Original "$1"/Expected

git add "$1"/Original
git add "$1"/VersionA
git add "$1"/VersionB
git add "$1"/Expected