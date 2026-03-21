import base64, glob, hashlib, os, re, subprocess, tempfile
from pathlib import Path

OUTPUTS_DIR = Path(__file__).parent.parent / "outputs"
OUTPUTS_DIR.mkdir(exist_ok=True)

def render_preview(code: str) -> dict:
    code = code.strip()
    match = re.search(r'class\s+(\w+)\s*\(', code)
    if not match:
        raise ValueError("No Scene class found in code")
    class_name = match.group(1)

    # Use a fresh temp dir every preview so state never bleeds between tasks
    with tempfile.TemporaryDirectory() as tmpdir:
        code_file = os.path.join(tmpdir, "scene.py")
        with open(code_file, "w", encoding="utf-8") as f:
            f.write(code)

        cmd = [
            "manim", "-ql",
            "--format", "mp4",
            "--media_dir", tmpdir,
            "--progress_bar", "none",
            code_file,
            class_name,
        ]

        result = subprocess.run(
            cmd, cwd=tmpdir,
            capture_output=True, text=True, timeout=120,
        )

        if result.returncode != 0:
            raise RuntimeError(result.stderr[-3000:])

        matches = glob.glob(os.path.join(tmpdir, "**", "*.mp4"), recursive=True)
        if not matches:
            raise RuntimeError(
                f"No output file found.\nSTDOUT:{result.stdout[-1000:]}\nSTDERR:{result.stderr[-1000:]}"
            )

        video_path = max(matches, key=os.path.getctime)
        with open(video_path, "rb") as f:
            video_bytes = f.read()

        return {
            "video": base64.b64encode(video_bytes).decode(),
            "mimeType": "video/mp4",
            "className": class_name,
            "size": len(video_bytes),
        }
