import os
import glob
import subprocess
import tempfile
import re
import hashlib
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
    Writes the code to a persistent file named by hash, runs manim, 
    and returns the absolute path to the produced video.
    """
    # 1. Parse scene name
    match = re.search(r'class\s+(\w+)\s*\(', code)
    scene_name = match.group(1) if match else "Animation"
    
    # 2. Setup persistent media cache
    MEDIA_CACHE = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "media_cache"))
    os.makedirs(MEDIA_CACHE, exist_ok=True)

    # 3. Use persistent file named by code hash
    code_hash = hashlib.md5(code.encode()).hexdigest()[:12]
    code_file = os.path.join(MEDIA_CACHE, f"scene_{code_hash}.py")
    with open(code_file, "w", encoding="utf-8") as f:
        f.write(code)

    try:
        # 4. Run manim with -ql (low quality) and caching enabled
        cmd = [
            "manim", "-ql",
            "--format", "mp4",
            "--media_dir", MEDIA_CACHE,
            code_file, scene_name,
            "--progress_bar", "none",
            "--disable_caching", "False"
        ]
        
        print(f"[preview_renderer] Executing: {' '.join(cmd)}")
        
        # 5. Wait for process with 60s timeout
        result = subprocess.run(
            cmd, 
            capture_output=True, 
            text=True, 
            timeout=60,
            check=True
        )
        
        # 6. Search for the output file in MEDIA_CACHE
        video_path = find_output_file(MEDIA_CACHE, scene_name)
        
        if not video_path or not os.path.exists(video_path):
            error_msg = f"Video file not found after rendering. Cache dir: {MEDIA_CACHE}. Scene: {scene_name}"
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
