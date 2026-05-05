import subprocess, os, glob, time, shutil
from pathlib import Path

# Use a fixed directory so we can read it after
fixed_tmp = Path("C:/manim_tmp/test_run")
fixed_tmp.mkdir(parents=True, exist_ok=True)

scene_code = '''from manim import *
class TestMath(Scene):
    def construct(self):
        eq = MathTex(r"\alpha + \beta", font_size=48)
        self.play(Write(eq))
        self.wait(1)
'''

scene_file = fixed_tmp / "scene.py"
scene_file.write_text(scene_code, encoding="utf-8")

result = subprocess.run([
    r'venv\Scripts\python.exe', '-m', 'manim',
    '-ql', '--format', 'mp4',
    '--media_dir', str(fixed_tmp),
    '--progress_bar', 'none',
    str(scene_file), 'TestMath'
], capture_output=True, text=True)

print('returncode:', result.returncode)
print('--- STDERR ---')
print(result.stderr[-2000:])

# Find and print any log files
logs = list(fixed_tmp.rglob('*.log'))
print(f'\nLog files found: {len(logs)}')
for log in logs:
    print(f'\n=== {log} ===')
    print(log.read_text(encoding='utf-8', errors='ignore')[-2000:])
