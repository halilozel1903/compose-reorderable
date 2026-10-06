#!/usr/bin/env bash
# Captures README screenshots of the sample app on a running emulator.
# A drag can't be performed reliably through adb, so the sample opens each scene from the `scene` extra and drives the
# reorder state from code (startDrag/dragBy, startKeyboardReorder/move) into a fixed state: an item lifted mid-drag
# with a gap where it will land, or an item in keyboard reorder mode. Every capture is checked for the expected text
# and for a blank image.
#
#   bash scripts/screenshots.sh tablet   # pixel_tablet in landscape: the widgets grid mid-drag, light and dark
#   bash scripts/screenshots.sh phone    # pixel_7: the playlist mid-drag and in keyboard reorder mode, light and dark
set -euo pipefail
source "$(dirname "$0")/screenshot-lib.sh"

device="${1:-phone}"

# The text each scene must show; the capture fails without it.
# "Moving ..." only appears while an item is lifted, "Enter to drop" only in keyboard reorder mode.
expected_text() {
  case "$1" in
    list) echo "Moving" ;;
    grid) echo "Moving" ;;
    dpad) echo "Enter to drop" ;;
  esac
}

suffix() {
  if [ "$1" = dark ]; then echo "-dark"; else echo ""; fi
}

install_sample
if [ "$device" = tablet ]; then
  ensure_landscape
  for mode in light dark; do
    set_night_mode "$mode"
    fresh_launch --es scene grid
    capture "tablet-grid$(suffix "$mode")" "$(expected_text grid)"
  done
else
  for mode in light dark; do
    set_night_mode "$mode"
    for scene in list dpad; do
      fresh_launch --es scene "$scene"
      capture "phone-$scene$(suffix "$mode")" "$(expected_text "$scene")"
    done
  done
fi
