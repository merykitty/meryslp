#!/bin/bash
echo "$(dirname "$(readlink -e "$0")")"
