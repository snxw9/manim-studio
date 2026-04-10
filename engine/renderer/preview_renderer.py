import base64
import glob
import os
import re
import subprocess
import tempfile


def render_preview(code: str) -> dict:
    code = code.strip()
    if not code:
        raise ValueError("No code provided")

    match = re.search(r'class\s+(\w+)\s*\(', code)
    if not match:
        raise ValueError("No Scene class found in code")
    class_name = match.group(1)

    with tempfile.TemporaryDirectory() as tmpdir:
        code_file = os.path.join(tmpdir, "scene.py")
        with open(code_file, "w", encoding="utf-8") as f:
            f.write(code)

        cmd = [
            "manim",
            "-ql",
            "--format", "mp4",
            "--media_dir", tmpdir,
            "--progress_bar", "none",
            code_file,
            class_name,
        ]

        # Safety check — catch any non-string sneaking in
        for i, item in enumerate(cmd):
            if not isinstance(item, str):
                raise TypeError(
                    f"cmd[{i}] is {type(item).__name__} = {repr(item)}, must be str"
                )

        print(f"[preview] CMD: {cmd}")

        try:
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                timeout=90,
            )
        except subprocess.TimeoutExpired:
            raise RuntimeError("Preview timed out")
        except FileNotFoundError:
            raise RuntimeError("manim not found in PATH")

        if result.returncode != 0:
            raise RuntimeError(f"Manim error:\n{result.stderr[-3000:]}")

        matches = glob.glob(
            os.path.join(tmpdir, "**", "*.mp4"),
            recursive=True,
        )

        if not matches:
            raise RuntimeError(
                f"No mp4 produced.\nSTDOUT:{result.stdout[-500:]}\nSTDERR:{result.stderr[-500:]}"
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