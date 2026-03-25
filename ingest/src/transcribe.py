#!/usr/bin/env python3
"""
Trabotto — Bull Video Transcription Pipeline
Uses Whisper (local) to FLAG_THRESHOLD Bull's YouTube videos.
Produces: full transcript + flagged low-confidence segments for manual review.

Usage:
  # Single video
  python transcribe.py https://www.youtube.com/watch?v=VIDEO_ID

  # Entire playlist
  python transcribe.py https://www.youtube.com/playlist?list=PLAYLIST_ID

  # Local audio/video file
  python transcribe.py /path/to/video.mp4

Requirements:
  pip install openai-whisper yt-dlp

Whisper models (pick based on your hardware):
  - "base"   → fast, decent quality, ~1GB VRAM
  - "small"  → good balance, ~2GB VRAM
  - "medium" → recommended for Italian, ~5GB VRAM
  - "large"  → best quality, ~10GB VRAM, slow without GPU
"""

import argparse
import json
import os
import subprocess
import sys
from pathlib import Path

# Confidence threshold for flagging segments.
# Whisper avg_logprob: closer to 0 = more confident, very negative = less confident.
# -0.7 is a reasonable threshold; adjust based on results.
FLAG_THRESHOLD = -0.4

# Whisper model. "medium" is best for Italian without a beefy GPU.
# Use "large" if you have a good GPU (RTX 3060+).
DEFAULT_MODEL = "medium"

# Output directory
OUTPUT_DIR = Path("knowledge/transcriptions")
FLAGS_DIR = Path("knowledge/transcriptions/flags")


def download_audio(url: str, output_dir: Path) -> list[Path]:
    """Download audio from YouTube video or playlist using yt-dlp."""
    output_dir.mkdir(parents=True, exist_ok=True)

    # yt-dlp template: playlist index + title
    template = str(output_dir / "%(playlist_index|0)03d_%(title)s.%(ext)s")

    cmd = [
        "yt-dlp",
        "--extract-audio",
        "--audio-format", "mp3",
        "--audio-quality", "0",
        "--output", template,
        "--restrict-filenames",       # safe filenames (no special chars)
        "--no-overwrites",
        "--cookies-from-browser", "chrome",
        "--remote-components", "ejs:github",
        url
    ]

    print(f"Downloading: {url}")
    subprocess.run(cmd, check=True)

    # Return all mp3 files in output dir, sorted
    return sorted(output_dir.glob("*.mp3"))


def transcribe_file(audio_path: Path, model_name: str) -> dict:
    """Transcribe a single audio file with Whisper, returning full result."""
    import whisper

    print(f"\nTranscribing: {audio_path.name} (model: {model_name})")
    print("This may take a few minutes...")

    model = whisper.load_model(model_name)
    result = model.transcribe(
        str(audio_path),
        language="it",
        verbose=False,
        word_timestamps=False
    )

    return result


def format_timestamp(seconds: float) -> str:
    """Convert seconds to MM:SS format."""
    minutes = int(seconds // 60)
    secs = int(seconds % 60)
    return f"{minutes}:{secs:02d}"


def save_transcript(result: dict, output_path: Path):
    """Save full transcript with timestamps."""
    with open(output_path, "w", encoding="utf-8") as f:
        for segment in result["segments"]:
            start = format_timestamp(segment["start"])
            text = segment["text"].strip()
            f.write(f"{start} {text}\n")

    print(f"Transcript saved: {output_path}")


def save_flags(result: dict, flags_path: Path, audio_name: str) -> int:
    """Extract and save low-confidence segments for manual review."""
    flags = []

    for segment in result["segments"]:
        avg_logprob = segment.get("avg_logprob", 0)
        no_speech_prob = segment.get("no_speech_prob", 0)

        # Flag if: low confidence OR high probability it's not speech
        if avg_logprob < FLAG_THRESHOLD or no_speech_prob > 0.5:
            flags.append({
                "timecode": f"{format_timestamp(segment['start'])}-{format_timestamp(segment['end'])}",
                "text": segment["text"].strip(),
                "confidence": round(avg_logprob, 3),
                "no_speech_prob": round(no_speech_prob, 3)
            })

    if flags:
        flags_path.parent.mkdir(parents=True, exist_ok=True)
        with open(flags_path, "w", encoding="utf-8") as f:
            f.write(f"# Low-confidence segments for: {audio_name}\n")
            f.write(f"# Total flagged: {len(flags)}\n")
            f.write(f"# Review these by listening to the video at the given timecodes.\n")
            f.write(f"# Fix the text directly in the main transcript file after review.\n\n")

            for flag in flags:
                f.write(f"[{flag['timecode']}] (confidence: {flag['confidence']})\n")
                f.write(f"  \"{flag['text']}\"\n\n")

        print(f"Flagged {len(flags)} segments → {flags_path}")
    else:
        print(f"No low-confidence segments found.")

    return len(flags)


def process_single(audio_path: Path, model_name: str):
    """Process a single audio file: transcribe + flag."""
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    # Extract playlist index from filename (first part before _)
    stem = audio_path.stem
    index = stem.split("_")[0]  # "001" from "001_title_here"
    prefix = model_name.lower()

    transcript_path = OUTPUT_DIR / f"{index}-{prefix}.txt"
    flags_path = FLAGS_DIR / f"{index}-{prefix}-flags.txt"

    result = transcribe_file(audio_path, model_name)
    save_transcript(result, transcript_path)
    num_flags = save_flags(result, flags_path, audio_path.name)

    return transcript_path, num_flags


def main():
    parser = argparse.ArgumentParser(description="Trabotto — Bull Video Transcription Pipeline")
    parser.add_argument("source", help="YouTube URL (video or playlist) or local audio/video file path")
    parser.add_argument("--model", default=DEFAULT_MODEL, help=f"Whisper model (default: {DEFAULT_MODEL})")
    parser.add_argument("--threshold", type=float, default=FLAG_THRESHOLD,
                        help=f"Confidence threshold for flagging (default: {FLAG_THRESHOLD})")
    parser.add_argument("--keep-audio", action="store_true", help="Keep downloaded audio files")

    args = parser.parse_args()

    flag_threshold = args.threshold

    source = args.source
    total_flags = 0

    if source.startswith("http"):
        # YouTube URL — download first
        tmp_dir = Path("tmp_audio")
        audio_files = download_audio(source, tmp_dir)

        if not audio_files:
            print("No audio files downloaded. Check the URL.")
            sys.exit(1)

        print(f"\nDownloaded {len(audio_files)} files. Starting transcription...\n")

        for audio_path in audio_files:
            _, num_flags = process_single(audio_path, args.model)
            total_flags += num_flags

        if not args.keep_audio:
            import shutil
            shutil.rmtree(tmp_dir)
            print(f"\nCleaned up temporary audio files.")

    else:
        # Local file
        audio_path = Path(source)
        if not audio_path.exists():
            print(f"File not found: {audio_path}")
            sys.exit(1)

        _, num_flags = process_single(audio_path, args.model)
        total_flags = num_flags

    print(f"\n{'='*60}")
    print(f"DONE. Total flagged segments across all videos: {total_flags}")
    print(f"Transcripts: {OUTPUT_DIR}/")
    if total_flags > 0:
        print(f"Flags to review: {FLAGS_DIR}/")
    print(f"{'='*60}")


if __name__ == "__main__":
    main()