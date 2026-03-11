import os
import glob
import subprocess
import tempfile
import re
from typing import Optional

def find_output_file(output_dir: str, scene_name: str, ext: str = "mp4") -> Optional[str]:
    """Manim nests output: media/videos/tmpXXX/480p15/SceneName.mp4"""
    pattern = os.path.join(output_dir, "**", f"{scene_name}*.{ext}")
    matches = glob.glob(pattern, recursive=True)
    if matches:
        # Return the most recently created file
        return max(matches, key=os.path.getctime)
    return None

def render_preview(code: str) -> str:
    """
    Writes the code to a temp .py file, runs manim, 
    and returns the absolute path to the produced video.
    """
    # 1. Parse scene name
    match = re.search(r'class\s+(\w+)\s*\(', code)
    scene_name = match.group(1) if match else "Animation"
    
    output_dir = os.path.abspath(os.getenv("MANIM_OUTPUT_DIR", "./outputs"))
    os.makedirs(output_dir, exist_ok=True)

    # 2. Create temporary file
    with tempfile.NamedTemporaryFile(delete=False, suffix=".py", mode="w", encoding="utf-8") as tmp:
        tmp.write(code)
        tmp_path = tmp.name

    try:
        # 3. Run manim with -ql (low quality)
        cmd = [
            "manim", "-ql",
            "--format", "mp4",
            "--media_dir", output_dir,
            tmp_path, scene_name,
            "--progress_bar", "none"
        ]
        
        print(f"[preview_renderer] Executing: {' '.join(cmd)}")
        
        # 4. Wait for process with 60s timeout
        result = subprocess.run(
            cmd, 
            capture_output=True, 
            text=True, 
            timeout=60,
            check=True
        )
        
        # 5. Search for the output file
        video_path = find_output_file(output_dir, scene_name)
        
        if not video_path or not os.path.exists(video_path):
            error_msg = f"Video file not found after rendering. Output dir: {output_dir}. Scene: {scene_name}"
            print(f"[preview_renderer] Error: {error_msg}")
            print(f"[preview_renderer] Stdout: {result.stdout}")
            print(f"[preview_renderer] Stderr: {result.stderr}")
            raise RuntimeError(error_msg)
            
        return os.path.abspath(video_path)

    except subprocess.TimeoutExpired:
        print("[preview_renderer] Timeout expired (60s)")
        raise RuntimeError("Preview rendering timed out (60s)")
    except subprocess.CalledProcessError as e:
        print(f"[preview_renderer] Manim failed with exit code {e.returncode}")
        print(f"[preview_renderer] Stdout: {e.stdout}")
        print(f"[preview_renderer] Stderr: {e.stderr}")
        raise RuntimeError(f"Manim rendering failed: {e.stderr}")
    finally:
        if os.path.exists(tmp_path):
            os.remove(tmp_path)
