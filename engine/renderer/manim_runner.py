import tempfile
import os
import subprocess
import re
import datetime
import glob
import shutil
import hashlib
import ast
from pathlib import Path

MEDIA_DIR = Path(__file__).parent.parent / "media_cache"

def pre_validate(code: str) -> str | None:
    """Returns error string or None if valid."""
    try:
        ast.parse(code)
    except SyntaxError as e:
        return f"Syntax error at line {e.lineno}: {e.msg}"
    
    if 'def construct' not in code:
        return "Missing construct(self) method"
    
    if not re.search(r'class\s+\w+\s*\(', code):
        return "No Scene class found"
    
    return None  # valid

def cleanup_previews(scene_name: str, output_dir: str):
    """Delete all preview files for a given scene name."""
    pattern = os.path.join(output_dir, f"{scene_name}_preview_*.mp4")
    for f in glob.glob(pattern):
        try:
            os.remove(f)
            print(f"Cleaned up preview: {f}")
        except Exception as e:
            print(f"Error cleaning up preview {f}: {e}")

def run_manim(code: str, preview: bool = False, quality: str = "720p", fmt: str = "mp4") -> str:
    # Pre-validate before calling Manim
    error = pre_validate(code)
    if error:
        raise ValueError(error)

    # 1. Parse scene name
    match = re.search(r'class\s+(\w+)\s*\(', code)
    scene_name = match.group(1) if match else "Animation"
    
    output_dir = os.path.abspath(os.getenv("MANIM_OUTPUT_DIR", "./outputs"))
    os.makedirs(output_dir, exist_ok=True)

    # 2. Handle preview cleanup if it's a final render
    if not preview:
        cleanup_previews(scene_name, output_dir)

    # 3. Handle persistent code file named by code hash
    MEDIA_CACHE = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "media_cache"))
    os.makedirs(MEDIA_CACHE, exist_ok=True)
    
    code_hash = hashlib.md5(code.encode()).hexdigest()[:12]
    code_file = os.path.join(MEDIA_CACHE, f"scene_{code_hash}.py")
    with open(code_file, "w", encoding="utf-8") as f:
        f.write(code)
    tmp_path = code_file
        
    # 4. Map quality to manim flags
    quality_map = {
        "480p": "-ql",
        "720p": "-qm",
        "1080p": "-qh",
        "2160p": "-qk"
    }
    quality_flag = quality_map.get(quality, "-qm") if not preview else "-ql"
    
    # FPS logic
    fps = "60" if quality == "2160p" else "30"

    # 5. Define output filename
    if preview:
        timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
        output_filename = f"{scene_name}_preview_{timestamp}.mp4"
    else:
        output_filename = f"{scene_name}_{quality}.{fmt}"

    final_output_path = os.path.join(output_dir, output_filename)

    try:
        # Run manim via subprocess
        manim_fmt = fmt if fmt in ["mp4", "gif"] else "mp4"
        
        cmd = [
            "manim", quality_flag,
            "--fps", fps,
            "--format", manim_fmt,
            "--media_dir", str(MEDIA_DIR),
            "--progress_bar", "none",
            tmp_path,
            scene_name,
        ]
        
        print(f"Executing: {' '.join(cmd)}")
        result = subprocess.run(cmd, capture_output=True, text=True, check=True)
        
        # Manim creates nested directories by default
        search_pattern = os.path.join(str(MEDIA_DIR), "videos", "**", output_filename)
        found_files = glob.glob(search_pattern, recursive=True)
        
        if found_files:
            # Move it to the root of output_dir
            shutil.copy2(found_files[0], final_output_path)
            
            # Safely cleanup the empty nested directories manim creates
            try:
                parent_dir = os.path.dirname(found_files[0])
                while parent_dir.startswith(str(MEDIA_DIR)) and parent_dir != str(MEDIA_DIR):
                    if not os.listdir(parent_dir):
                        os.rmdir(parent_dir)
                        parent_dir = os.path.dirname(parent_dir)
                    else:
                        break
            except Exception as cleanup_err:
                print(f"Non-critical cleanup error: {cleanup_err}")
        else:
            if not os.path.exists(final_output_path):
                raise RuntimeError(f"Manim finished but {output_filename} was not found. Stdout: {result.stdout}")

        # Post-processing for webm/mov if needed
        if not preview and fmt in ["webm", "mov"]:
            temp_mp4 = final_output_path
            if fmt == "webm":
                new_path = final_output_path.replace(".mp4", ".webm")
                subprocess.run(["ffmpeg", "-y", "-i", temp_mp4, "-c:v", "libvpx-vp9", "-b:v", "1M", new_path], check=True)
                os.remove(temp_mp4)
                final_output_path = new_path
            elif fmt == "mov":
                new_path = final_output_path.replace(".mp4", ".mov")
                subprocess.run(["ffmpeg", "-y", "-i", temp_mp4, "-codec", "copy", new_path], check=True)
                os.remove(temp_mp4)
                final_output_path = new_path

        return final_output_path

    except subprocess.CalledProcessError as e:
        print(f"Manim Error Stdout: {e.stdout}")
        print(f"Manim Error Stderr: {e.stderr}")
        raise RuntimeError(f"Manim rendering failed:\n{e.stderr}")
