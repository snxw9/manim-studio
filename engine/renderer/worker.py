"""
Persistent Manim render worker.
Reads JSON jobs from stdin, writes JSON results to stdout.
Stays alive between renders — no cold start overhead.
"""
import sys
import json
import os
import base64
import glob
import re
import tempfile
import shutil
from pathlib import Path
import subprocess

MEDIA_DIR = Path(__file__).parent.parent / "media_cache"
MEDIA_DIR.mkdir(exist_ok=True)
OUTPUTS_DIR = Path(__file__).parent.parent / "outputs"
OUTPUTS_DIR.mkdir(exist_ok=True)

def get_friendly_name(class_name: str, quality: str, fmt: str) -> str:
    # Convert CamelCase to Title Case with spaces
    # WaterfallModel -> Waterfall Model
    # PythagoreanTheorem -> Pythagorean Theorem
    spaced = re.sub(r'(?<=[a-z])(?=[A-Z])', ' ', class_name)
    spaced = re.sub(r'(?<=[A-Z])(?=[A-Z][a-z])', ' ', spaced)
    # Remove "Scene" suffix if present
    spaced = spaced.replace(' Scene', '').replace('Scene', '').strip()
    return f"{spaced} ({quality}).{fmt}"

def render_job(job: dict) -> dict:
    code = job["code"]
    quality = job.get("quality", "720p")
    fmt = job.get("format", "mp4")
    fps = job.get("fps", 30)

    match = re.search(r'class\s+(\w+)\s*\(', code)
    if not match:
        return {"error": "No Scene class found"}
    class_name = match.group(1)

    quality_map = {
        "480p": "-ql", "720p": "-qm",
        "1080p": "-qh", "2160p": "-qk",
    }
    q_flag = quality_map.get(quality, "-qm")
    fmt_actual = "mp4" if fmt == "mov" else fmt

    with tempfile.TemporaryDirectory() as tmpdir:
        code_file = os.path.join(tmpdir, "scene.py")
        with open(code_file, "w", encoding="utf-8") as f:
            f.write(code)

        cmd = [
            sys.executable, "-m", "manim",
            q_flag,
            "--fps", str(fps),
            "--format", fmt_actual,
            "--media_dir", str(MEDIA_DIR),
            "--progress_bar", "none",
            code_file, class_name,
        ]

        result = subprocess.run(
            cmd, capture_output=True,
            text=True, timeout=300,
        )

        if result.returncode != 0:
            return {"error": result.stderr[-3000:]}

        # Manim puts output in media_dir/videos/...
        matches = glob.glob(
            os.path.join(str(MEDIA_DIR), "videos", "**", f"*.{fmt_actual}"),
            recursive=True,
        )
        # Also check tmpdir just in case
        matches += glob.glob(
            os.path.join(tmpdir, "**", f"*.{fmt_actual}"),
            recursive=True,
        )

        if not matches:
            return {"error": f"No output file found.\n{result.stderr[-500:]}"}

        video_path = max(matches, key=os.path.getctime)
        
        # After finding the output file, rename it into OUTPUTS_DIR:
        friendly = get_friendly_name(class_name, quality, fmt_actual)
        # Sanitize for filesystem
        friendly_safe = re.sub(r'[<>:"/\\|?*]', '', friendly)
        output_path = OUTPUTS_DIR / friendly_safe
        
        shutil.copy2(video_path, output_path)

        with open(output_path, "rb") as f:
            video_bytes = f.read()

        return {
            "video": base64.b64encode(video_bytes).decode(),
            "mimeType": f"video/{fmt_actual}",
            "className": class_name,
            "filename": friendly_safe,
            "size": len(video_bytes),
        }

def main():
    """Read JSON lines from stdin, write JSON results to stdout."""
    for line in sys.stdin:
        line = line.strip()
        if not line:
            continue
        try:
            job = json.loads(line)
            result = render_job(job)
        except Exception as e:
            result = {"error": str(e)}
        sys.stdout.write(json.dumps(result) + "\n")
        sys.stdout.flush()

if __name__ == "__main__":
    main()
