#!/usr/bin/env python3
"""
Run this once to set up the engine environment.
Usage: python setup.py
"""
import os
import subprocess
import sys
from pathlib import Path

HERE = Path(__file__).parent

def run(cmd, **kwargs):
    print(f"  > {cmd}")
    result = subprocess.run(cmd, shell=True, **kwargs)
    return result.returncode == 0

def main():
    print("\nManim Studio — Engine Setup\n")

    # 1. Create venv
    venv = HERE / "venv"
    if not venv.exists():
        print("[1/3] Creating virtual environment...")
        if not run(f'python -m venv "{venv}"'):
            print("ERROR: Could not create venv. Is Python installed?")
            sys.exit(1)
    else:
        print("[1/3] Virtual environment exists — skipping")

    # 2. Install requirements
    pip = str(venv / "Scripts" / "pip.exe") if sys.platform == "win32" else str(venv / "bin" / "pip")
    print("[2/3] Installing Python dependencies...")
    run(f'"{pip}" install -r "{HERE / "requirements.txt"}" -q')

    # 3. Create .env if missing
    env_file = HERE / ".env"
    print("[3/3] Checking .env file...")
    if not env_file.exists():
        print("  Creating .env template...")
        env_file.write_text(
            "# Manim Studio Engine — API Keys\n"
            "# Get a free Groq key at https://console.groq.com\n"
            "GROQ_API_KEY=\n"
            "GEMINI_API_KEY=\n"
            "OPENAI_API_KEY=\n"
        )
        print("  Created engine/.env")
        print("  Add your Groq API key: https://console.groq.com")
    else:
        print("  .env exists — checking for keys...")
        content = env_file.read_text()
        if "GROQ_API_KEY=" in content and not content.split("GROQ_API_KEY=")[1].split("\n")[0].strip():
            print("  WARNING: GROQ_API_KEY is empty in engine/.env")
            print("  Get a free key at: https://console.groq.com")

    print("\nSetup complete.")
    print("Start the app: cd web && npm run dev\n")

if __name__ == "__main__":
    main()
