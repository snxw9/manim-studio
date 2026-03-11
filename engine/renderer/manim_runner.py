import tempfile
import os
import subprocess
import re
import datetime
import glob
import shutil

def cleanup_previews(scene_name: str, output_dir: str):
    """Delete all preview files for a given scene name."""
    pattern = os.path.join(output_dir, f"{scene_name}_preview_*.mp4")
    for f in glob.glob(pattern):
        try:
            os.remove(f)
            print(f"Cleaned up preview: {f}")
        except Exception as e:
            print(f"Error cleaning up preview {f}: {e}")

def run_manim(code: str, preview: bool = False, quality: str = "1080p", fmt: str = "mp4") -> str:
    # 1. Parse scene name
    match = re.search(r'class\s+(\w+)\s*\(', code)
    scene_name = match.group(1) if match else "Animation"
    
    output_dir = os.path.abspath(os.getenv("MANIM_OUTPUT_DIR", "./outputs"))
    os.makedirs(output_dir, exist_ok=True)

    # 2. Handle preview cleanup if it's a final render
    if not preview:
        cleanup_previews(scene_name, output_dir)

    # 3. Create a temporary file to hold the code
    with tempfile.NamedTemporaryFile(delete=False, suffix=".py", mode="w", encoding="utf-8") as tmp:
        tmp.write(code)
        tmp_path = tmp.name
        
    # 4. Map quality to manim flags
    quality_map = {
        "480p": "-ql",
        "720p": "-qm",
        "1080p": "-qh",
        "2160p": "-qk"
    }
    quality_flag = quality_map.get(quality, "-qh") if not preview else "-ql"
    
    # 5. Define output filename
    if preview:
        timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
        output_filename = f"{scene_name}_preview_{timestamp}.mp4"
    else:
        output_filename = f"{scene_name}_{quality}.{fmt}"

    final_output_path = os.path.join(output_dir, output_filename)

    try:
        # Run manim via subprocess
        # We use --format for mp4/gif
        manim_fmt = fmt if fmt in ["mp4", "gif"] else "mp4"
        
        cmd = [
            "manim", tmp_path, scene_name,
            quality_flag,
            "--media_dir", output_dir,
            "--format", manim_fmt,
            "--output_file", output_filename,
            "--progress_bar", "none"
        ]
        
        print(f"Executing: {' '.join(cmd)}")
        result = subprocess.run(cmd, capture_output=True, text=True, check=True)
        
        # Manim creates nested directories by default (e.g., videos/tmp.../1080p60/output.mp4)
        # We want to move it directly to output_dir with our specific name
        # Let's find where manim actually put it
        search_pattern = os.path.join(output_dir, "videos", "**", output_filename)
        found_files = glob.glob(search_pattern, recursive=True)
        
        if found_files:
            # Move it to the root of output_dir
            shutil.move(found_files[0], final_output_path)
            # Cleanup the "videos" junk directory manim creates
            # (Optional: might be safer to leave it or only delete empty dirs)
        else:
            # Fallback check if it's already where we expect or check stdout
            if not os.path.exists(final_output_path):
                raise RuntimeError(f"Manim finished but {output_filename} was not found. Stdout: {result.stdout}")

        # Post-processing for webm/mov if needed
        if not preview and fmt in ["webm", "mov"]:
            temp_mp4 = final_output_path
            if fmt == "webm":
                # mp4 to webm via ffmpeg
                new_path = final_output_path.replace(".mp4", ".webm")
                subprocess.run(["ffmpeg", "-y", "-i", temp_mp4, "-c:v", "libvpx-vp9", "-b:v", "1M", new_path], check=True)
                os.remove(temp_mp4)
                final_output_path = new_path
            elif fmt == "mov":
                # mp4 to mov
                new_path = final_output_path.replace(".mp4", ".mov")
                subprocess.run(["ffmpeg", "-y", "-i", temp_mp4, "-codec", "copy", new_path], check=True)
                os.remove(temp_mp4)
                final_output_path = new_path

        return final_output_path

    except subprocess.CalledProcessError as e:
        print(f"Manim Error Stdout: {e.stdout}")
        print(f"Manim Error Stderr: {e.stderr}")
        raise RuntimeError(f"Manim rendering failed:\n{e.stderr}")
    finally:
        if os.path.exists(tmp_path):
            os.remove(tmp_path)
