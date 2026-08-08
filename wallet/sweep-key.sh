#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Usage:
  sweep-key.sh <private-key-wif-or-bip38> [--testnet]

Description:
  Opens Bitcoin Wallet's "Sweep wallet" screen on a connected Android device and
  pre-fills it with the provided private key. Confirming the sweep in the app
  sends all spendable coins from that key into your wallet.
EOF
}

if [[ ${1:-} == "-h" || ${1:-} == "--help" || $# -lt 1 || $# -gt 2 ]]; then
  usage
  exit 1
fi

PRIVATE_KEY="$1"
PACKAGE_NAME="de.schildbach.wallet"

if [[ ${2:-} == "--testnet" ]]; then
  PACKAGE_NAME="de.schildbach.wallet_test"
elif [[ $# -eq 2 ]]; then
  usage
  exit 1
fi

adb shell am start \
  -n "${PACKAGE_NAME}/de.schildbach.wallet.ui.send.SweepWalletActivity" \
  --es sweep_key "${PRIVATE_KEY}" >/dev/null

echo "Sweep screen opened. Review and confirm the sweep in the app."
