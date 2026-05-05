import base64
import glob
import hashlib
import os
import re
import shutil
import subprocess
import tempfile
from pathlib import Path

ENGINE_DIR = Path(__file__).parent.parent
OUTPUTS_DIR = ENGINE_DIR / "outputs"
LATEX_CACHE_DIR = ENGINE_DIR / "latex_cache"

OUTPUTS_DIR.mkdir(exist_ok=True)
LATEX_CACHE_DIR.mkdir(exist_ok=True)

QUALITY_MAP = {
    "480p":  "-ql",
    "720p":  "-qm",
    "1080p": "-qh",
    "2160p": "-qk",
}

QUALITY_TIMEOUTS = {
    "-ql": 90,
    "-qm": 180,
    "-qh": 300,
    "-qk": 600,
}

QUALITY_RESOLUTION = {
    "-ql": "854,480",
    "-qm": "1280,720",
    "-qh": "1920,1080",
    "-qk": "3840,2160",
}

def pre_validate(code: str) -> str | None:
    import ast
    try:
        ast.parse(code)
    except SyntaxError as e:
        return f"Syntax error at line {e.lineno}: {e.msg}"
    if "def construct" not in code:
        return "Missing construct(self) method"
    if not re.search(r'class\s+\w+\s*\(', code):
        return "No Scene class found"
    return None

def render_scene(code: str, quality: str = "720p", fmt: str = "mp4") -> dict:
    # Validate quality
    q_flag = QUALITY_MAP.get(quality, "-qm")
    print(f"[render] quality={quality!r} -> flag={q_flag}")

    # Get scene class name
    match = re.search(r'class\s+(\w+)\s*\(', code)
    if not match:
        raise ValueError("No Scene class found in code")
    class_name = match.group(1)
    print(f"[render] class={class_name}")

    fmt_actual = "mp4" if fmt == "mov" else fmt
    timeout = QUALITY_TIMEOUTS.get(q_flag, 180)
    resolution = QUALITY_RESOLUTION.get(q_flag, "1280,720")

    # Use a FRESH local temp dir for every render to avoid system temp path issues
    render_id = hashlib.md5(f"{code}{quality}{fmt}{os.urandom(8)}".encode()).hexdigest()[:12]
    tmpdir = OUTPUTS_DIR / f"tmp_{render_id}"
    tmpdir.mkdir(parents=True, exist_ok=True)
    
    try:
        # Write scene file
        code_file = tmpdir / "scene.py"
        code_file.write_text(code, encoding="utf-8")

        # media_dir is the local temp dir for VIDEO output
        cmd = [
            "manim",
            q_flag,
            "--fps", "30",
            "--resolution", resolution,
            "--format", fmt_actual,
            "--media_dir", str(tmpdir),     # local to project
            "--disable_caching",            # never reuse video cache
            "--progress_bar", "none",
            str(code_file),
            class_name,
        ]

        # Validate all args are strings
        for i, item in enumerate(cmd):
            if not isinstance(item, str):
                raise TypeError(f"cmd[{i}] is {type(item).__name__}: {repr(item)}")

        print(f"[render] timeout={timeout}s")

        env = os.environ.copy()
        env["MANIM_TEX_DIR"] = str(LATEX_CACHE_DIR)

        try:
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                timeout=timeout,
                env=env,
            )
        except subprocess.TimeoutExpired:
            raise RuntimeError(
                f"Render timed out after {timeout}s at {quality}.\n"
                f"Try a lower quality setting or a simpler animation."
            )
        except FileNotFoundError:
            raise RuntimeError(
                "manim not found. Make sure it is installed in the venv."
            )

        if result.returncode != 0:
            raise RuntimeError(
                f"Manim error:\n{result.stderr[-3000:]}"
            )

        # Find the output file — search recursively in temp dir
        patterns = [
            os.path.join(tmpdir, "**", f"{class_name}.{fmt_actual}"),
            os.path.join(tmpdir, "**", f"*.{fmt_actual}"),
        ]
        matches = []
        for pattern in patterns:
            matches = glob.glob(pattern, recursive=True)
            if matches:
                break

        if not matches:
            raise RuntimeError(
                f"No .{fmt_actual} file found after render.\n"
                f"STDERR: {result.stderr[-1000:]}"
            )

        video_path = max(matches, key=os.path.getctime)
        print(f"[render] output={video_path}")

        # Build friendly filename
        spaced = re.sub(r'(?<=[a-z])(?=[A-Z])', ' ', class_name)
        spaced = spaced.replace(' Scene', '').replace('Scene', '').strip()
        friendly = re.sub(r'[<>:"/\\|?*]', '', f"{spaced} ({quality}).{fmt_actual}")

        # Copy to outputs dir for reference
        output_path = OUTPUTS_DIR / friendly
        shutil.copy2(video_path, output_path)

        # Read and return as base64
        with open(video_path, "rb") as f:
            video_bytes = f.read()

        return {
            "video": base64.b64encode(video_bytes).decode(),
            "mimeType": f"video/{fmt_actual}",
            "className": class_name,
            "filename": friendly,
            "size": len(video_bytes),
        }
    finally:
        shutil.rmtree(tmpdir, ignore_errors=True)
