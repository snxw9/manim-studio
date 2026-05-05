import tempfile, os, subprocess

# See what temp dir is being used
print('TEMP:', tempfile.gettempdir())

# Run manim with verbose output to see where it writes tex files
result = subprocess.run([
    r'venv\Scripts\python.exe', '-m', 'manim',
    '-ql', '--format', 'mp4',
    '--progress_bar', 'none',
    '--verbosity', 'DEBUG',
    'test_scene.py', 'TestMath'
], capture_output=True, text=True)
print('returncode:', result.returncode)
# Show lines mentioning tex or latex
for line in result.stderr.split('\n'):
    if any(w in line.lower() for w in ['tex', 'latex', 'log', 'temp', 'tmp', 'write']):
        print(line)
